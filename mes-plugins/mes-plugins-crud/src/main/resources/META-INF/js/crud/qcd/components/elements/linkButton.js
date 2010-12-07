/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.LinkButton = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;
	
	var element = _element;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	
	var pageUrl;
	
	var button = $("#"+element.attr('id')+"_buttonDiv");
	var buttonLink = $("#"+element.attr('id')+"_buttonLink");
	
	this.getComponentValue = function() {
		return { value: {}};
	}
	
	this.setComponentValue = function(value) {
		insertValue(value);
	}
	
	this.setComponentState = function(state) {
		insertValue(state);
	}
	
	function insertValue(value) {
		pageUrl = value;
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			button.addClass('activeButton');
		} else {
			button.removeClass('activeButton');
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {

	}
	
	function onButtonClick(e) {
		buttonLink.blur();
		if (button.hasClass('activeButton')) {
			mainController.goToPage(pageUrl);
		}
	}
	
	function constructor(_this) {		
		buttonLink.click(onButtonClick);
	}
	
	constructor(this);
}