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
//		$("#"+elementName+"_backButton").click(performBack);
		if (_this.options.ribbon) {
			
			var ribbon = new QCD.components.Ribbon(_this.options.ribbon, mainController);
			
			var ribbonElement = ribbon.constructElement();
			
			var ribbonDiv = $("#"+_this.elementPath+"_windowContainerRibbon");
			ribbonDiv.append(ribbonElement);
			
			
		}
	}
	
//	function performBack() {
//		mainController.goBack();
//	}
	
	this.getComponentValue = function() {
		return null;
	}
	this.setComponentValue = function() {
	}
	
	this.setComponentEnabled = function(isEnabled) {
		
	}
	
	this.performBack = function(actionsPerformer) {
		mainController.goBack();
		actionsPerformer.performNext();
	}
	
	this.performCancel = function(actionsPerformer) {
		mainController.performCancel(actionsPerformer);
	}
	
	constructor(this);
}