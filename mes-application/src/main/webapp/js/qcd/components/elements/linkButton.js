var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.LinkButton = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;
	
	var element = _element;
	
	var button = $("#"+element.attr('id')+"_button");
	
	this.getComponentValue = function() {
		return null;
	}
	
	this.setComponentValue = function(value) {
		button.click(function() {
			mainController.goToPage(value);
		});
	}
	
	this.setComponentState = function(state) {
		insertValue(state);
	}
	
	function insertValue(value) {
		button.click(function() {
			mainController.goToPage(value);
		});
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			button.removeAttr('disabled');
		} else {
			button.attr('disabled', 'true');
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {

	}
	
	function constructor(_this) {
		button.click(function() {
			mainController.goToPage(_this.options.pageUrl);
		});
	}
	
	constructor(this);
}