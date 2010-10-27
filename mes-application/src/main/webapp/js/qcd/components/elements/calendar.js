var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Calendar = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var calendar = $("#"+this.elementPath+"_calendar");
	
	var input = this.input;
	
	var datepicker;
	
	var constructor = function(_this) {
		datepicker = input.datepicker({
			changeMonth: true,
			changeYear: true,
			showOn: 'button',
			dateFormat: 'yyyy-mm-dd',
		});
		
		calendar.click(function() {
			if(calendar.hasClass("enabled")) {
				input.datepicker("show");
			}
		})
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			calendar.addClass("enabled");
			input.datepicker("enable");
		} else {
			calendar.removeClass("enabled");
			input.datepicker("disable")
		}
	}

	constructor(this);
}