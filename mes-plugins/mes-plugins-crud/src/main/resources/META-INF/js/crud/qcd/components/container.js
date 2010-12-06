/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Container = function(_element, _mainController, childrenElements) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	var mainController = _mainController;

	var components;
	
	this.constructChildren = function(childrenElements) {
		components = QCDPageConstructor.getChildrenComponents(childrenElements, mainController);
		this.components = components;
	}
	
	this.getComponentsValue = function() {
		var values = new Object();
		for (var i in components) {
			values[i] = components[i].getValue();
		}
		return values;
	}
	
	this.setComponentsValue = function(value) {
		for (var i in value.components) {
			var componentValue = value.components[i];
			components[i].setValue(componentValue);
		}
	}
	
	this.setComponentsState = function(state) {
		for (var i in state.components) {
			var componentState = state.components[i];
			components[i].setState(componentState);
		}
	}

	
	this.isChanged = function() {
		changed = false;
		for (var i in components) {
			if(components[i].isChanged()) {
				changed = true;
			}
		}
		return changed;
	}
	
}