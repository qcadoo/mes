var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Component = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	var options;
	
	function constructor(_this) {
		options = QCDOptions.getElementOptions(elementName);
		_this.options = options;
		//QCDLogger.info("Component");
	}
	
	constructor(this);
	
}