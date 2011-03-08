
var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.WindowTab = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	
	var ribbon;
	var ribbonElement;
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementSearchName+" > div");
		_this.constructChildren(childrenElement.children());
		
		if (_this.options.ribbon) {
			ribbon = new QCD.components.Ribbon(_this.options.ribbon, _this.elementName, mainController, _this.options.translations);
			ribbonElement = ribbon.constructElementContent();
			//var ribbonDiv = $("#"+_this.elementPath+"_windowContainerRibbon");
			//ribbonDiv.append(ribbonElement);
		}
		
		if (_this.options.referenceName) {
			mainController.registerReferenceName(_this.options.referenceName, _this);
		}
	}
	
	this.getRibbonElement = function() {
		return ribbonElement;
	}
	
	this.getRibbonItem = function(ribbonItemPath) {
		return ribbon.getRibbonItem(ribbonItemPath);
	}
	
	this.performComponentScript = function() {
		if (ribbon) {
			ribbon.performScripts();
		}
	}
	
	this.getComponentValue = function() {
		return {};
	}
	this.setComponentValue = function(value) {
	}
	
	this.setComponentState = function(state) {
	}
	
	this.setMessages = function(messages) {
	}
	
	this.setComponentEnabled = function(isEnabled) {
	}
	
	this.setComponentLoading = function() {
	}
	
	this.setVisible = function(isVisible) {
		// do nothing
	}
	
	this.updateSize = function(_width, _height) {
		var componentsHeight = _height ? _height-20 : null;
		for (var i in this.components) {
			this.components[i].updateSize(_width-20, componentsHeight);
		}
	}
	
	constructor(this);
}
