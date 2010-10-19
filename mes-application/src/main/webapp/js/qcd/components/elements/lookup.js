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
	
	var labelNormal = labelElement.html();
	var labelFocus = "<span style='color: #005ec5;'>Podaj kod:</span>";
	
	var currentData = new Object();
	
	var listeners = this.options.listeners;
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	constructor = function(_this) {
		$("#"+_this.elementPath+"_openLookupButton").click(openLookup);
		$(window.document).focus(onWindowClick);
		var elementName = elementPath.replace(/-/g,".");
		window[elementName+"_onReadyFunction"] = function() {
			lookupWindow.init();
		}
		window[elementName+"_onSelectFunction"] = function(entityId, entityString, entityCode) {
			currentData.selectedEntityId = entityId;
			currentData.selectedEntityValue = entityString;
			currentData.selectedEntityCode = entityCode;
			updateData();
			element.removeClass("error");
			if (hasListeners) {
				mainController.getUpdate(elementPath, entityId, listeners);
			}
		}
		inputElement.focus(onInputFocus).blur(onInputBlur);
		valueDivElement.click(function() {
			inputElement.focus();
		});
	}
	
	this.setComponentData = function(data) {
		currentData.selectedEntityId = data.selectedEntityId;
		currentData.selectedEntityValue = data.selectedEntityValue;
		currentData.selectedEntityCode = data.selectedEntityCode;
		currentData.contextEntityId = data.contextEntityId;
		updateData();
		loadingElement.hide();
	}
	
	this.getComponentData = function() {
		return currentData;
	}
	
	function updateData() {
		//if (currentData.selectedEntityValue && currentData.selectedEntityValue != "") {
		if (currentData.selectedEntityId == null && currentData.selectedEntityCode != null) {
			
		} else {
			valueDivElement.html(currentData.selectedEntityValue);	
			valueDivElement.show();
			inputElement.val("");
			labelElement.html(labelNormal);
		}
		//}
	}
	
	function onInputFocus() {
		valueDivElement.hide();
		inputElement.val(currentData.selectedEntityCode);
		labelElement.html(labelFocus);
	}
	
	function onInputBlur() {
		var newCode = inputElement.val().trim();
		if (newCode != currentData.selectedEntityCode) {
			currentData.selectedEntityCode = inputElement.val().trim();
			currentData.selectedEntityValue = null;
			currentData.selectedEntityId = null;
			if (currentData.selectedEntityCode == "") {
				updateData();
				element.removeClass("error");
				labelElement.html(labelNormal);
			} else {
				loadingElement.show();
				mainController.getUpdate(elementPath, entityId, listeners);
			}
		} else {
			updateData();
			labelElement.html(labelNormal);
		}
	}
	
	function onWindowClick() {
		closeLookup();
	}
	
	function openLookup() {
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