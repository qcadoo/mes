var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.GridLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));
	
	this.getLayoutChildren = function() {
		//return $("#"+_this.elementSearchName+"_layoutComponents");
		return new Array();
	}
	
}