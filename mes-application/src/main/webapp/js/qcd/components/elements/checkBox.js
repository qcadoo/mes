var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.CheckBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var mainController = _mainController;
	var textRepresentation = $("#" + _element.attr('id') + "_text");
	var currentValue;
	
	this.getComponentData = function() {
		if (this.input.attr('checked')) {
			return { value: "1" };
		}
		return { value: "0" };
	}
	
	this.setComponentData = function(data) {
		if (data != null && data.value == 1) {
			this.input.attr('checked', true);
			textRepresentation.html(mainController.getTranslation('commons.true'));
		} else {
			this.input.attr('checked', false);
			textRepresentation.html(mainController.getTranslation('commons.false'));
		}
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if(this.options.textRepresentationOnDisabled) {
			if(isEnabled) {
				this.input.show();
				textRepresentation.hide();
			} else {
				this.input.hide();
				textRepresentation.show();
			}
		}
	}
	
	this.setSelected = function(actionsPerformer, isSelected) {
		this.input.attr('checked', isSelected);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.setCurrentValue = function(data) {
		currentValue = this.input.attr('checked');
	} 
	
	this.isChanged = function() {
		return currentValue != this.input.attr('checked');
	}

}