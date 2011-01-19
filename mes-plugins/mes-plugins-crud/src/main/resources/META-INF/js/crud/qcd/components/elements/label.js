var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.elements.Label = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	if (this.options.referenceName) {
		_mainController.registerReferenceName(this.options.referenceName, this);
	}

	this.setComponentState = function(state) {
	}
	this.getComponentValue = function() {
		return {};
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentEnabled = function(_isEnabled) {
	}
	this.setComponentLoading = function(isLoadingVisible) {
	}
}