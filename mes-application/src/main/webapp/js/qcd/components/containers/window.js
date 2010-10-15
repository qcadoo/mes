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
		if (_this.options.ribbon) {
			var ribbon = new QCD.components.Ribbon(_this.options.ribbon, mainController);
			
			var ribbonElement = ribbon.constructElement();
			
			var ribbonDiv = $("#"+_this.elementPath+"_windowContainerRibbon");
			ribbonDiv.append(ribbonElement);
		}
	}
	
	this.getComponentValue = function() {
		return null;
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentState = function(state) {
	}
	
	this.setMessages = function(messages) {
		QCD.info(messages);
	}
	
	this.setComponentEnabled = function(isEnabled) {
		
	}
	
	this.setComponentLoading = function() {
		
	}
	
	this.updateSize = function(_width, _height) {
		var childrenElement = $("#"+elementName+" .windowComponents");
		
		var margin = Math.round(_width * 0.02);
		//QCD.info(margin);
		
		width = Math.round(_width - 2 * margin);
		childrenElement.width(width);
		childrenElement.css("margin-top", margin+"px");
		childrenElement.css("margin-bottom", margin+"px");
		height = null;
		if (this.options.fixedHeight) {
			height = Math.round(_height - 2 * margin - 80);
			QCD.info(_height + " - " + height + " - " + margin);
			childrenElement.height(height);
		}
		for (var i in this.components) {
			this.components[i].updateSize(width, height);
		}
	}
	
	this.performBack = function(actionsPerformer) {
		mainController.goBack();
		actionsPerformer.performNext();
	}
	
	this.performCancel = function(actionsPerformer) {
		mainController.performCancel(actionsPerformer);
	}
	this.performNew = function(actionsPerformer) {
		mainController.performNew(actionsPerformer);
	}
	
	constructor(this);
}