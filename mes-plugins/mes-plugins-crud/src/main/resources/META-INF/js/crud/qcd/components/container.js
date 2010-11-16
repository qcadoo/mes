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
	
	this.isContainer = true;
	
	this.constructChildren = function(childrenElements) {
		components = QCDPageConstructor.getChildrenComponents(childrenElements, mainController);
		this.components = components;
	}
	
	this.insterData = function(data) {
		for (var i in data) {
			var component = this.components[i];
			component.insterData(data[i]);
		}
	}
	
	this.getComponentsValue = function() {
		var values = null;
		for (var i in components) {
			var value = components[i].getValue();
			if (value != null) {
				if (values == null) {
					values = new Object();
				}
				values[i] = value;
			}
		}
		return values;
	}
	
	this.setComponentsValue = function(value) {
		for (var i in value.components) {
			var componentValue = value.components[i];
			if (value.enabled == false)
				componentValue.enabled = false;
			components[i].setValue(componentValue);
		}
	}
	
	this.setComponentsState = function(state) {
		if (this.setComponentState) {
			this.setComponentState(state.value);
		} else {
			QCD.error(this.elementPath+".setComponentState() no implemented");
		}
		for (var i in state.components) {
			var componentState = state.components[i];
			components[i].setState(componentState);
		}
	}
	
	this.getComponent = function(componentName) {
		if (! componentName || $.trim(componentName) == "") {
			return this;
		} else {
			var name = componentName.split(".")[0];
			var path = componentName.substring(name.length+1);
			return components[name].getComponent(path);
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
	
	function constructor(_this) {
	}
	
	constructor(this);
	
}