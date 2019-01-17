#!/usr/bin/env bash

# Assigns the provided variable to the specified value if the variable is undefined.
# @param the variable name to check for nullity
# @param the variable value to assign in case the variable is undefined
default_value()
{
  require_args "${#}" 2 "default_value ${1} <value>"

  local VARIABLE_NAME=\$"${1}"
  local VARIABLE_VALUE=`eval "expr \"$VARIABLE_NAME\""`

  if [[ -z "${VARIABLE_VALUE}" ]]
  then
    eval "${1}=${2}"
  fi

  debug "Default value ${1}="`eval "expr \\$${1}"`
}

# Ensures the specified file is present. Abort the script if the file is not found.
# @param the file to check
file_present()
{
  require_args "${#}" 1 "file_present <filepath>"

  test -f "$1"

  abort_if "${?}" "File '$1' was not found. Aborting."
}

# Aborts if the condition code specified as arg #1 is not 0 and prints the aborting message specified as arg #2.
# @param the condition code
# @param the aborting message
abort_if()
{
  if [[ "${#}" -ne 2 ]]
  then
    echo "function 'abort_if' expects 2 arguments but ${#} provided. Aborting."
    abort 1
  fi

  if [[ "${1}" -ne 0 ]]
  then
    echo "$2"
    abort 1
  fi
}

# Aborts if the expected argument check failed.
# @param the number of arguments passed to the calling function
# @param the number of arguments expected by the calling function
# @param the name of the calling function
require_args()
{
  test "${1}" -ge "${2}"
  abort_if "${?}" "function '${3}' expects ${2} argument but ${1} provided. Aborting."
}

# Prints the provided log if DEBUG is set.
# @param(s) the log message
debug()
{
  if [[ -n "${DEBUG}" ]]
  then
    >&2 echo "${@}"
  fi
}

# Polls by executing the specified command the specified number of attempts waiting the specified amount of seconds
# between each attempt.
# @param number of attempts
# @param amount of seconds between each attempt
# @param(s) the command to run
poll()
{
  local TOTAL_ATTEMPTS=$1
  local WAIT_IN_SECONDS=$2
  local args=( "$@" )
  local CMD=${args[@]:2}
  debug "Polling command: ${CMD}"

  local returnCode=1 # fail by default

  local attempts=0
  while [[ ${attempts} -le ${TOTAL_ATTEMPTS} ]]
  do
    attempts=$(( $attempts + 1 ))
    # Run omitting parameters #1 and #2
    ${CMD} > /dev/null
    returnCode=$?
    if [ ${returnCode} -eq 0 ]
    then
      break
    fi
    sleep ${WAIT_IN_SECONDS}
  done

  return ${returnCode}
}

# Returns 0 if the provided topics are present in zookeeper; something else otherwise.
# @params any number of topic to lookup.
lookup_kafka_topics()
{
    declare -a lift_topic_not_found=()
    declare -a list_topic_to_find=${@}
    echo "topics to verify existence ${list_topic_to_find[*]}"
    declare -a -r list_topic=($(${KAFKA_HOME}/bin/kafka-topics.sh --list --zookeeper ${ZK_QUORUM} | awk '{print $1}'))
    echo "found topics ${list_topic[*]}"

    for topic_to_find in "${list_topic_to_find[@]}"
    do
        skip=false
        for topic in "${list_topic[@]}"
        do
            [[ ${topic_to_find} == ${topic} ]] && { skip=true; break; }
        done
        [[ -n ${skip} ]] || lift_topic_not_found+=("${topic_to_find}")
    done

    if [[ ${#lift_topic_not_found[@]} -eq 0 ]];then
        echo "all expected topic(s) exist(s)"
      return 0
    else
      echo "Some expected topic(s) does not exist: ${lift_topic_not_found[@]}"
      return 1
    fi
}

# Sends the specified file to Kafka using the command kafkacat within the docker container.
#
# @required KAFKACAT_BIN: the kafkacat binary
# @required KAFKA_URL: the kafka broker connection
# @required KAFKA_TOPIC: the kafka queue name
#
# In case docker is used for the tests, the properties below are also required
# @required DOCKER_BIN: the docker binary
# @required LOGISLAND_DOCKER_CONTAINER_ID: the docker container identifier
#
# @param file to send using kafkacat
# @param topic to data

kafkacat()
{
    echo "cat ${1} | ${KAFKACAT_BIN} -P -b ${KAFKA_BROKER_URL} -t ${2}"
    cat ${1} | ${KAFKACAT_BIN} -P -b ${KAFKA_BROKER_URL} -t ${2}
}


# main class that test
main() {

    echo "initializing variables"
    #SET CONSTANT AND ENVIRONMENT VARIABLES
    CONF_FILE="logisland-config.yml"
    INPUT_FILE_PATH="/conf/input"
    EXPECTED_FILE_PATH="/conf/input"
    KAFKA_INPUT_TOPIC="logisland_raw"
    KAFKA_OUTPUT_TOPIC="logisland_events"
#    KAFKA_OUTPUT_TOPIC_2="logisland_aggregations"
    KAFKA_ERROR_TOPIC="logisland_errors"
    #DEBUG="set -x"#Comment if you do not want debug

    KAFKA_BROKER_HOST="kafka"
    KAFKA_BROKER_PORT="9092"
    KAFKA_BROKER_URL="${KAFKA_BROKER_HOST}:${KAFKA_BROKER_PORT}"
    default_value KAFKACAT_BIN "/usr/local/bin/kafkacat"

    export KAFKA_BROKERS="${KAFKA_BROKER_HOST}:${KAFKA_BROKER_PORT}"
    export ZK_QUORUM="zookeeper:2181"

    echo "starting logisland with ${CONF_FILE}"
    nohup bin/logisland.sh --conf /conf/${CONF_FILE} & > ${CONF_FILE}_job.log
    sleep 10
    echo "waiting 10 seconds for job to initialize"

    echo "some check before sending data"
    file_present "${INPUT_FILE_PATH}"
    file_present "${EXPECTED_FILE_PATH}"

    # Ensure kafka topic is created before sending data.
    lookup_kafka_topics ${KAFKA_INPUT_TOPIC} ${KAFKA_OUTPUT_TOPIC} ${KAFKA_ERROR_TOPIC}

    # Sends data to kafka.
    echo "sending input in kafka"
    EXPECTED_DOCS_COUNT=$(${DEBUG}; wc "${INPUT_FILE_PATH}" | awk '{print $1}')
    echo "EXPECTED_DOCS_COUNT ${EXPECTED_DOCS_COUNT}"
#    echo "cat ${INPUT_FILE_PATH} | ${KAFKACAT_BIN} -P -b ${KAFKA_BROKER_URL} -t ${KAFKA_INPUT_TOPIC}"
    echo "cat ${INPUT_FILE_PATH} | ${KAFKA_HOME}/bin/kafka-console-producer.sh --broker-list ${KAFKA_BROKER_URL} --topic ${KAFKA_INPUT_TOPIC}"
    cat ${INPUT_FILE_PATH} | ${KAFKA_HOME}/bin/kafka-console-producer.sh --broker-list ${KAFKA_BROKER_URL} --topic ${KAFKA_INPUT_TOPIC}
    abort_if "${?}" "Unable to send input ${INPUT_FILE_PATH}  into ${KAFKA_INPUT_TOPIC}. Aborting."

    echo "check that we received it"
    #Test first stream pipe
    REAL_DOCS_COUNT=$( \
    ${KAFKA_HOME}/bin/kafka-console-consumer.sh --topic ${KAFKA_OUTPUT_TOPIC} \
    --zookeeper ${ZK_QUORUM} \
    --from-beginning \
    --timeout-ms 2000 \
    | grep '\"id\" :' \
    | wc -l \
    )
    abort_if "${?}" "Unable to count events in ${KAFKA_OUTPUT_TOPIC}. Aborting."
    echo "sent ${EXPECTED_DOCS_COUNT} inputs and got ${REAL_DOCS_COUNT} outputs"
    if [[ ${EXPECTED_DOCS_COUNT} == ${REAL_DOCS_COUNT} ]]
    then
        echo "first stream ok"
    else
        echo "first stream did not receive events"
        exit 1
    fi

    #Test second stream pipe
    sleep 5
    echo "waiting 5 seconds for job to initialize"


    if [ -d "kafka_to_hdfs" ]; then
        echo "directory kafka_to_hdfs has correctly been created"
    else
        echo "directory kafka_to_hdfs has not correctly been created"
        exit 1
    fi

    if [ -d "kafka_to_hdfs/record_daytime=1995-07-01/record_type=apache_log/" ]; then
        echo "directory 'kafka_to_hdfs/record_daytime=1995-07-01/record_type=apache_log/' has correctly been created"
    else
        echo "directory 'kafka_to_hdfs/record_daytime=1995-07-01/record_type=apache_log/' has not correctly been created"
        exit 1
    fi
}

main $@


