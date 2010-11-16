/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.TextInput = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var textRepresentation = $("#" + _element.attr('id') + "_text");
	
	var input = this.input;
	
	this.getComponentData = function() {
		return {
			value : input.val()
		}
	}
	
	this.setComponentData = function(data) {
		if (data.value != undefined && data.value != null) {
			input.val(data.value);
			textRepresentation.html(data.value);
		}
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if(this.options.textRepresentationOnDisabled) {
			if(isEnabled) {
				input.show();
				textRepresentation.hide();
			} else {
				input.hide();
				textRepresentation.show();
			}
		}
	}
}