var QCDOptions = {};

QCDOptions.getElementOptions = function(elementName) {
	var optionsElement = $($("#"+elementName+" .element_options")[0]);
	if (!optionsElement.html() || $.trim(optionsElement.html()) == "") {
		var options = new Object();
	} else {
		var options = jsonParse(optionsElement.html());
	}
	optionsElement.remove();
	return options;
}