/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.FormComponent = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;

	var element = _element;
	
	var errorIcon = $("#" + this.elementSearchName + "_error_icon");
	var errorMessages = $("#" + this.elementSearchName + "_error_messages");

	var descriptionIcon = $("#" + this.elementSearchName + "_description_icon");
	var descriptionMessage = $("#" + this.elementSearchName + "_description_message");
	
	var currentValue;
	
	this.input = $("#" + this.elementSearchName + "_input");

	function constructor(_this) {
		_this.registerCallbacks();
	}
	
	this.registerCallbacks = function() {
		descriptionIcon.hover(function() {
			descriptionMessage.show();
		}, function() {
			descriptionMessage.hide();
		});

		errorIcon.hover(function() {
			errorMessages.show();
		}, function() {
			errorMessages.hide();
		});
	}
	
	this.getComponentData = function() {
		return {
			value : this.input.val()
		}
	}

	this.setComponentData = function(data) {
		if (data.value) {
			this.input.val(data.value);
		}
	}

	this.getComponentValue = function() {
		var value = this.getComponentData();
		value.required = element.hasClass("required");
		return value;
	}

	this.setComponentValue = function(value) {
		this.setComponentData(value);
		setComponentRequired(value.required);
	}

	this.setComponentState = function(state) {
		this.setComponentData(state);
		setComponentRequired(state.required);
	}
	
	this.setCurrentValue = function(data) {
		currentValue = data.value ? data.value : "";
	} 
	
//	this.isChanged = function() {
//		return currentValue != this.input.val();
//	}

	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			element.removeClass("disabled");
			this.input.removeAttr('disabled');
		} else {
			element.addClass("disabled");
			this.input.attr('disabled', 'true');
		}
		if (this.setFormComponentEnabled) {
			this.setFormComponentEnabled(isEnabled);
		}
	}
	
	function setComponentRequired(isRequired) {
		if (isRequired) {
			element.addClass("required");
		} else {
			element.removeClass("required");
		}
	}

	this.setMessages = function(messages) {
		errorMessages.html("");
		for ( var i in messages) {
			messageDiv = $('<div>');
			
			QCD.info(messages[i]);

			message = QCD.MessagesController.split(messages[i].content, 'error');

			messageDiv.append('<span>' + message[0] + '</span>');
			messageDiv.append('<p>' + message[1] + '</p>');

			errorMessages.append(messageDiv);
		}
		if (messages) {
			setComponentError(messages.length != 0);
		}
	}
	
	function setComponentError(isError) {
		if (isError) {
			element.addClass("error");
		} else {
			element.removeClass("error");
		}
	}

	this.setComponentLoading = function(isLoadingVisible) {}
	
	constructor(this);

}