var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Lookup = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var elementPath = this.elementPath;
	
	var mainController = _mainController;
	
	var lookupWindow;
	
	var inputElement = this.input;
	
	var currentData = new Object();
	
	constructor = function(_this) {
		$("#"+_this.elementPath+"_openLookupButton").click(openLookup);
		$(window.document).focus(onWindowClick);
		var elementName = elementPath.replace(/-/g,".");
		window[elementName+"_onReadyFunction"] = function() {
			lookupWindow.init();
		}
		window[elementName+"_onSelectFunction"] = function(entityId, entityString) {
			currentData.selectedEntityId = entityId;
			currentData.selectedEntityValue = entityString;
			updateData();
		}
	}
	
	this.setComponentData = function(data) {
		currentData = data;
		updateData();
	}
	
	this.getComponentData = function() {
		return currentData;
	}
	
	function updateData() {
		if (currentData.selectedEntityValue && currentData.selectedEntityValue != "") {
			inputElement.val(currentData.selectedEntityValue);	
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