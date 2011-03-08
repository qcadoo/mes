/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Component = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	
	var elementPath = element.attr('id');
	var elementSearchName = elementPath.replace(/\./g,"\\.");
	var elementName = elementPath.split(".")[elementPath.split(".").length - 1];
	
	this.element = element;
	this.elementPath = elementPath;
	this.elementSearchName = elementSearchName;
	this.elementName = elementName;
	
	var isVisible = true;
	var isEnabled = true;
	
	this.contextObject = null;
	
	var onChangeListeners;
	
	function constructor(_this) {
		var optionsElement = $("#"+elementSearchName+" > .element_options");
		if (!optionsElement.html() || $.trim(optionsElement.html()) == "") {
			_this.options = new Object();
		} else {
			_this.options = jsonParse(optionsElement.html());
		}
		optionsElement.remove();
		isVisible = _this.options.defaultVisible;
		isEnabled = _this.options.defaultEnabled;
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
		this.setMessages(value.messages);
		
		if (value.components) {
			this.setComponentsValue(value);
		}
		if (value.content != null) {
			this.setComponentValue(value.content);
		}
		if (value.updateState) {
			this.performUpdateState();
		}
		if (onChangeListeners) {
			this.fireOnChangeListeners("onSetValue", [value]);
		}
	}
	
	this.performUpdateState = function() {
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
		if (onChangeListeners) {
			this.fireOnChangeListeners("onSetValue", [state]);
		}
	}
	
	this.setEditable = function(isEditable) {
		this.setComponentEditable(isEditable);
	}
	
	this.performScript = function() {
		if (this.options.script) {
			mainController.getActionEvaluator().performJsAction(this.options.script, this);
		}
		if (this.performComponentScript) {
			this.performComponentScript();
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
			mainController.showMessage(messages[i]);
		}
	}

	this.setEnabled = function(_isEnabled, isDeep) {
		isEnabled = _isEnabled;
		this.setComponentEnabled(isEnabled);
		if (isDeep && this.components) {
			for (var i in this.components) {
				this.components[i].setEnabled(_isEnabled, isDeep);
			}
		}
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

	this.addOnChangeListener = function(listener) {
		if (!onChangeListeners) {
			onChangeListeners = new Array();
		}
		onChangeListeners.push(listener);
	}
	
	this.fireOnChangeListeners = function(method, args) {
		for (var i in onChangeListeners) {
			var func = onChangeListeners[i][method];
			if (func) {
				func.apply(this, args);
			}
		}
	}
	
	this.isChanged = function() {
		return this.isComponentChanged();
	}
	
	this.isComponentChanged = function() {
		return false;
	}
	
	constructor(this);

}