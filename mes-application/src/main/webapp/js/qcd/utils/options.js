var QCDOptions = {};

QCDOptions.getElementOptions = function(elementName) {
	var optionsElement = $($("#"+elementName+" .element_options")[0]);
	//QCD.info(optionsElement);
	if (!optionsElement.html() || optionsElement.html().trim() == "") {
		var options = new Object();
	} else {
		//QCD.info(optionsElement.html());
		var options = jsonParse(optionsElement.html());
		//QCD.info(options);
	}
	optionsElement.remove();
	return options;
}