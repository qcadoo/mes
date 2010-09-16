var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(element, args, _mainController, _containerComponents) {
	//QCDLogger.info("init form");
	var mainController = _mainController;
	this.containerComponents = _containerComponents;
	
	this.elementFullName = args.elementFullName;
	this.elementName = args.elementName;
	
	
	
	this.insterData = function(data) {
		QCDLogger.info(this.containerComponents);
		for (var i in data) {
			var component = this.containerComponents[i];
			QCDLogger.info(component);
			component.insterData(data[i]);
		}
	}
	
	function performCancel() {
		mainController.goBack();
	}
	
	function constructor(_this) {
		$("#"+_this.elementFullName+"_cancelButton").click(performCancel);
	}
	
	constructor(this);
}