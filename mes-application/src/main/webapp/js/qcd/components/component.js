var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Component = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	this.elementPath = element.attr('id');
	var elementName = this.elementPath.split("-")[this.elementPath.split("-").length - 1];
	this.elementName = elementName;
	
	var options;
	
	var isVisible;
	var isEnabled;
	
	function constructor(_this) {
		options = QCDOptions.getElementOptions(_this.elementPath);
		_this.options = options;
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
		if (value.value) {
			this.setComponentValue(value.value);
		}
		this.setMessages({
			error: value.errorMessages,
			info: value.infoMessages,
			success: value.successMessages
		});
		if (value.components) {
			this.setComponentsValue(value);
		}
	}
	
	this.setLoading = function(isLoadingVisible) {
		var listeners = options.listeners;
		if (listeners) {
			for (var i in listeners) {
				mainController.getComponent(listeners[i]).setLoading(isLoadingVisible);
			}
		}
		if (this.setComponentLoading) {
			this.setComponentLoading(isLoadingVisible);
		} else {
			QCD.error(this.elementPath+".setLoading() no implemented");
		}
	}
	
	this.getComponent = function(componentName) {
		if (! componentName || componentName.trim() == "") {
			return this;
		} else {
			QCD.error("no component");
		}
	}
	
	this.setMessages = function(messages) {
	}
	
	this.getComponentsValue = function() {
		return null;
	}
	this.setComponentsValue = function() {
		
	}
	
	this.setEnabled = function(_isEnabled) {
		isEnabled = _isEnabled;
		this.setComponentEnabled(isEnabled);
		//if (this.isContainer) {
		//	this.setComponentsEnabled(isEnabled);	
		//}
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