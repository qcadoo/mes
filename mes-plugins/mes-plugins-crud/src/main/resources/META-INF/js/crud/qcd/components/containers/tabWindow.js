
var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.TabWindow = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	
	var ribbon;
	var ribbonLeftElement
	var tabsLeftElement;
	var ribbonMainElement;
	var tabRibbonDiv;
	var tabsRightElement;
	var ribbonShadowElement;
	
	this.element.css("height","100%");
	
	var currentWidth;
	var currentHeight;
	
	var tabs;
	var tabHeaders = new Object();
	
	var oneTab = this.options.oneTab;
	
	var currentTabName;
	
	var innerWidthMarker = $("#"+this.elementSearchName+"_windowContainerContentBodyWidthMarker");
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementSearchName+"_windowComponents");
		_this.constructChildren(childrenElement.children());
		
		mainController.setWindowHeaderComponent(_this);
		
		tabs =  _this.getChildren();
		var tabsElement = $("#"+_this.elementSearchName+"_windowTabs > div");
		for (var tabName in tabs) {
			var tabElement = $("<a href='#'>").html(_this.options.translations["tab."+tabName]).bind('click', {tabName: tabName}, function(e) {
				e.target.blur();
				showTab(e.data.tabName);
			});
			tabHeaders[tabName] = tabElement;
			tabsElement.append(tabElement);
		}
		
		if (_this.options.hasRibbon) {
		
			if (_this.options.ribbon) {
				ribbon = new QCD.components.Ribbon(_this.options.ribbon, _this.elementName, mainController, _this.options.translations);
			}
				
			element = $("<div>");
			
			var row3Element =  $("<div>").attr("id", "q_row3_out_container");
			element.append(row3Element);
			
			ribbonLeftElement = $("<div>").attr("id", "q_row3_out_left");
			row3Element.append(ribbonLeftElement);
			
			ribbonMainElement = $("<div>").attr("id", "q_row3_out_main");
			row3Element.append(ribbonMainElement);
			if (ribbon) {
				ribbonMainElement.append(ribbon.constructElementContent());
			}
			
			if (! oneTab) {
				tabsLeftElement = $("<div>").attr("id", "q_row3_out_tabs_left");
				tabsLeftElement.append($("<div>"));
				row3Element.append(tabsLeftElement);
				
				var tabsElement = $("<div>").attr("id", "q_row3_out_tabs");
				row3Element.append(tabsElement);
				
				tabsRightElement = $("<div>").attr("id", "q_row3_out_tabs_right");
				tabsRightElement.append($("<div>"));
				row3Element.append(tabsRightElement);
				
				tabRibbonDiv = tabsElement;
			}
			
			ribbonShadowElement = $("<div>").attr("id", "q_row4_out");
			element.append(ribbonShadowElement);
			
			var ribbonDiv = $("#"+_this.elementPath+"_windowContainerRibbon");
			ribbonDiv.append(element);
		} else {
			$("#"+_this.elementPath+"_windowContainerContentBody").css("top","5px");
		}
		
		if (_this.options.firstTabName) {
			showTab(_this.options.firstTabName);
		}
		
		if (_this.options.referenceName) {
			mainController.registerReferenceName(_this.options.referenceName, _this);
		}
	}
	
	function showTab(tabName) {
		if (currentTabName) {
			tabs[currentTabName].element.children().hide();
			tabHeaders[currentTabName].removeClass("activeTab");
		}
		currentTabName = tabName;
		if (! oneTab) {
			tabHeaders[tabName].addClass("activeTab");
		}
		tabs[tabName].element.children().show();
		
		if (tabRibbonDiv) {
			if (tabs[tabName].getRibbonElement) {
				if (tabs[tabName].getRibbonElement()) {
					tabRibbonDiv.empty();
					var tabRibbonElement = tabs[tabName].getRibbonElement();
					if (tabRibbonElement) {
						tabRibbonDiv.append(tabRibbonElement);
					}
					tabRibbonDiv.css("display", "inline-block");
					tabsLeftElement.css("display", "inline-block");
					tabsRightElement.css("display", "inline-block");
				} else {
					tabRibbonDiv.css("display", "none");
					tabsLeftElement.css("display", "none");
					tabsRightElement.css("display", "none");
				}
			}
		}
	}
	
	this.getComponentValue = function() {
		return {};
	}
	this.setComponentValue = function(value) {
		for (var tabName in tabs) {
			tabHeaders[tabName].removeClass("errorTab");
		}
		for (var i in value.errors) {
			tabHeaders[value.errors[i]].addClass("errorTab");
		}
	}
	
	this.setComponentState = function(state) {
	}
	
	this.setMessages = function(messages) {
	}
	
	this.setComponentEnabled = function(isEnabled) {
	}
	
	this.setComponentLoading = function() {
	}
	
	this.setHeader = function(header) {
		var headerElement = $("#"+this.elementPath+"_windowHeader");
		if (headerElement) {
			headerElement.html(header);
		}
	}
	
	this.updateSize = function(_width, _height) {
		currentWidth = _width;
		currentHeight = _height;
		
		var isMinWidth = ! mainController.isPopup();
		
		var childrenElement = $("#"+this.elementSearchName+"_windowContent");
		
		var margin = Math.round(_width * 0.02);
		if (margin < 20 && isMinWidth) {
			margin = 20;
		}
		var ribbonWidth = _width - margin;
		width = Math.round(_width - 2 * margin);
		
		var innerWidth = innerWidthMarker.innerWidth();
		if (innerWidth != $(window).width()) { // IS VERTICAL SCROLLBAR
			width -= 15;
		}
		
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
				//height -= 34;
				height -= 24;
			}
			childrenElement.height(containerHeight);
		}
		
		if (! oneTab) {
			//var componentsHeight = height ? height-30 : null;
			var componentsHeight = height ? height-35 : null;
			for (var i in this.components) {
				this.components[i].updateSize(width, componentsHeight);
			}
		} else {
			//var componentsHeight = height ? height-20 : null;
			//var componentsHeight = height ? height-18 : null;
			var componentsHeight = height;
			for (var i in this.components) {
				this.components[i].updateSize(width-20, componentsHeight);
			}
		}
		
		if (this.options.hasRibbon) {
			ribbonLeftElement.width(margin);
			ribbonShadowElement.width(innerWidth);
			if (tabRibbonDiv) {
				var tabRibbonWidth = width - ribbonMainElement.width() - 16; // TODO
				tabRibbonDiv.width(tabRibbonWidth);
			}
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
	
	this.performComponentScript = function() {
		if (ribbon) {
			ribbon.performScripts();
		}
	}
	
	this.getRibbonItem = function(ribbonItemPath) {
		return ribbon.getRibbonItem(ribbonItemPath);
	}
	
	constructor(this);
}
