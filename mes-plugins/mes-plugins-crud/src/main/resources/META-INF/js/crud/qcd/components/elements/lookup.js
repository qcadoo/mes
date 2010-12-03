/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
	var valueDivElement = $("#"+this.elementPath+"_valueDiv");
	var loadingElement = $("#"+this.elementPath+"_loadingDiv");
	var labelElement = $("#"+this.elementPath+"_labelDiv");
	var openLookupButtonElement = $("#"+this.elementPath+"_openLookupButton");
	
	var labelNormal = labelElement.html();
	var labelFocus = "";
	
	var currentData = new Object();
	
	var isFocused = false;
	
	var currentValue;
	
	var listeners = this.options.listeners;
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	var constructor = function(_this) {
		
		var nameToTranslate = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".label.focus";
		labelFocus = "<span class='focusedLabel'>"+mainController.getTranslation(nameToTranslate)+"</span>";
		
		openLookupButtonElement.click(openLookup);
		if (window.parent) {
			$(window.parent).focus(onWindowClick);
		} else {
			$(window).focus(onWindowClick);
		}
		var elementName = elementPath.replace(/-/g,".");
		window[elementName+"_onReadyFunction"] = function() {
			if (currentData.selectedEntityCode) {
				lookupWindow.getComponent("mainWindow.lookupGrid").setFilterState("lookupCodeVisible", currentData.selectedEntityCode);	
			}
			lookupWindow.init();
		}
		window[elementName+"_onSelectFunction"] = function(entityId, entityString, entityCode) {
			currentData.selectedEntityId = entityId;
			currentData.selectedEntityValue = entityString;
			currentData.selectedEntityCode = entityCode;
			currentData.isError = false;
			updateData();
			if (hasListeners) {
				mainController.getUpdate(elementPath, entityId, listeners);
			}
		}
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
		currentData.selectedEntityId = data.selectedEntityId;
		currentData.selectedEntityValue = data.selectedEntityValue;
		currentData.selectedEntityCode = data.selectedEntityCode;
		currentData.contextEntityId = data.contextEntityId;
		if (currentData.selectedEntityId == null && currentData.selectedEntityCode != null && $.trim(currentData.selectedEntityCode) != "") {
			currentData.isError = true;
		} else {
			currentData.isError = false;
		}
		updateData();
	}
	
	this.getComponentData = function() {
		return currentData;
	}
	
	this.isChanged = function() {
		return false; // TODO
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
	
	this.setCurrentValue = function(data) {
		currentValue = currentData.selectedEntityCode;
	} 
	
	this.isChanged = function() {
		return currentValue != currentData.selectedEntityCode;
	}
	
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
			currentData.selectedEntityId = null;
			if (currentData.selectedEntityCode == "") {
				if (hasListeners) {
					loadingElement.show();
					mainController.getUpdate(elementPath, entityId, listeners);
				} else {
					currentData.isError = false;
					updateData();
					element.removeClass("error");
				}
			} else {
				loadingElement.show();
				mainController.getUpdate(elementPath, entityId, listeners);
			}
		} else {
			updateData();
		}
	}
	
	function onWindowClick() {
		closeLookup();
	}
	
	function openLookup() {
		if (! openLookupButtonElement.hasClass("enabled")) {
			return;
		}
		var elementName = elementPath.replace(/-/g,".");
		var location = mainController.getViewName()+".html?lookupComponent="+elementName;
		if (currentData.contextEntityId) {
			location += "&entityId="+currentData.contextEntityId;
		}
		var left = (screen.width/2)-(400);
		var top = (screen.height/2)-(350);
		lookupWindow = window.open(location, 'lookup', 'status=0,toolbar=0,width=800,height=700,left='+left+',top='+top);
		//lookupWindow.moveTo(50,50);
	}
	
	function closeLookup() {
		if (lookupWindow) {
			try {
				lookupWindow.close();
			} catch (e) {
			}
			lookupWindow = null;
		}
	}
	
	constructor(this);
}