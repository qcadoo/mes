/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
	
	var baseValue;
	
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
			value : this.input.val(),
		}
	}

	this.setComponentData = function(data) {
		if (data.value) {
			this.input.val(data.value);
		} else {
			this.input.val("");
		}
	}

	this.getComponentValue = function() {
		var value = this.getComponentData();
		value.required = element.hasClass("required");		
		value.baseValue = baseValue;
		return value;
	}

	this.setComponentValue = function(value) {
		this.setComponentData(value);
		setComponentRequired(value.required);
	}

	this.setComponentState = function(state) {
		this.setComponentData(state);
		setComponentRequired(state.required);
		if (state.baseValue != undefined) {
			baseValue = state.baseValue;
		}
	}
	
	this.performUpdateState = function() {
		baseValue = this.getComponentData().value;
	}
	this.isComponentChanged = function() {
		if (! (baseValue == this.getComponentData().value)) {
			baseValue
		}
		return ! (baseValue == this.getComponentData().value);
	}

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
			this.addMessage(messages[i]);
		}
		if (messages) {
			setComponentError(messages.length != 0);
		}
	}
	
	this.addMessage = function(message) {
		messageDiv = $('<div>');
		
		messageDiv.append('<span>' + message.title + '</span>');
		messageDiv.append('<p>' + message.content + '</p>');

		errorMessages.append(messageDiv);

		var top = this.input.offset().top;
		var errorIconHeight = errorMessages.height();
		var inputHeight = this.input.outerHeight() - 1;
		var viewHeight = document.documentElement.clientHeight + $(document).scrollTop();

		if ((top+errorIconHeight+inputHeight) > viewHeight) {
			errorMessages.css("top", "");
			errorMessages.css("bottom", errorIcon.outerHeight()+"px");
		} else {
			errorMessages.css("top", errorIcon.outerHeight()+"px");
			errorMessages.css("bottom", "");
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
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().height(height);
	}
	
	constructor(this);

}