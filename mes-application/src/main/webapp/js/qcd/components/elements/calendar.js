var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Calendar = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var openCalendarButtonElement = $(".datepick-trigger");
		
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			openCalendarButtonElement.removeClass("disabled");
		} else {
			openCalendarButtonElement.addClass("disabled");
		}
	}	
}