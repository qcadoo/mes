var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.GridLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));
	
	var elementSearchName = this.elementSearchName;
	var rootElement = $("#"+elementSearchName+"_layoutComponents > table > tbody");
	
	var colsNumber = this.options.colsNumber;
	
	var fixedRowHeight = this.options.fixedRowHeight;
	
	function constructor(_this) {
		_this.constructChildren(getLayoutChildren());
	}
	
	function getLayoutChildren() {
		var components = rootElement.children().children().children();
		return components;
	}
	
	this.updateSize = function(_width, _height) {
		QCD.info(this.elementPath + " - " + _height + ' ' + fixedRowHeight);
		var baseWidth = _width/colsNumber;
		var baseHeight = 50;
		
		var tdElements = rootElement.children().children();
		
		for (var i=0; i<tdElements.length; i++) {
			var tdElement = $(tdElements[i]);
			var colspan = tdElement.attr("colspan") ? tdElement.attr("colspan") : 1;
			var elementWidth = baseWidth * colspan;
			tdElement.width(elementWidth);
		}
		
		for (var i in this.components) {
			var tdElement = this.components[i].element.parent();
			var rowspan = tdElement.attr("rowspan") ? tdElement.attr("rowspan") : 1;
			var colspan = tdElement.attr("colspan") ? tdElement.attr("colspan") : 1;
			
			var elementWidth = baseWidth * colspan;
			
			if (fixedRowHeight) {
				var elementHeight = baseHeight * rowspan;
				this.components[i].updateSize(elementWidth, elementHeight);
			} else {
				this.components[i].updateSize(elementWidth);
			}
		}
	}
	
	constructor(this);
}