var exec = require('cordova/exec');
var platform = require('cordova/platform');

module.exports = function(message, completeCallback) {
	exec(completeCallback, null, "DigiMarc", "DigiMarc", [message]);
}