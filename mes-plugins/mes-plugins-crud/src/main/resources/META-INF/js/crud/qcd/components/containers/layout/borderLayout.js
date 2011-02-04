var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.BorderLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

	
	function constructor(_this) {
		_this.constructChildren(_this.getLayoutChildren());
	}
	
	this.getLayoutChildren = function() {
		return $("#"+this.elementSearchName+"_layoutComponents").children();
	}
	
	this.updateSize = function(_width, _height) {
		for (var i in this.components) {
			this.components[i].updateSize(_width-20, _height-20);
		}
	}
	
	this.setBackground = function(color) {
		$("#"+this.elementSearchName+"_layoutComponents").css("backgroundColor", color);
		var label = $("#"+this.elementSearchName+"_layoutComponents > .borderLayoutLabel"); 
		label.css("backgroundColor", color);
		if (color == QCD.components.containers.layout.Layout.COLOR_NORMAL) {
			label.css("borderTop", "none");
			label.css("borderLeft", "none");
			label.css("borderRight", "none");
		} else {
			label.css("borderTop", "solid #A7A7A7 1px");
			label.css("borderLeft", "solid #A7A7A7 1px");
			label.css("borderRight", "solid #A7A7A7 1px");
		}
	}
	
	constructor(this);
}