var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.CheckBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	this.getComponentData = function() {
		if (this.input.attr('checked')) {
			return { value: "1" };
		}
		return { value: "0" };
	}
	
	this.setComponentData = function(data) {
		if (data != null && data.value == 1) {
			this.input.attr('checked', true);
		} else {
			this.input.attr('checked', false);
		}
	}
	
	this.setSelected = function(actionsPerformer, isSelected) {
		this.input.attr('checked', isSelected);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}

}