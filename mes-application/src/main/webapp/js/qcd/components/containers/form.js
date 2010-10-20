var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	var elementPath = this.elementPath;
	
	var buttons = new Object();
	
	buttons.saveButton = $("#"+elementName+"_saveButton");
	
	var formValue = null;
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementPath+"_formComponents");
		_this.constructChildren(childrenElement.children());
		block();
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
		mainController.performSave(elementName, actionsPerformer);
	}
	
	this.performDelete = function(actionsPerformer) {
		if (window.confirm(mainController.getTranslation("commons.confirm.deleteMessage"))) {
			block();
			mainController.performDelete(elementPath, formValue ? formValue.id : null, actionsPerformer);
		}
	}
	
	this.performCancel = function(actionsPerformer) {
		if (window.confirm("cancel?")) {
			block();
			mainController.performCancel(formValue ? formValue.id : null, actionsPerformer);
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
		element.block({ message: mainController.getTranslation("commons.loading.gridLoading"), showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
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