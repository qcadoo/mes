/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.EntityComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var mainController = _mainController;
	var options = this.options;
	var elementPath = this.elementPath;
	var input = this.input;
	
	function constructor(_this) {
		_this.input.change(onChange);
	}

	function onChange() {
		if (options.listeners.length > 0) {
			mainController.getUpdate(elementPath, input.val(), options.listeners);
		}
	}
	
	this.getComponentData = function() {
		var selected = this.input.val();
		if (!selected || $.trim(selected) == "") {
			selected = null;
		}
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		var previousSelected = this.input.val();
		
		if(data.values != null) {
			this.input.children().remove();
			var blankValue = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".blankValue";
			this.input.append("<option value=''>"+mainController.getTranslation(blankValue)+"</option>");
			for (var i in data.values) {
				var value = data.values[i];
				this.input.append("<option value='"+i+"'>"+value+"</option>");
			}
		}
		
		selected = data.value;
		
		if (selected != null) {
			this.input.val(selected);
		} else {
			this.input.val(previousSelected);
		}
	}
	
	constructor(this);
}