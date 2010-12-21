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
	
	var translations = this.options.translations;
	
	var mainController = _mainController;
	
	var lookupWindow;
	
	var inputElement = this.input;
	var loadingElement = $("#"+this.elementSearchName+"_loadingDiv");
	var labelElement = $("#"+this.elementSearchName+"_labelDiv");
	var openLookupButtonElement = $("#"+this.elementSearchName+"_openLookupButton");
	var lookupDropdownElement = $("#"+this.elementSearchName+"_lookupDropdown");
	var lookupDropdown = new QCD.components.elements.lookup.Dropdown(lookupDropdownElement, this, translations);
	
	var labelNormal = labelElement.html();
	var labelFocus = "<span class='focusedLabel'>"+this.options.translations.labelOnFocus+"</span>";
	
	var currentData = new Object();
	currentData.value = null;
	currentData.selectedEntityValue = null;
	currentData.selectedEntityCode = null;
	currentData.currentCode = null;
	
	var autocompleteMatches
	var autocompleteCode;
	var autocompleteEntitiesNumber;
	
	var isFocused = false;
	
	var baseValue;
	
	var listeners = this.options.listeners;
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	var _this = this;
	
	var isDropdownVisible = false;
	
	var autocompleteRefreshTimeout = null;
	
	var shouldUpdateDataAfterSearch = false;
	
	
	
	var _this = this;
	
	var constructor = function(_this) {
		openLookupButtonElement.click(openLookup);
		inputElement.focus(onInputFocus).blur(onInputBlur);
		
		inputElement.keyup(function(e) {
			var key = e.keyCode || e.which;
			
			if (key == 40) { // down
				if (! lookupDropdown.isOpen()) {
					onInputValueChange(true);
				}
				isDropdownVisible = true;
				lookupDropdown.selectNext();
				return;
			}
			if (key == 38) { // up
				if (! lookupDropdown.isOpen()) {
					onInputValueChange(true);
				}
				isDropdownVisible = true;
				lookupDropdown.selectPrevious();
				return;
			}
			if (key == 13) { // enter
				if (! lookupDropdown.isOpen()) {
					return;
				}
				var entity = lookupDropdown.getSelected();
				if (entity == null) {
					return;
				}
				performSelectEntity(entity);
				currentData.currentCode = currentData.selectedEntityCode;
				inputElement.val(currentData.selectedEntityCode);
				//updateView();
				isDropdownVisible = false;
				onInputValueChange(true);
				lookupDropdown.hide();
				return;
			}
			if (key == 27) { // escape
				lookupDropdown.hide();
				//QCD.info("escape");
				return;
			}
			isDropdownVisible = true;
			var inputVal = inputElement.val();
			
			if (currentData.currentCode != inputVal) {
				currentData.currentCode = inputVal;
				performSelectEntity(null);
				onInputValueChange();
			} 
		});
	}
	
	// prevent event propagation
	inputElement.keydown(function(e) {
		var key = e.keyCode || e.which;
		if (key == 38 || key == 27) { // up or escape
			e.preventDefault();
			e.stopImmediatePropagation();
			e.stopPropagation();
			return;
		}
	}).keypress(function(e) {
		var key = e.keyCode || e.which;
		if (key == 38 || key == 27) { // up or escape
			e.preventDefault();
			e.stopImmediatePropagation();
			e.stopPropagation();
			return;
		}
	});
	
	this.setComponentData = function(data) {
		performSelectEntity({
			id: data.value ? data.value : null,
			code: data.selectedEntityCode,
			value: data.selectedEntityValue
		}, false);
		currentData.contextEntityId = data.contextEntityId;
		
		
		if (data.autocompleteMatches) {
			autocompleteMatches = data.autocompleteMatches;
			autocompleteCode = data.autocompleteCode;
			autocompleteEntitiesNumber = data.autocompleteEntitiesNumber;
		} else {
			autocompleteMatches = null;
			autocompleteCode = null;
			autocompleteEntitiesNumber = null;
		}
		
		if (isFocused) {
			
			autocompleteCode = autocompleteCode ? autocompleteCode : "";
			if (autocompleteCode == currentData.currentCode) {
				loadingElement.hide();	
			}
			lookupDropdown.updateAutocomplete(autocompleteMatches, autocompleteEntitiesNumber);
			if (isDropdownVisible) {
				lookupDropdown.show();
			}
			
		} else {
			inputElement.val(currentData.selectedEntityValue);
			loadingElement.hide();
		}
		
		//updateView();
		
		if (shouldUpdateDataAfterSearch) {
			shouldUpdateDataAfterSearch = false;
			onInputBlur();
		}
	}
	
	this.getComponentData = function() {
		return currentData;
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			openLookupButtonElement.addClass("enabled")
		} else {
			openLookupButtonElement.removeClass("enabled")
		}
	}
	
	this.performUpdateState = function() {
		baseValue = new Object();
		baseValue.value = currentData.value;
		baseValue.selectedEntityCode = currentData.selectedEntityCode;
	}
	
	this.isComponentChanged = function() {
		return ! (currentData.selectedEntityCode == baseValue.selectedEntityCode);
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
	
	function onInputValueChange(immidiateRefresh) {
		//performSelectEntity(null);
		
		if (autocompleteRefreshTimeout) {
			window.clearTimeout(autocompleteRefreshTimeout);
			autocompleteRefreshTimeout = null;
		}
		if (immidiateRefresh) {
			loadingElement.show();
			mainController.callEvent("autompleteSearch", elementPath, null, null, null);	
		} else {
			autocompleteRefreshTimeout = window.setTimeout(function() {
				autocompleteRefreshTimeout = null;
				loadingElement.show();
				mainController.callEvent("autompleteSearch", elementPath, null, null, null);
			}, 200);	
		}
	}
	
	function onInputFocus() {
		isFocused = true;
		updateData();
		
		//TODO: mady 429
//		setCaretToPos(inputElement,2);
//		var input = $("input:first");
//		input.val(input.val());
//		inputElement.val(inputElement.val());
//		input.focus();
//		input.value = input.value;

	}
	
	function updateData() {
		if (isFocused) {
			openLookupButtonElement.addClass("lightHover");
			labelElement.html(labelFocus);
			
			if (currentData.selectedEntityCode && ! currentData.currentCode) {
				currentData.currentCode = currentData.selectedEntityCode
			}
			
			inputElement.val(currentData.currentCode);
			
			
			if (autocompleteCode == currentData.currentCode) {
				loadingElement.hide();	
			}
			lookupDropdown.updateAutocomplete(autocompleteMatches, autocompleteEntitiesNumber);
			if (isDropdownVisible) {
				lookupDropdown.show();
			}
			
		} else {
			inputElement.val(currentData.selectedEntityValue);
			loadingElement.hide();
		}
		
	}
	
	function performSelectEntity(entity, callEvent) {
		if (callEvent == undefined) {
			callEvent = true;
		}
		if (entity) {
			currentData.value = entity.id;
			currentData.selectedEntityCode = entity.code;
			currentData.selectedEntityValue = entity.value;	
		} else {
			currentData.value = null;
			currentData.selectedEntityCode = null;
			currentData.selectedEntityValue = null;
		}
		if (hasListeners && callEvent) {
			mainController.callEvent("onSelectedEntityChange", elementPath, null, null, null);
		}
	}
	
	function onInputBlur() {
		QCD.info("blur");
		isFocused = false;
		openLookupButtonElement.removeClass("lightHover");
		
		if (loadingElement.is(':visible') || autocompleteRefreshTimeout) {
			QCD.info("is searching");
			shouldUpdateDataAfterSearch = true;
			return;
		}
		
		QCD.info(currentData.value);
		
		if (!currentData.value) {
			if (autocompleteMatches) {
				if (autocompleteMatches.length == 0) {
					if (currentData.currentCode != "") {
						_this.addMessage({
							title: "",
							content: translations.noMatchError
						});
						element.addClass("error");
					} else {
						labelElement.html(labelNormal);
						performSelectEntity(null);
						inputElement.val("");
					}
				} else if (autocompleteMatches.length > 1) {
					
					var entity = lookupDropdown.getSelected();
					if (entity == null) {
						element.addClass("error");
						_this.addMessage({
							title: "",
							content: translations.moreTahnOneMatchError
						});
					} else {
						element.removeClass("error");
						performSelectEntity(entity);
						currentData.currentCode = currentData.selectedEntityCode;
						inputElement.val(currentData.selectedEntityCode);
						labelElement.html(labelNormal);
					}
				} else {
					element.removeClass("error");
					performSelectEntity(autocompleteMatches[0]);
					currentData.currentCode = currentData.selectedEntityCode;
					labelElement.html(labelNormal);
				}
			} else {
				_this.addMessage({
					title: "",
					content: translations.noMatchError
				});
				element.addClass("error");
			}
		} else {
			labelElement.html(labelNormal);
			element.removeClass("error");
		}
		lookupDropdown.hide();
		inputElement.val(currentData.selectedEntityValue);
	}
	
	function openLookup() {
		if (! openLookupButtonElement.hasClass("enabled")) {
			return;
		}
		var url = _this.options.viewName+".html";
		if (currentData.contextEntityId) {
			var params = new Object();
			params["window.grid.belongsToEntityId"] = currentData.contextEntityId;
			url += "?context="+JSON.stringify(params);
	}		
		lookupWindow = mainController.openPopup(url, _this, "lookup");
	}
	
	this.onPopupInit = function() {
		var grid = lookupWindow.getComponent("window.grid");
		grid.setLinkListener(this);
		if (currentData.selectedEntityCode) {
			grid.setFilterState("lookupCode", currentData.selectedEntityCode);	
		}
		lookupWindow.init();
	}
	
	this.onPopupClose = function() {
		lookupWindow = null;
	}
	
	this.onGridLinkClicked = function(entityId) {
		var grid = lookupWindow.getComponent("window.grid");
		var lookupData = grid.getLookupData(entityId);
		performSelectEntity({
			id: lookupData.entityId,
			code: lookupData.lookupCode,
			value: lookupData.lookupValue
		});
		currentData.isError = false;
		updateData();
//		if (hasListeners) {
//			mainController.callEvent("search", elementPath, null, null, null);
//		}
		mainController.closePopup();
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().parent().parent().height(height);
	}
	
	constructor(this);
}