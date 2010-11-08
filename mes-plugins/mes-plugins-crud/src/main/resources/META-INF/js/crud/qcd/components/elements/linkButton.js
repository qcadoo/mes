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
	
	//var button = $("#"+element.attr('id')+"_button");
	var button;
	
	this.getComponentValue = function() {
		return null;
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
			button.addClass('headerButtonEnabled');
		} else {
			button.removeClass('headerButtonEnabled');
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {

	}
	
	function onButtonClick() {
		if (button.hasClass('headerButtonEnabled')) {
			mainController.goToPage(pageUrl);
		}
	}
	
	function constructor(_this) {
		var labelToTranslate = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".label";
		button = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(labelToTranslate), onButtonClick);
		button.addClass("linkButton");
		element.append(button);
	}
	
	constructor(this);
}