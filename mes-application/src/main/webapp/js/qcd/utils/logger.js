var QCDLogger = {};

QCDLogger.info = function(msg) {
	if (window.console && window.console.info) {
		window.console.info(msg);
	}
};

QCDLogger.debug = function(msg) {
	if (window.console && window.console.debug) {
		window.console.debug(msg);
	}
};