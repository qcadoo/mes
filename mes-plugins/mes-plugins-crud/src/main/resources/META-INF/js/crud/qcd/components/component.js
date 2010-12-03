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
	
	var isVisible = null;
	var isEnabled = null;
	
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
		this.setEnabled(value.e);
		this.setVisible(value.v);
		
		if (value.content != null) {
			this.setComponentValue(value.content);
		}
		//} else {
			//this.setComponentLoading(false);
		//}
//		this.setMessages({
//			error: value.errorMessages,
//			info: value.infoMessages,
//			success: value.successMessages
//		});
		if (value.components) {
			this.setComponentsValue(value);
		}
		//updateMode = QCD.components.Component.UPDATE_MODE_UPDATE;
	}
	
	this.addContext = function(contextField, contextValue) {
		if (! this.contextObject) {
			this.contextObject = new Object;
		}
		this.contextObject[contextField] = contextValue;
	}
	
//	this.setState = function(state) {
//		this.setEnabled(state.enabled);
//		this.setVisible(state.visible);
//		if (this.setComponentState) {
//			this.setComponentState(state.value);
//		} else {
//			QCD.error(this.elementPath+".setComponentState() no implemented");
//		}
//		if (state.components) {
//			this.setComponentsState(state);
//		}
//		updateMode = QCD.components.Component.UPDATE_MODE_IGNORE;
//	}
	
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
	
//	this.getComponent = function(componentName) {
//		if (! componentName || $.trim(componentName) == "") {
//			return this;
//		} else {
//			QCD.error("no component");
//		}
//	}
	
	this.updateSize = function(width, height) {
	}
//	
//	this.setMessages = function(messages) {
//	}
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

//	this.isChanged = function() {
//		return false;
//	}
	
	constructor(this);
	
}