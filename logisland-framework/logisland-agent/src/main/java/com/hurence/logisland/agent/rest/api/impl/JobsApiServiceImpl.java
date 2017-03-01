package com.hurence.logisland.agent.rest.api.impl;

import com.hurence.logisland.agent.rest.api.ApiResponseMessage;
import com.hurence.logisland.agent.rest.api.JobsApiService;
import com.hurence.logisland.agent.rest.api.NotFoundException;
import com.hurence.logisland.agent.rest.model.Engine;
import com.hurence.logisland.agent.rest.model.Error;
import com.hurence.logisland.agent.rest.model.Job;
import com.hurence.logisland.agent.rest.model.JobSummary;
import com.hurence.logisland.kakfa.registry.KafkaRegistry;
import com.hurence.logisland.kakfa.registry.exceptions.RegistryException;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.hurence.logisland.agent.rest.model.JobSummary.StatusEnum.*;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-15T10:15:35.873+01:00")
public class JobsApiServiceImpl extends JobsApiService {

    public JobsApiServiceImpl(KafkaRegistry kafkaRegistry) {
        super(kafkaRegistry);
    }


    private static Logger logger = LoggerFactory.getLogger(JobsApiService.class);


    //------------------------
    //  CRUD Jobs section
    //------------------------

    @Override
    public Response addJobWithId(Job body, String jobId, SecurityContext securityContext) throws NotFoundException {
        return addJob(body.name(jobId), securityContext);
    }

    @Override
    public Response addJob(Job job, SecurityContext securityContext) throws NotFoundException {
        logger.debug("adding job " + job);

        try {
            Job job0 = kafkaRegistry.addJob(job);
            return Response.ok().entity(job0).build();
        } catch (RegistryException e) {
            String error = "unable to add job into kafkastore " + e;
            logger.error(error);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, error))
                    .build();
        }


    }

    @Override
    public Response updateJob(String jobId, Job job, SecurityContext securityContext) throws NotFoundException {
        logger.debug("update job " + job);

        try {
            // update date modified
            updateJobStatus(job, job.getSummary().getStatus());
            Job job0 = kafkaRegistry.updateJob(job);
            return Response.ok().entity(job0).build();
        } catch (RegistryException e) {
            String error = "unable to update job into kafkastore " + e;
            logger.error(error);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, error))
                    .build();
        }
    }


    @Override
    public Response deleteJob(String jobId, SecurityContext securityContext) throws NotFoundException {
        logger.debug("delete job");
        try {
            kafkaRegistry.deleteJob(jobId);
            return Response.ok().build();
        } catch (RegistryException e) {
            String error = "unable to get alls job from kafkastore " + e;
            logger.error(error);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, error))
                    .build();
        }
    }

    @Override
    public Response getAllJobs(SecurityContext securityContext) throws NotFoundException {

        logger.debug("get all jobs");
        try {
            List<Job> jobs = kafkaRegistry.getAllJobs();
            return Response.ok().entity(jobs).build();
        } catch (RegistryException e) {
            String error = "unable to get alls job from kafkastore " + e;
            logger.error(error);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, error))
                    .build();
        }

    }

    @Override
    public Response getJob(String jobId, SecurityContext securityContext) throws NotFoundException {

        logger.debug("get job " + jobId);


        Job job = null;
        try {
            job = kafkaRegistry.getJob(jobId);
        } catch (RegistryException e) {
            return Response.serverError().entity(e).build();
        }

        if (job == null)
            return Response.serverError()
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Error().code(404).message("Job not found for id: " + jobId))
                    .build();
        else
            return Response.ok().entity(job).build();
    }


    @Override
    public Response getJobEngine(String jobId, SecurityContext securityContext) throws NotFoundException {
        logger.debug("get job engine" + jobId);


        StringBuilder builder = new StringBuilder();
        try {
            Engine engine = kafkaRegistry.getJob(jobId).getEngine();
            engine.getConfig().stream().forEach(property -> {
                builder.append(property.getKey());
                builder.append(": ");
                builder.append(property.getValue());
                builder.append("\n");
            });
        } catch (RegistryException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Unable to find job " + jobId))
                    .build();
        }


        return Response.ok().entity(builder.toString()).build();
    }


    //------------------------
    //  Jobs metrology
    //------------------------
    @Override
    public Response getJobAlerts(Integer count, SecurityContext securityContext) throws NotFoundException {
        return null;
    }

    @Override
    public Response getJobErrors(Integer count, SecurityContext securityContext) throws NotFoundException {
        return null;
    }

    @Override
    public Response getJobMetrics(Integer count, SecurityContext securityContext) throws NotFoundException {
        return null;
    }


    //------------------------
    // Jobs scheduling
    //------------------------
    @Override
    public Response getJobStatus(String jobId, SecurityContext securityContext) throws NotFoundException {

        Job job = null;
        try {
            job = kafkaRegistry.getJob(jobId);
            if (job == null)
                throw new RegistryException("job " + jobId + "not found !");
        } catch (RegistryException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Unable to find job " + jobId))
                    .build();
        }

        return Response.ok().entity(job.getSummary().getStatus()).build();
    }

    @Override
    public Response pauseJob(String jobId, SecurityContext securityContext) throws NotFoundException {
        Job job = null;
        try {
            job = kafkaRegistry.getJob(jobId);
            if (job == null)
                throw new RegistryException("job " + jobId + "not found !");
        } catch (RegistryException e) {
            return Response.serverError().entity(e).build();
        }

        switch (job.getSummary().getStatus()) {
            case PAUSED:
                updateJobStatus(job, RUNNING);
                return updateJob(jobId, job, securityContext);
            case RUNNING:
                updateJobStatus(job, PAUSED);
                return updateJob(jobId, job, securityContext);
            default:
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Unable to pause a " + job.getSummary().getStatus() + " job " + jobId))
                        .build();
        }
    }

    @Override
    public Response shutdownJob(String jobId, SecurityContext securityContext) throws NotFoundException {

        Job job = null;
        try {
            job = kafkaRegistry.getJob(jobId);
            if (job == null)
                throw new RegistryException("job " + jobId + "not found !");
        } catch (RegistryException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Unable to shutdown job " + jobId))
                    .build();
        }

        updateJobStatus(job, STOPPED);
        return updateJob(jobId, job, securityContext);
    }

    @Override
    public Response startJob(String jobId, SecurityContext securityContext) throws NotFoundException {

        Job job = null;
        try {
            job = kafkaRegistry.getJob(jobId);
            if (job == null)
                throw new RegistryException("job " + jobId + "not found !");
        } catch (RegistryException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Unable to start job " + jobId))
                    .build();
        }

        CommandLine cmdLine = new CommandLine("bin/logisland-launch-spark-job");
        cmdLine.addArgument("--agent");
        cmdLine.addArgument("localhost:8081");
        cmdLine.addArgument("--job");
        cmdLine.addArgument(jobId);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        Executor executor = new DaemonExecutor();
        executor.setExitValue(1);
        // executor.setWatchdog(watchdog);
        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateJobStatus(job, RUNNING);


        return updateJob(jobId, job, securityContext);
    }

    private void updateJobStatus(Job job, JobSummary.StatusEnum newStatus) {
        JobSummary summary = job.getSummary();
        summary.dateModified(new Date())
                .status(newStatus);
        job.setSummary(summary);
    }

}
