var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Container = function(_element, _mainController, childrenElements) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	var mainController = _mainController;

	var components;
	
	this.constructChildren = function(childrenElements) {
		components = QCDPageConstructor.getChildrenComponents(childrenElements, mainController);
		this.components = components;
	}
	
	this.insterData = function(data) {
		for (var i in data) {
			var component = this.components[i];
			component.insterData(data[i]);
		}
	}
	
	this.getComponentsValue = function() {
		var values = null;
		for (var i in components) {
			var value = components[i].getValue();
			if (value != null) {
				if (values == null) {
					values = new Object();
				}
				values[i] = value;
			}
		}
		return values;
	}
	
	function constructor(_this) {
		//QCDLogger.info("Container");
	}
	
	constructor(this);
	
}