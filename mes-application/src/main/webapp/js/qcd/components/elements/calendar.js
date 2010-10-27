var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Calendar = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var calendar = $("#"+this.elementPath+"_calendar");
	
	var input = this.input;
	
	var datepicker;
	
	var opened = false;
	
	var constructor = function(_this) {
		options = $.datepicker.regional[locale];
		
		if(!options) {
			options = $.datepicker.regional[''];
		}
		
		options.changeMonth = true;
		options.changeYear = true;
		options.showOn = 'button';
		options.dateFormat = 'yy-mm-dd';
		options.showAnim = 'show';
		options.onClose = function() {
			opened = false;
		}
		
		input.datepicker(options);
		
		calendar.click(function() {
			if(calendar.hasClass("enabled")) {
				if(!opened) {
					input.datepicker("show");
					opened = true;
				} else {
					input.datepicker("hide");
					opened = false;
				}
			}
		});
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