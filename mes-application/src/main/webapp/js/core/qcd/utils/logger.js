/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};

QCD.info = function(msg) {
	if (window.console && window.console.info) {
		window.console.info(msg);
	}
};

QCD.debug = function(msg) {
	if (window.console && window.console.debug) {
		window.console.debug(msg);
	}
};

QCD.error = function(msg) {
	if (window.console && window.console.error) {
		window.console.error(msg);
	}
};