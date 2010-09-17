var QCDOptions = {};

QCDOptions.getElementOptions = function(elementName) {
	var optionsElement = $($("#"+elementName+" .element_options")[0]);
	//QCDLogger.info(optionsElement);
	if (!optionsElement.html() || optionsElement.html().trim() == "") {
		var options = new Object();
	} else {
		//QCDLogger.info(optionsElement.html());
		var options = jsonParse(optionsElement.html());
		//QCDLogger.info(options);
	}
	optionsElement.remove();
	return options;
}