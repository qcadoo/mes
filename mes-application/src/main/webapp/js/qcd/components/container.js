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
	
//	this.setComponentsEnabled = function(isEnabled) {
//		for (var i in components) {
//			components[i].setEnabled(isEnabled);
//		}
//	}
	
	this.getComponent = function(componentName) {
		if (! componentName || componentName.trim() == "") {
			return this;
		} else {
			var name = componentName.split(".")[0];
			var path = componentName.substring(name.length+1);
			return components[name].getComponent(path);
		}
	}
	
	function constructor(_this) {
		//QCD.info("Container");
	}
	
	constructor(this);
	
}