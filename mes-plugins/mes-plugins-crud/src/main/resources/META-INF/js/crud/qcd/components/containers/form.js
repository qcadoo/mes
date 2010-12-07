/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	
	var elementPath = this.elementPath;
	
	var formValue = null;
	
	var baseValue = null; 
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementSearchName+"_formComponents");
		_this.constructChildren(childrenElement.children());
		block();
	}

	this.getComponentValue = function() {
		return {
			entityId: formValue,
			baseValue: baseValue
		};
	}
	
	this.setComponentValue = function(value) {
//		if(value.valid) {
//			if(value.headerEntityIdentifier) {
//				mainController.setWindowHeader(value.header + ' <span>' + value.headerEntityIdentifier + '</span>');
//			} else {
//				mainController.setWindowHeader(value.header);
//			}
//		}
		formValue = value.entityId;
		unblock();
	}
	
	this.setComponentState = function(state) {
//		if(state.headerEntityIdentifier) {
//			mainController.setWindowHeader(state.header + ' <span>' + state.headerEntityIdentifier + '</span>');
//		} else {
//			mainController.setWindowHeader(state.header);
//		}
		formValue = state.entityId;
		if (state.baseValue) {
			baseValue = state.baseValue;
		}
		unblock();
	}
	
	this.setComponentEnabled = function(isEnabled) {
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			block();
		} else {
			unblock();
		}
	}
	
	this.performUpdateState = function() {
		baseValue = formValue;
	}
	
	this.isComponentChanged = function() {
		return ! (baseValue == formValue);
	}
	
	this.performSave = function(actionsPerformer) {
		callEvent("save", actionsPerformer);
	}
	
	this.performDelete = function(actionsPerformer) {
		//var confirmDeleteMessage = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".confirmDeleteMessage";
		var confirmDeleteMessage = "confirmDeleteMessage";
		if (window.confirm(mainController.getTranslation(confirmDeleteMessage))) {
			callEvent("delete", actionsPerformer);
		}
	}
	
	this.performCancel = function(actionsPerformer) {
		//var confirmCancelMessage = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".confirmCancelMessage";
		var confirmCancelMessage = "confirmCancelMessage";
		if (window.confirm(mainController.getTranslation(confirmCancelMessage))) {
			callEvent("reset", actionsPerformer);
		}
	}
	
	function callEvent(eventName, actionsPerformer) {
		block();
		mainController.callEvent(eventName, elementPath, function() {
			unblock();
		}, null, actionsPerformer);
	}
	
//	this.performCallFunction = function(actionsPerformer, functionName, additionalAttribute) {
//		if (formValue && formValue.id) {
//			mainController.performCallFunction(functionName, additionalAttribute, formValue.id, actionsPerformer);
//		} else {
//			entityWithoutIdentifier = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".entityWithoutIdentifier";
//			mainController.showMessage("error", mainController.getTranslation(entityWithoutIdentifier)); 
//		}
//	}
	
	this.updateSize = function(_width, _height) {
		width = _width - 40;
		if(width > 1380) {
			width = 1380;
		} 
		element.width(width);
		for (var i in this.components) {
			this.components[i].updateSize(width, height);
		}
	}
	
	function block() {
		element.block({ message: '<div class="loading_div">'+mainController.getTranslation("commons.loading")+'</div>', showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: '#000', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: .5, 
            color: '#fff' } });
	}
	
	function unblock() {
		element.unblock();
	}
	
	constructor(this);
}