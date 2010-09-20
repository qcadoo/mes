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
	
	this.setValue = function(value) {
		var componentValue = {
			enabled: value.enabled,
			visible: value.visible,
			value: value.value
		}
		this.setComponentValue(componentValue);
		if (value.components) {
			this.setComponentsValue(value.components);
		}
	}
	
	this.getComponentsValue = function() {
		return null;
	}
	this.setComponentsValue = function() {
		
	}
	
	constructor(this);
	
}