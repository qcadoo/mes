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
	//var element = _element;
	//var elementName = element.attr('id');
	
	//var elementPath = this.elementPath;
	
	//var buttons = new Object();
	
	//buttons.saveButton = $("#"+elementName+"_saveButton");
	
	//var formValue = null;
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementPath+"_formComponents");
		QCD.info(childrenElement);
		_this.constructChildren(childrenElement.children());
		//block();
	}

	this.getComponentValue = function() {
		return formValue;
	}
	
	this.setComponentValue = function(value) {
		if(value.valid) {
			if(value.headerEntityIdentifier) {
				mainController.setWindowHeader(value.header + ' <span>' + value.headerEntityIdentifier + '</span>');
			} else {
				mainController.setWindowHeader(value.header);
			}
		}
		formValue = value;
		unblock();
	}
	
	this.setComponentState = function(state) {
		if(state.headerEntityIdentifier) {
			mainController.setWindowHeader(state.header + ' <span>' + state.headerEntityIdentifier + '</span>');
		} else {
			mainController.setWindowHeader(state.header);
		}
		formValue = state;
		unblock();
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (buttons.saveButton) {
			if (isEnabled) {
				buttons.saveButton.removeAttr('disabled');
			} else {
				buttons.saveButton.attr('disabled', 'true');
				unblock();
			}
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			block();
		} else {
			unblock();
		}
	}
	
	this.performSave = function(actionsPerformer) {
		block();
		mainController.performSave(elementName, actionsPerformer, function() {
			unblock();
		});
	}
	
	this.performDelete = function(actionsPerformer) {
		var confirmDeleteMessage = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".confirmDeleteMessage";
		if (window.confirm(mainController.getTranslation(confirmDeleteMessage))) {
			block();
			mainController.performDelete(elementPath, formValue ? formValue.id : null, actionsPerformer);
		}
	}
	
	this.performCancel = function(actionsPerformer) {
		var confirmCancelMessage = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".confirmCancelMessage";
		if (window.confirm(mainController.getTranslation(confirmCancelMessage))) {
			block();
			mainController.performCancel(formValue ? formValue.id : null, actionsPerformer);
		}
	}
	
	this.callUpdateFunction = function(actionsPerformer, functionTriggerName) {
		mainController.performCallUpdateFunction(functionTriggerName, actionsPerformer);
	}
	
	this.performCallFunction = function(actionsPerformer, functionName, additionalAttribute) {
		if (formValue && formValue.id) {
			mainController.performCallFunction(functionName, additionalAttribute, formValue.id, actionsPerformer);
		} else {
			entityWithoutIdentifier = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".entityWithoutIdentifier";
			mainController.showMessage("error", mainController.getTranslation(entityWithoutIdentifier)); 
		}
	}
	
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
	
	this.setMessages = function(messages) {
		for ( var i in messages.error) {
			mainController.showMessage("error", messages.error[i]);
		}
	}
	
	constructor(this);
}