var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec');

var digigmarc = 
{
    start:function(successCB, errorCB)
    {
		cordova.exec(successCB, errorCB, "DigiMarc", "start");
    }
};

module.exports = digigmarc;