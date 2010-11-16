/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.StaticComponent = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	

	this.setComponentState = function(state) {
	}
	this.getComponentValue = function() {
		return null;
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentEnabled = function(_isEnabled) {
	}
	this.setComponentLoading = function(isLoadingVisible) {
	}
}