var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Component = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	var options;
	
	var isVisible;
	var isEnabled;
	
	function constructor(_this) {
		options = QCDOptions.getElementOptions(elementName);
		_this.options = options;
		//QCD.info("Component");
	}
	
	this.getValue = function() {
		return {
			enabled: this.isEnabled(),
			visible: this.isVisible(),
			value: this.getComponentValue(),
			components: this.getComponentsValue()
		}
	}
	
	this.setValue = function(value) {
		this.setEnabled(value.enabled);
		this.setVisible(value.visible);
		this.setComponentValue(value.value);
		if (value.components) {
			this.setComponentsValue(value.components);
		}
	}
	
	this.setLoading = function(isLoadingVisible) {
		QCD.error(elementName+".setLoading() no implemented");
	}
	
	this.getComponent = function(componentName) {
		if (! componentName || componentName.trim() == "") {
			return this;
		} else {
			QCD.error("no component");
		}
	}
	
	this.getComponentsValue = function() {
		return null;
	}
	this.setComponentsValue = function() {
		
	}
	
	this.setEnabled = function(_isEnabled) {
		isEnabled = _isEnabled;
		this.setComponentEnabled(isEnabled);
	}
	
	this.isEnabled = function() {
		return isEnabled;
	}
	
	this.setVisible = function(_isVisible) {
		isVisible = _isVisible;
		if (this.setComponentVisible) {
			this.setComponentVisible(isVisible);
		} else {
			if (isVisible) {
				element.show();
			} else {
				element.hide();
			}
		}
		
	}
	
	this.isVisible = function() {
		return isVisible;
	}
	
	constructor(this);
	
}