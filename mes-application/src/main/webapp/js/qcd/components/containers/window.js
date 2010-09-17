var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Window = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	var options;
	
	var components;
	
	//this.elementFullName = args.elementFullName;
	//this.elementName = args.elementName;
	
	//this.biuldComponent = function() {
		
	//}
	
	function constructor(_this) {
		options = QCDOptions.getElementOptions(elementName);
		var childrenElement = $("#"+elementName+" .windowComponents");
		components = QCDPageConstructor.getChildrenComponents(childrenElement.children(), mainController);
		_this.components = components;
	}
	
	constructor(this);
}