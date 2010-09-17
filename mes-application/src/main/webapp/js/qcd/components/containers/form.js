var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	var mainController = _mainController;
	var element = _element;
	
	var elementName = element.attr('id');
	
	var options;
	
	var components;
	
//	this.insterData = function(data) {
//		QCDLogger.info(this.containerComponents);
//		for (var i in data) {
//			var component = this.containerComponents[i];
//			QCDLogger.info(component);
//			component.insterData(data[i]);
//		}
//	}
//	
//	function performCancel() {
//		mainController.goBack();
//	}
	
	function constructor(_this) {
		options = QCDOptions.getElementOptions(elementName);
		var childrenElement = $("#"+elementName+" .formComponents");
		components = QCDPageConstructor.getChildrenComponents(childrenElement.children(), mainController);
		_this.components = components;
		//$("#"+_this.elementFullName+"_cancelButton").click(performCancel);
	}
	
	constructor(this);
}