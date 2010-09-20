var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Window = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	function constructor(_this) {
		var childrenElement = $("#"+elementName+" .windowComponents");
		_this.constructChildren(childrenElement.children());
	}
	
	this.getComponentValue = function() {
		return null;
	}
	
	constructor(this);
}