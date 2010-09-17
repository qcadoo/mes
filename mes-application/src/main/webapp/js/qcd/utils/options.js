var QCDOptions = {};

QCDOptions.getElementOptions = function(elementName) {
	var optionsElement = $("#"+elementName+" .element_options");
	if (!optionsElement.html() || optionsElement.html().trim() == "") {
		var options = new Object();
	} else {
		//QCDLogger.info(optionsElement.html());
		var options = jsonParse(optionsElement.html());
	}
	optionsElement.remove();
	return options;
}