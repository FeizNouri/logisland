
export default ['$mdSidenav', '$log', ListService];

function ListService($mdSidenav, $log) {
    var lists = ['left_jobs', 'right_processors'];

    return {
        toggle: function(id) {
            $mdSidenav(id, true)
                .toggle()
                .then(function() {
                    $log.debug("toggleList(" + id + ")");
                });
        },
        close: function(id) {
            $mdSidenav(id, true)
                .close()
                .then(function() {
                    $log.debug("close(" + id + ")");
                 });
        }
    }
}


