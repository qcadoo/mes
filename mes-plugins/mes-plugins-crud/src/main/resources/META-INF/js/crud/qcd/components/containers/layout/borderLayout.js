var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.BorderLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementSearchName+"_layoutComponents");
		_this.constructChildren(childrenElement.children());
	}
	
	this.getComponentValue = function() {
		return {};
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentState = function(state) {
	}
	
	this.setMessages = function(messages) {
	}
	
	this.setComponentEnabled = function(isEnabled) {
		
	}
	
	this.setComponentLoading = function() {
		
	}
	
	constructor(this);
}