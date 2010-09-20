var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
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
		var childrenElement = $("#"+elementName+" .formComponents");
		_this.constructChildren(childrenElement.children());
		//$("#"+_this.elementFullName+"_cancelButton").click(performCancel);
	}
	
	this.getComponentValue = function() {
		return null;
	}
	
	this.setComponentValue = function() {
		
	}
	
	constructor(this);
}