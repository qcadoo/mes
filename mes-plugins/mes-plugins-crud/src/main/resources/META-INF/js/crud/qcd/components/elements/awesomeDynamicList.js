
var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.AwesomeDynamicList = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var elementPath = this.elementPath;
	var elementSearchName = this.elementSearchName;
	
	var innerFormContainer;
	var awesomeDynamicListContent;
	
	var awesomeDynamicListHeader;
	var awesomeDynamicListHeaderObject;
	
	var formObjects;
	var formObjectsIndex = 1;
	
	var currentWidth;
	var currentHeight;
	
	var buttonsArray = new Array();
	
	var firstLine;
	
	var BUTTONS_WIDTH = 70;
	
	var hasButtons = this.options.hasButtons;
	
	var enabled = true;
	
	var isChanged = false;
	
	var components = new Object();
	
	function constructor(_this) {
		innerFormContainer = $("#"+_this.elementSearchName+" > .awesomeDynamicList > .awesomeDynamicListInnerForm").children();
		awesomeDynamicListContent = $("#"+_this.elementSearchName+" > .awesomeDynamicList > .awesomeDynamicListContent");
		awesomeDynamicListHeader = $("#"+_this.elementSearchName+" > .awesomeDynamicList > .awesomeDynamicListHeader");
		if (awesomeDynamicListHeader && awesomeDynamicListHeader.length > 0) {
			awesomeDynamicListHeaderObject = QCDPageConstructor.getChildrenComponents(awesomeDynamicListHeader.children(), mainController)["header"];
			awesomeDynamicListHeaderObject.setEnabled(true, true);
		}
		
		formObjects = new Array();
		formObjectsMap = new Object();
		if (!hasButtons) {
			BUTTONS_WIDTH = 0;
		}
		updateButtons();
	}
	
	this.getComponentValue = function() {
		var formValues = new Array();
		for (var i in formObjects) {
			if (! formObjects[i]) {
				continue;
			}
			formValues.push({
				name: formObjects[i].elementName,
				value: formObjects[i].getValue()
			});
		}
		return { 
			forms: formValues
		};
	}
	
	this.setComponentValue = function(value) {
		var forms = value.forms;
		if (forms) {
			formObjects = new Array();
			awesomeDynamicListContent.empty();
			this.components = new Object();
			components = this.components;
			formObjectsIndex = 1;
			for (var i in forms) {
				var formValue = forms[i];
				var formObject = getFormCopy(formObjectsIndex);
				formObject.setValue(formValue);
				formObjects[formObjectsIndex] = formObject;
				this.components[formObject.elementName] = formObject;
				formObjectsIndex++;
			}
			updateButtons();
		} else {
			var innerFormChanges = value.innerFormChanges;
			for (var i in innerFormChanges) {
				this.components[i].setValue(innerFormChanges[i]);
			}
		}
		mainController.updateSize();
	}
	
	this.setComponentState = function(state) {
		this.setComponentValue(state);
	}
	
	this.setComponentEnabled = function(isEnabled) {
		enabled = isEnabled;
		if (isEnabled) {
			for (var i in buttonsArray) {
				if (buttonsArray[i]) {
					buttonsArray[i].addClass("enabled");
				}
			}
		} else {
			for (var i in buttonsArray) {
				if (buttonsArray[i]) {
					buttonsArray[i].removeClass("enabled");
				}
			}
		}
	}
	
	this.isComponentChanged = function() {
		if (isChanged) {
			return true;
		}
		for (var i in formObjects) {
			if (formObjects[i] && formObjects[i].isChanged()) {
				return true;
			}
		}
		return false;
	}
	
	this.performUpdateState = function() {
		isChanged = false;
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
	}
	
	this.updateSize = function(_width, _height) {
		currentWidth = _width;
		currentHeight = _height;
		for (var i in formObjects) {
			if (formObjects[i]) {
				formObjects[i].updateSize(_width-BUTTONS_WIDTH, _height);
			}
		}
		if (awesomeDynamicListHeaderObject) {
			awesomeDynamicListHeader.width(_width-BUTTONS_WIDTH-20);
			awesomeDynamicListHeaderObject.updateSize(_width-BUTTONS_WIDTH-30, _height);
		}
	}
	
	function getFormCopy(formId) {
		var copy = innerFormContainer.clone();
		changeElementId(copy, formId);
		var line = $("<div>").addClass("awesomeListLine").attr("id", elementPath+"_line_"+formId);
		var formContainer = $("<div>").addClass("awesomeListFormContainer");
		formContainer.append(copy);
		line.append(formContainer);
		if (hasButtons) {
			var buttons = $("<div>").addClass("awesomeListButtons");
		
			buttonsArray.push(removeLineButton);
			var removeLineButton = $("<a>").addClass("awesomeListButton").addClass("awesomeListMinusButton").addClass("enabled").attr("id", elementPath+"_line_"+formId+"_removeButton");
			removeLineButton.css("display", "none");
			removeLineButton.click(function(e) {
				var button = $(e.target);
				if (button.hasClass("enabled")) {
					var lineId = button.attr("id").substring(elementPath.length+6, button.attr("id").length-13); 
					removeRowClicked(lineId);
				}
			});
			buttons.append(removeLineButton);
			var addLineButton = $("<a>").addClass("awesomeListButton").addClass("awesomeListPlusButton").addClass("enabled").attr("id", elementPath+"_line_"+formId+"_addButton");
			addLineButton.click(function(e) {
				var button = $(e.target);
				if (button.hasClass("enabled")) {
					var lineId = button.attr("id").substring(elementPath.length+6, button.attr("id").length-10); 
					addRowClicked(lineId);
				}
			});
			addLineButton.css("display", "none");
			buttonsArray.push(addLineButton);
			buttons.append(addLineButton);
			
			line.append(buttons);
		}
		awesomeDynamicListContent.append(line);
		var formObject = QCDPageConstructor.getChildrenComponents(copy, mainController)["innerForm_"+formId];
		formObject.updateSize(currentWidth-BUTTONS_WIDTH, currentHeight);
		formObject.setEnabled(true, true);
		
		var tabController = mainController.getTabController();
		tabController.getTabIndexInfo(copy);
		
		return formObject;
	}
	
	function addRowClicked(rowId) {
		var formObject = getFormCopy(formObjectsIndex);
		formObjects[formObjectsIndex] = formObject;
		components[formObject.elementName] = formObject;
		isChanged = true;
		formObjectsIndex++;
		updateButtons();
		mainController.updateSize();
	}
	
	function removeRowClicked(rowId) {
		var line = $("#"+elementSearchName+"_line_"+rowId);
		line.remove();
		isChanged = true;
		formObjects[rowId] = null;
		updateButtons();
		mainController.updateSize();
	}
	
	function updateButtons() {
		if (!hasButtons) {
			return;
		}
		var objectCounter = 0;
		var lastObject = 0;
		for (var i in formObjects) {
			if (formObjects[i]) {
				objectCounter++;
				lastObject = i;
			}
		}
		if (objectCounter  > 0) {
			if (firstLine) {
				firstLine.hide();
				firstLine = null;
			}
			for (var i in formObjects) {
				if (! formObjects[i]) {
					continue;
				}
				var line = $("#"+elementSearchName+"_line_"+i);
				var removeButton = $("#"+elementSearchName+"_line_"+i+"_removeButton");
				var addButton = $("#"+elementSearchName+"_line_"+i+"_addButton");
				removeButton.show();
				if (i == lastObject) {
					addButton.show();
					line.addClass("lastLine");
				} else {
					addButton.hide();
					line.removeClass("lastLine");
				}
			}
		} else {
			firstLine = $("<div>").addClass("awesomeListLine").addClass("lastLine").attr("id", elementPath+"_line_0");
			var buttons = $("<div>").addClass("awesomeListButtons");
			var addLineButton = $("<a>").addClass("awesomeListButton").addClass("awesomeListPlusButton").attr("id", elementPath+"_line_0_addButton");
			addLineButton.click(function(e) {
				var button = $(e.target);
				if (button.hasClass("enabled")) {
					var lineId = button.attr("id").substring(elementPath.length+6, button.attr("id").length-10); 
					addRowClicked(lineId);
				}
			});
			buttons.append(addLineButton);
			buttonsArray.push(addLineButton);
			if (enabled) {
				addLineButton.addClass("enabled");
			}
			firstLine.append(buttons);
			awesomeDynamicListContent.append(firstLine);
		}
	}
	
	
	
	function changeElementId(element, formId) {
		var id = element.attr("id");
		if (id) {
			element.attr("id",id.replace("@innerFormId", formId));
		}
		element.children().each(function(i,e) {
			var kid = $(e);
			changeElementId(kid, formId)
		});
	}
	
	constructor(this);
}