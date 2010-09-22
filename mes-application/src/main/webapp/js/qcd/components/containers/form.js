var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	var buttons = new Object();
	buttons.saveButton = $("#"+elementName+"_saveButton");
	
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
	
	function performSave() {
		mainController.performSave(elementName);
	}
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementPath+"_formComponents");
		_this.constructChildren(childrenElement.children());
		//mainWindow-beanAForm_saveButton
		buttons.saveButton.click(performSave);
	}
	
	this.getComponentValue = function() {
		return formValue;
	}
	
	this.setComponentValue = function(value) {
		formValue = value;
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (buttons.saveButton) {
			if (isEnabled) {
				buttons.saveButton.removeAttr('disabled');
			} else {
				buttons.saveButton.attr('disabled', 'true');
			}
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			element.block({ message: mainController.getTranslation("commons.loading.gridLoading"), showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
	            border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#000', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: .5, 
	            color: '#fff' } });
		} else {
			element.unblock();
		}
	}
	
	constructor(this);
}