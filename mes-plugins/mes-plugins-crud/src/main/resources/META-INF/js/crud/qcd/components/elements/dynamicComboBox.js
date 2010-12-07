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
	var stateSelectedValue = null;
	
	var input = this.input;

	this.getComponentData = function() {
		var selected = this.input.val();
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		setData(data);
	}
	
	function setData(data) {
		if (data == null) {
			return;
		}
		if (data.value && ! data.values) { // is setState
			stateSelectedValue = data.value;
			return;
		}
		var previousSelected = input.val();
		input.children().remove();
		for (var i in data.values) {
			var value = data.values[i];
			input.append("<option value='"+value.key+"'>"+value.value+"</option>");
		}
		
		if (stateSelectedValue) {
			selected = stateSelectedValue;
		} else {
			selected = data.value;
		}
		
		if (!selected || $.trim(selected) == "") {
			input.val(previousSelected);
		} else {
			input.val(selected);
		}
	}
}
