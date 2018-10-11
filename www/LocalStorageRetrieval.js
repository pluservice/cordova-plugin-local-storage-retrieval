var exec = require("cordova/exec");

var pluginName = "LocalStorageRetrieval";

exports.getLocalStorageData = function(baseUrl, successCb, errorCb) {
  exec(successCb, errorCb, pluginName, "getLocalStorageData", [baseUrl]);
};
