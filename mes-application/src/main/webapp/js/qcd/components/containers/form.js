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
	
	var messagesSpan = $("#"+elementName+"_messagesSpan");
	
	var formValue = null;
	
//	this.insterData = function(data) {
//		QCD.info(this.containerComponents);
//		for (var i in data) {
//			var component = this.containerComponents[i];
//			QCD.info(component);
//			component.insterData(data[i]);
//		}
//	}
//	
//	function performCancel() {
//		mainController.goBack();
//	}
	
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementPath+"_formComponents");
		_this.constructChildren(childrenElement.children());
		block();
		//mainWindow-beanAForm_saveButton
		//buttons.saveButton.click(performSave);
	}
	
	this.setMessages = function(messages) {
		var message = "";
		for (var i in messages.error) {
			if (message != "") {
				message += ", ";
			}
			message += messages.error[i];
		}
		for (var i in messages.info) {
			if (message != "") {
				message += ", ";
			}
			message += messages.info[i];
		}
		for (var i in messages.success) {
			if (message != "") {
				message += ", ";
			}
			message += messages.success[i];
		}
		messagesSpan.html(message);
	}
	
	this.getComponentValue = function() {
		return formValue;
	}
	
	this.setComponentValue = function(value) {
		formValue = value;
		unblock();
	}
	
	this.setComponentState = function(state) {
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
			mainController.performDelete(elementPath, formValue, actionsPerformer);
		}
	}
	
	this.performCancel = function(actionsPerformer) {
		if (window.confirm("cancel?")) {
			block();
			mainController.performCancel(formValue, actionsPerformer);
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