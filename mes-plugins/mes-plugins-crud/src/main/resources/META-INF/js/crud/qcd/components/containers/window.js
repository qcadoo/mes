/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Window = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	
	function constructor(_this) {
		
		var childrenElement = $("#"+_this.elementSearchName+"_windowComponents");
		_this.constructChildren(childrenElement.children());
		
		mainController.setWindowHeaderComponent(_this);
		
		if (_this.options.ribbon) {
			ribbon = new QCD.components.Ribbon(_this.options.ribbon, _this.elementName, mainController);
			var ribbonElement = ribbon.constructElement();
			var ribbonDiv = $("#"+_this.elementPath+"_windowContainerRibbon");
			ribbonDiv.append(ribbonElement);
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
	
	this.updateSize = function(_width, _height) {
		
		var isMinWidth = ! mainController.isPopup();
		
		var childrenElement = $("#"+this.elementSearchName+"_windowContent");
		
		var margin = Math.round(_width * 0.02);
		if (margin < 20 && isMinWidth) {
			margin = 20;
		}
		width = Math.round(_width - 2 * margin);
		
		if (width < 960 && isMinWidth) {
			width = 960;
			childrenElement.css("marginLeft", margin+"px");
			childrenElement.css("marginRight", margin+"px");
		} else {
			childrenElement.css("marginLeft", "auto");
			childrenElement.css("marginRight", "auto");
		}
		childrenElement.width(width);
		childrenElement.css("marginTop", margin+"px");
		if (! this.options.fixedHeight) {
			childrenElement.css("marginBottom", margin+"px");
		}
		
		height = null;
		if (this.options.fixedHeight) {
			var containerHeight = Math.round(_height - 2 * margin - 70);
			height = containerHeight;
			if (this.options.header) {
				height -= 34;
			}
			childrenElement.height(containerHeight);
		}
		
		for (var i in this.components) {
			this.components[i].updateSize(width, height);
		}
		
		var innerWidth = $("#"+this.elementSearchName+"_windowContainerContentBodyWidthMarker").innerWidth();
		if (ribbon) {
			ribbon.updateSize(margin, innerWidth);
		}
	}
	
	this.setHeader = function(header) {
		var headerElement = $("#"+this.elementPath+"_windowHeader");
		if (headerElement) {
			headerElement.html(header);
		}
	}
	
	this.performBack = function(actionsPerformer) {
		mainController.goBack();
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.performCloseWindow = function(actionsPerformer) {
		mainController.closeWindow();
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	constructor(this);
}