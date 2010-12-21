var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Lookup = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var element = _element;
	var elementPath = this.elementPath;
	
	var translations = this.options.translations;
	
	var AUTOCOMPLETE_TIMEOUT = 200;
	
	var keyboard = {
		UP: 38,
		DOWN: 40,
		ENTER: 13,
		ESCAPE: 27
	};
	
	var elements = {
		input: this.input,
		loading: $("#"+this.elementSearchName+"_loadingDiv"),
		label: $("#"+this.elementSearchName+"_labelDiv"),
		openLookupButton: $("#"+this.elementSearchName+"_openLookupButton"),
		lookupDropdown: $("#"+this.elementSearchName+"_lookupDropdown")
	};
	
	var labels = {
		normal: elements.label.html(),
		focus: "<span class='focusedLabel'>"+this.options.translations.labelOnFocus+"</span>"
	};
	
	var viewState = {
		isFocused: false,
		error: null
	};
	
	var dataState = {
		currentCode: null,
		selectedEntity: {
			id: null,
			value: null,
			code: null
		},
		autocomplete: {
			matches: null,
			code: null,
			entitiesNumber: null
		},
		contextEntityId: null
	}
	
	var autocompleteRefreshTimeout = null;
	
	var blurAfterLoad = false;
	
	var lookupDropdown = new QCD.components.elements.lookup.Dropdown(elements.lookupDropdown, this, translations);
	
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	var _this = this;
	
	
	function constructor(_this) {
		elements.input.focus(function() {
			viewState.isFocused = true;
			onViewStateChange();
		}).blur(function() {
			viewState.isFocused = false;
			onViewStateChange();
		});
		
		elements.input.keyup(function(e) {
			var key = getKey(e);
			if (key == keyboard.UP) {
				if (! lookupDropdown.isOpen()) {
					onInputValueChange(true);
				}
				lookupDropdown.selectPrevious();
				
			} else if (key == keyboard.DOWN) {
				if (! lookupDropdown.isOpen()) {
					onInputValueChange(true);
				}
				lookupDropdown.selectNext();
				
			} else if (key == keyboard.ENTER) {
				if (! lookupDropdown.isOpen()) {
					return;
				}
				var entity = lookupDropdown.getSelected();
				if (entity == null) {
					return;
				}
				performSelectEntity(entity);
				dataState.currentCode = dataState.selectedEntity.code;
				elements.input.val(dataState.currentCode);
				lookupDropdown.hide();
				
			} else if (key == keyboard.ESCAPE) {
				preventEvent(e);
				elements.input.val(dataState.currentCode);
				lookupDropdown.hide();
			} else {
				var inputVal = elements.input.val();
				if (dataState.currentCode != inputVal) {
					dataState.currentCode = inputVal;
					performSelectEntity(null);
					onInputValueChange();
				} 
			}
		});
		
		// prevent event propagation
		elements.input.keydown(function(e) {
			var key = getKey(e);
			if (key == keyboard.UP || key == keyboard.ESCAPE) {
				preventEvent(e);
				return false;
			}
		}).keypress(function(e) {
			var key = getKey(e);
			if (key == keyboard.UP || key == keyboard.ESCAPE) {
				preventEvent(e);
				return false;
			}
		});
	}
	
	
	
	this.getComponentData = function() {
		return {
			value: dataState.selectedEntity.id,
			selectedEntityValue: dataState.selectedEntity.value,
			selectedEntityCode: dataState.selectedEntity.code,
			currentCode: dataState.currentCode,
			autocompleteCode: dataState.autocomplete.code,
			contextEntityId: dataState.contextEntityId
		};
	}
	
	this.setComponentData = function(data) {
		dataState.currentCode = data.currentCode ? data.currentCode : dataState.currentCode;
		dataState.selectedEntity.id = data.value ? data.value : null;
		dataState.selectedEntity.value = data.selectedEntityValue;
		dataState.selectedEntity.code = data.selectedEntityCode;
		dataState.autocomplete.matches = data.autocompleteMatches ? data.autocompleteMatches : [];
		dataState.autocomplete.code = data.autocompleteCode ? data.autocompleteCode : "";
		dataState.autocomplete.entitiesNumber = data.autocompleteEntitiesNumber;
		if (dataState.contextEntityId != data.contextEntityId) {
			dataState.contextEntityId = data.contextEntityId;
			dataState.currentCode = "";
		}
		
		// initialaize current code on first load
		if (! dataState.currentCode) {
			dataState.currentCode = dataState.selectedEntity.id ? dataState.selectedEntity.code : "";
		}
		
		onDataStateChange();
	}
	
	function onViewStateChange() {
		if (viewState.isFocused) {
			elements.openLookupButton.addClass("lightHover");
			elements.label.html(labels.focus);
			elements.input.val(dataState.currentCode);
		} else {
			elements.openLookupButton.removeClass("lightHover");
			lookupDropdown.hide();
			
			if (autocompleteRefreshTimeout || elements.loading.is(':visible')) {
				blurAfterLoad = true;
				return;
			}
			
			viewState.error = null;
			if (! dataState.selectedEntity.id && ! lookupDropdown.getSelected() && dataState.autocomplete.matches && dataState.currentCode != "") {
				if (dataState.autocomplete.matches.length == 0) {
					viewState.error = translations.noMatchError;
				} else if (dataState.autocomplete.matches.length > 1) {
					viewState.error = translations.moreTahnOneMatchError;
				} else {
					performSelectEntity(dataState.autocomplete.matches[0]);
				}
			}
			
			if (viewState.error == null) {
				elements.label.html(labels.normal);
				if (dataState.selectedEntity.id) {
					elements.input.val(dataState.selectedEntity.value);	
				} else if (lookupDropdown.getSelected()) {
					performSelectEntity(lookupDropdown.getSelected());
					dataState.currentCode = lookupDropdown.getSelected().code;
					elements.input.val(dataState.selectedEntity.value);	
				}
			} else {
				_this.addMessage({
					title: "",
					content: viewState.error
				});
				element.addClass("error");
			}
		}
	}
	
	function onDataStateChange() {
		if (dataState.autocomplete.code == dataState.currentCode) {
			elements.loading.hide();	
		}
		if (blurAfterLoad) {
			blurAfterLoad = false;
			viewState.isFocused = false;
			lookupDropdown.updateAutocomplete(dataState.autocomplete.matches, dataState.autocomplete.entitiesNumber);
			onViewStateChange();
			return;
		}
		if (viewState.isFocused) {
			lookupDropdown.updateAutocomplete(dataState.autocomplete.matches, dataState.autocomplete.entitiesNumber);
			lookupDropdown.show();
		} else {
			elements.input.val(dataState.selectedEntity.value);
		}
	}
	
	function onInputValueChange(immidiateRefresh) {
		if (autocompleteRefreshTimeout) {
			window.clearTimeout(autocompleteRefreshTimeout);
			autocompleteRefreshTimeout = null;
		}
		if (immidiateRefresh) {
			elements.loading.show();
			mainController.callEvent("autompleteSearch", elementPath, null, null, null);	
		} else {
			autocompleteRefreshTimeout = window.setTimeout(function() {
				autocompleteRefreshTimeout = null;
				elements.loading.show();
				mainController.callEvent("autompleteSearch", elementPath, null, null, null);
			}, AUTOCOMPLETE_TIMEOUT);	
		}
	}
	
	function performSelectEntity(entity, callEvent) {
		if (callEvent == undefined) {
			callEvent = true;
		}
		if (entity) {
			dataState.selectedEntity.id = entity.id;
			dataState.selectedEntity.code = entity.code;
			dataState.selectedEntity.value = entity.value;	
		} else {
			dataState.selectedEntity.id = null;
			dataState.selectedEntity.code = null;
			dataState.selectedEntity.value = null;
		}
		if (hasListeners && callEvent) {
			mainController.callEvent("onSelectedEntityChange", elementPath, null, null, null);
		}
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().parent().parent().height(height);
	}
	
	function preventEvent(e) {
		e.preventDefault();
		e.stopImmediatePropagation();
		e.stopPropagation();
		e.keyCode = 0;
		e.which = 0;
		e.returnValue = false;
	}
	
	function getKey(e) {
		return e.keyCode || e.which;
	}
	
	constructor(this);
}