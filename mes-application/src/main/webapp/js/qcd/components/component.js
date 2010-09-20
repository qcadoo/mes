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
	
	this.getValue = function() {
		return {
			enabled: true,
			visible: true,
			value: this.getComponentValue(),
			components: this.getComponentsValue()
		}
	}
	
	this.getComponentsValue = function() {
		return null;
	}
	
	constructor(this);
	
}