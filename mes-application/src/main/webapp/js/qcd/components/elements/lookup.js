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
	var labelFocus = "<span class='focusedLabel'>enter code:</span>";
	
	var currentData = new Object();
	
	var isFocused = false;
	
	var listeners = this.options.listeners;
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	var constructor = function(_this) {
		
		var nameToTranslate = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".label.focus";
		labelFocus = "<span class='focusedLabel'>"+mainController.getTranslation(nameToTranslate)+"</span>";
		
		openLookupButtonElement.click(openLookup);
		$(window.document).focus(onWindowClick);
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
				// TODO mina
			}
		});
		valueDivElement.click(function() {
			inputElement.focus();
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
	
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			openLookupButtonElement.addClass("enabled")
		} else {
			openLookupButtonElement.removeClass("enabled")
		}
	}
	
	function updateData() {
		loadingElement.hide();
		if (! currentData.isError) {
			valueDivElement.html(currentData.selectedEntityValue);
			if (! isFocused) {
				valueDivElement.show();
				inputElement.val("");
				labelElement.html(labelNormal);
			} else {
				inputElement.val(currentData.selectedEntityCode);
			}
		}
	}
	
	function onInputFocus() {
		isFocused = true;
		valueDivElement.hide();
		inputElement.val(currentData.selectedEntityCode);
		labelElement.html(labelFocus);
	}
	
	function onInputBlur() {
		isFocused = false;
		var newCode = $.trim(inputElement.val());
		if (newCode != currentData.selectedEntityCode) {
			QCD.info("lalala");
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
		lookupWindow = window.open(location, 'lookup', 'width=800,height=700');
	}
	
	function closeLookup() {
		if (lookupWindow) {
			lookupWindow.close();
			lookupWindow = null;
		}
	}
	
	constructor(this);
}