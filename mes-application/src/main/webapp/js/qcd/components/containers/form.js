var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
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
		var childrenElement = $("#"+elementName+" .formComponents");
		_this.constructChildren(childrenElement.children());
		//mainWindow-beanAForm_saveButton
		$("#"+elementName+"_saveButton").click(performSave);
	}
	
	this.getComponentValue = function() {
		return null;
	}
	
	this.setComponentValue = function() {
		
	}
	
	constructor(this);
}