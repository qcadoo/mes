var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Lookup = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var elementPath = this.elementPath;
	
	var mainController = _mainController;
	
	function openLookup() {
		var elementName = elementPath.replace(/-/g,".");
		window.open(mainController.getViewName()+".html?lookupComponent="+elementName, 'lookup', 'width=800,height=700');
	}
	
	constructor = function(_this) {
		$("#"+_this.elementPath+"_openLookupButton").click(openLookup)
	}
	
	constructor(this);
}