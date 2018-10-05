System.config({
  defaultJSExtensions: true,
  transpiler: "traceur",
  paths: {
    "app/*": "src/*.js",
    "users/*": "src/users/*.js",
    "jobs/*": "src/jobs/*.js",
    "material-start/*": "src/*.js",
    "github:*": "jspm_packages/github/*",
    "npm:*": "jspm_packages/npm/*"
  },

  map: {
    "angular": "github:angular/bower-angular@1.6.2",
    "angular-animate": "github:angular/bower-angular-animate@1.6.2",
    "angular-aria": "github:angular/bower-angular-aria@1.6.2",
    "angular-datetime": "npm:angular-datetime@3.0.4",
    "angular-material": "github:angular/bower-material@master",
    "angular-messages": "github:angular/bower-angular-messages@1.6.2",
    "angular-resource": "github:angular/bower-angular-resource@1.6.2",
    "angular-route": "github:angular/bower-angular-route@1.6.2",
    "angular-ui-router": "github:angular-ui/angular-ui-router-bower@0.4.2",
    "angular-xeditable": "github:vitalets/angular-xeditable@0.6.0",
    "babel": "npm:babel-core@5.8.38",
    "babel-runtime": "npm:babel-runtime@5.8.38",
    "core-js": "npm:core-js@1.2.7",
    "css": "github:systemjs/plugin-css@0.1.33",
    "json": "github:systemjs/plugin-json@0.1.2",
    "moment": "npm:moment@2.17.1",
    "text": "github:systemjs/plugin-text@0.0.4",
    "traceur": "github:jmcriffey/bower-traceur@0.0.93",
    "traceur-runtime": "github:jmcriffey/bower-traceur-runtime@0.0.93",
    "github:angular/bower-angular-animate@1.6.2": {
      "angular": "github:angular/bower-angular@1.6.2"
    },
    "github:angular/bower-angular-aria@1.6.2": {
      "angular": "github:angular/bower-angular@1.6.2"
    },
    "github:angular/bower-angular-messages@1.6.2": {
      "angular": "github:angular/bower-angular@1.6.2"
    },
    "github:angular/bower-angular-resource@1.6.2": {
      "angular": "github:angular/bower-angular@1.6.2"
    },
    "github:angular/bower-angular-route@1.6.2": {
      "angular": "github:angular/bower-angular@1.6.2"
    },
    "github:angular/bower-material@master": {
      "angular": "github:angular/bower-angular@1.6.2",
      "angular-animate": "github:angular/bower-angular-animate@1.6.2",
      "angular-aria": "github:angular/bower-angular-aria@1.6.2",
      "angular-messages": "github:angular/bower-angular-messages@1.6.2",
      "css": "github:systemjs/plugin-css@0.1.33"
    },
    "github:jspm/nodelibs-assert@0.1.0": {
      "assert": "npm:assert@1.4.1"
    },
    "github:jspm/nodelibs-buffer@0.1.0": {
      "buffer": "npm:buffer@3.6.0"
    },
    "github:jspm/nodelibs-path@0.1.0": {
      "path-browserify": "npm:path-browserify@0.0.0"
    },
    "github:jspm/nodelibs-process@0.1.2": {
      "process": "npm:process@0.11.9"
    },
    "github:jspm/nodelibs-util@0.1.0": {
      "util": "npm:util@0.11.0"
    },
    "github:jspm/nodelibs-vm@0.1.0": {
      "vm-browserify": "npm:vm-browserify@0.0.4"
    },
    "npm:angular-datetime@3.0.4": {
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "moment": "npm:moment@2.17.1",
      "moment-timezone": "npm:moment-timezone@0.5.11"
    },
    "npm:assert@1.4.1": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "buffer": "github:jspm/nodelibs-buffer@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "util": "npm:util@0.11.0"
    },
    "npm:babel-runtime@5.8.38": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:buffer@3.6.0": {
      "base64-js": "npm:base64-js@0.0.8",
      "child_process": "github:jspm/nodelibs-child_process@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "ieee754": "npm:ieee754@1.1.8",
      "isarray": "npm:isarray@1.0.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:core-js@1.2.7": {
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:inherits@2.0.1": {
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:moment-timezone@0.5.11": {
      "moment": "npm:moment@2.17.1",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:path-browserify@0.0.0": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:process@0.11.9": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "vm": "github:jspm/nodelibs-vm@0.1.0"
    },
    "npm:util@0.11.0": {
      "inherits": "npm:inherits@2.0.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:vm-browserify@0.0.4": {
      "indexof": "npm:indexof@0.0.1"
    }
  }
});
