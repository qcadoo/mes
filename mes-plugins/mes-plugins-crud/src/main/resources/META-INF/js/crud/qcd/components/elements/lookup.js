/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Lookup = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var elementPath = this.elementPath;
	var element = _element;
	
	var mainController = _mainController;
	
	var lookupWindow;
	
	var inputElement = this.input;
	var valueDivElement = $("#"+this.elementSearchName+"_valueDiv");
	var loadingElement = $("#"+this.elementSearchName+"_loadingDiv");
	var labelElement = $("#"+this.elementSearchName+"_labelDiv");
	var openLookupButtonElement = $("#"+this.elementSearchName+"_openLookupButton");
	
	var labelNormal = labelElement.html();
	var labelFocus = this.options.translations.labelOnFocus;
	
	var currentData = new Object();
	currentData.value = null;
	currentData.selectedEntityValue = null;
	currentData.selectedEntityCode = null;
	
	var isFocused = false;
	
	var currentValue;
	
	var listeners = this.options.listeners;
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	QCD.info(this.elementPath);
	QCD.info(this.options);
	
	var _this = this;
	
	var constructor = function(_this) {
		
		//var nameToTranslate = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".label.focus";
		//labelFocus = "<span class='focusedLabel'>"+mainController.getTranslation(nameToTranslate)+"</span>";
		labelFocus = "<span class='focusedLabel'>"+labelFocus+"</span>";
		
		openLookupButtonElement.click(openLookup);

//		var elementName = elementPath.replace(/-/g,".");
//		window[elementName+"_onReadyFunction"] = function() {
//			if (currentData.selectedEntityCode) {
//				lookupWindow.getComponent("mainWindow.lookupGrid").setFilterState("lookupCodeVisible", currentData.selectedEntityCode);	
//			}
//			lookupWindow.init();
//		}
//		window[elementName+"_onSelectFunction"] = function(entityId, entityString, entityCode) {
//			currentData.selectedEntityId = entityId;
//			currentData.selectedEntityValue = entityString;
//			currentData.selectedEntityCode = entityCode;
//			currentData.isError = false;
//			updateData();
//			if (hasListeners) {
//				mainController.getUpdate(elementPath, entityId, listeners);
//			}
//		}
		inputElement.focus(onInputFocus).blur(onInputBlur);
		inputElement.keypress(function(e) {
			var key=e.keyCode || e.which;
			if (key==13) {
				performSearch();
			}
		});
		valueDivElement.click(function() {
			if (openLookupButtonElement.hasClass("enabled")) {
				inputElement.focus();
			}
		});
	}
	
	this.setComponentData = function(data) {
		currentData.value = data.value;
		currentData.selectedEntityValue = data.selectedEntityValue;
		currentData.selectedEntityCode = data.selectedEntityCode;
//		currentData.contextEntityId = data.contextEntityId;
		if (currentData.value == null && currentData.selectedEntityCode != null && $.trim(currentData.selectedEntityCode) != "") {
			currentData.isError = true;
		} else {
			currentData.isError = false;
		}
		updateData();
	}
	
	this.getComponentData = function() {
		return currentData;
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			openLookupButtonElement.addClass("enabled")
			document.getElementById(this.elementPath+"_valueDiv").removeAttribute("disabled");
		} else {
			openLookupButtonElement.removeClass("enabled")
			document.getElementById(this.elementPath+"_valueDiv").setAttribute("disabled", true);
		}
	}
	
//	this.setCurrentValue = function(data) {
//		currentValue = currentData.selectedEntityCode;
//	} 
	
//	this.isChanged = function() {
//		return currentValue != currentData.selectedEntityCode;
//	}
	
	function updateData() {
		loadingElement.hide();
		if (! currentData.isError) {
			element.removeClass("error");
			valueDivElement.html(currentData.selectedEntityValue);
			if (currentData.selectedEntityCode) {
				valueDivElement.attr('title', currentData.selectedEntityValue);
				inputElement.attr('title', currentData.selectedEntityValue);
				if (! isFocused) {
					valueDivElement.show();
					inputElement.val("");
					labelElement.html(labelNormal);
				} else {
					inputElement.val(currentData.selectedEntityCode);
				}
			} else {
				valueDivElement.attr('title', "");
				inputElement.attr('title', "");
				inputElement.val("");
			}
		}
	}
	
	//TODO: mady 429
//	function setSelectionRange(input, selectionStart, selectionEnd) {
//		input = document.getElementsByTagName("input")[0];
//		input.value = input.value;
//		if (input.createTextRange) {
//			var range = input.createTextRange();
//			range.collapse(true);
//			range.moveEnd('character', selectionEnd);
//			range.moveStart('character', selectionStart);
//			range.select();
//		}
//	}

//	function setCaretToPos(input, pos) {
//		setSelectionRange(input, pos, pos);
//	}
	
	function onInputFocus() {
		isFocused = true;
		valueDivElement.hide();
		labelElement.html(labelFocus);
		if (currentData.selectedEntityCode) {
			inputElement.val(currentData.selectedEntityCode);
			inputElement.attr('title', currentData.selectedEntityCode);
		} else {
			
		}

		//TODO: mady 429
//		setCaretToPos(inputElement,2);
//		var input = $("input:first");
//		input.val(input.val());
//		inputElement.val(inputElement.val());
//		input.focus();
//		input.value = input.value;

	}
	
	function onInputBlur() {
		isFocused = false;
		performSearch();
	}
	
	function performSearch() {
		var newCode = $.trim(inputElement.val());
		if (newCode != currentData.selectedEntityCode) {
			currentData.selectedEntityCode = $.trim(inputElement.val());
			currentData.selectedEntityValue = null;
			currentData.value = null;
			if (currentData.selectedEntityCode == "") {
				if (hasListeners) {
					loadingElement.show();
					mainController.callEvent("search", elementPath, null, null, null);
				} else {
					currentData.isError = false;
					updateData();
					element.removeClass("error");
				}
			} else {
				loadingElement.show();
				mainController.callEvent("search", elementPath, null, null, null);
			}
		} else {
			updateData();
		}
	}
	
	
	function openLookup() {
		if (! openLookupButtonElement.hasClass("enabled")) {
			return;
		}
//		if (currentData.contextEntityId) {
//		location += "&entityId="+currentData.contextEntityId;
//	}		
		lookupWindow = mainController.openPopup(_this.options.viewName+".html", _this, "lookup");
	}
	
	this.onPopupInit = function() {
		var grid = lookupWindow.getComponent("window.grid");
		grid.setLinkListener(this);
		//grid.setFilterState("lookupCodeVisible", currentData.selectedEntityCode);	
		lookupWindow.init();
	}
	
	this.onPopupClose = function() {
		lookupWindow = null;
	}
	
	this.onGridLinkClicked = function(entityId) {
		var grid = lookupWindow.getComponent("window.grid");
		var lookupData = grid.getLookupData(entityId);
		QCD.info(lookupData);
		mainController.closePopup();
	}
	
	constructor(this);
}