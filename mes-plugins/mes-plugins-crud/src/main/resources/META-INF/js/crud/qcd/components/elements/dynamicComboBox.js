/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.DynamicComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var mainController = _mainController;
	var elementPath = this.elementPath;
	var selectedValue = null;

	this.getComponentData = function() {
		var selected = this.input.val();
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		if (data == null) {
			return;
		}
		var previousSelected = this.input.val();
		this.input.children().remove();
		var blankValue = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".blankValue";
		this.input.append("<option value=''>"+mainController.getTranslation(blankValue)+"</option>");
		for (var i in data.values) {
			var value = data.values[i];
			this.input.append("<option value='"+value.key+"'>"+value.value+"</option>");
		}
		selected = data.value;
		
		if (!selected || $.trim(selected) == "") {
			this.input.val(previousSelected);
		} else {
			this.input.val(selected);
		}
	}
	
	this.setComponentState = function(state) {
		selectedValue = state.value;
	}
	
}