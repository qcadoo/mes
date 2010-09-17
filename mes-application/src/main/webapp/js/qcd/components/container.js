var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Container = function(_element, _mainController, childrenElements) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	
	var options;
	
	this.constructChildren = function(childrenElements) {
		var components = QCDPageConstructor.getChildrenComponents(childrenElements, mainController);
		this.components = components;
	}
	
	function constructor(_this) {
		//QCDLogger.info("Container");
	}
	
	constructor(this);
	
}