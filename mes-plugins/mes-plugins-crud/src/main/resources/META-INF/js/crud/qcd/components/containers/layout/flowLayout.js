var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.FlowLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

	function constructor(_this) {
		_this.constructChildren(_this.getLayoutChildren());
	}
	
	this.getLayoutChildren = function() {
		return $("#"+this.elementSearchName+"_layoutComponents").children();
	}
	
	this.updateSize = function(_width, _height) {
		for (var i in this.components) {
			this.components[i].updateSize(_width, _height);
		}
	}
	
	constructor(this);
}