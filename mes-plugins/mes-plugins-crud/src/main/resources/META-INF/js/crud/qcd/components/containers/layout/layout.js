var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.Layout = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var element = this.element;
	
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
	
	this.setBackground = function(color) {
		element.css("backgroundColor", color);
	}
}

QCD.components.containers.layout.Layout.COLOR_DISABLED = "#dbdbdb";
QCD.components.containers.layout.Layout.COLOR_NORMAL = "transparent";