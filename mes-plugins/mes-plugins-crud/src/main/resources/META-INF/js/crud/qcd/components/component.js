/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Component = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	
	var elementPath = element.attr('id');
	var elementSearchName = elementPath.replace(/\./g,"\\.");
	var elementName = elementPath.split(".")[elementPath.split(".").length - 1];
	
	this.elementPath = elementPath;
	this.elementSearchName = elementSearchName;
	this.elementName = elementName;
	
	var isVisible = true;
	var isEnabled = true;
	
	this.contextObject = null;
	
	
	function constructor(_this) {
		var optionsElement = $("#"+elementSearchName+" > .element_options");
		if (!optionsElement.html() || $.trim(optionsElement.html()) == "") {
			_this.options = new Object();
		} else {
			_this.options = jsonParse(optionsElement.html());
		}
		optionsElement.remove();
	}
	
	this.getValue = function() {
		var valueObject = new Object();
		
		valueObject.enabled = isEnabled;
		valueObject.visible = isVisible;
		
		if (this.getComponentValue) {
			valueObject.content = this.getComponentValue();
		} else {
			valueObject.content = null;
		}
		if (this.contextObject) {
			valueObject.context = this.contextObject;
		}
		if (this.getComponentsValue) {
			valueObject.components = this.getComponentsValue();
		}
		return valueObject;
	}
	
	this.setValue = function(value) {
		this.setEnabled(value.enabled);
		this.setVisible(value.visible);
		
		if (value.content != null) {
			this.setComponentValue(value.content);
		}
		//} else {
			//this.setComponentLoading(false);
		//}
		this.setMessages(value.messages);
		if (value.components) {
			this.setComponentsValue(value);
		}
	}
	
	this.addContext = function(contextField, contextValue) {
		if (! this.contextObject) {
			this.contextObject = new Object;
		}
		this.contextObject[contextField] = contextValue;
	}
	
	this.fireEvent = function(eventName, args) {
		this.beforeEventFunction();
		mainController.callEvent(eventName, elementPath, null, args);
	}
	
	this.setState = function(state) {
		this.setEnabled(state.enabled);
		this.setVisible(state.visible);
		if (this.setComponentState) {
			this.setComponentState(state.content);
		} else {
			QCD.error(this.elementPath+".setComponentState() no implemented");
		}
		if (state.components) {
			this.setComponentsState(state);
		}
	}
	
//	this.setLoading = function(isLoadingVisible) {
//		var listeners = options.listeners;
//		if (listeners) {
//			for (var i in listeners) {
//				mainController.getComponent(listeners[i]).setLoading(isLoadingVisible);
//			}
//		}
//		if (this.setComponentLoading) {
//			this.setComponentLoading(isLoadingVisible);
//		} else {
//			QCD.error(this.elementPath+".setLoading() no implemented");
//		}
//	}
	
	this.updateSize = function(width, height) {
	}

	this.setMessages = function(messages) {
		for ( var i in messages) {
			//QCD.info(messages[i]);
			mainController.showMessage(messages[i]);
		}
	}
//	
//	this.getComponentsValue = function() {
//		return null;
//	}
//	this.setComponentsValue = function() {
//		
//	}
//	
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
				QCD.info("hide: "+this.elementPath);
				element.hide();
			}
		}
		
	}
	
	this.isVisible = function() {
		return isVisible;
	}

	this.isChanged = function() {
		return false;
	}
	
	constructor(this);
	
}