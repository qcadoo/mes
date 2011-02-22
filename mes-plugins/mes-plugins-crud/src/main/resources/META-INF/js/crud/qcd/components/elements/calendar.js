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

QCD.components.elements.Calendar = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var ANIMATION_LENGTH = 200;
	
	var containerElement = _element;
	
	var calendar = $("#"+this.elementSearchName+"_calendar");
	
	var input = this.input;
	
	var datepicker;
	var datepickerElement;
	
	var element = this.element;
	var elementPath = this.elementPath;
	
	var opened = false;
	
	var skipButtonClick = false;
	
	var isTriggerBootonHovered = false;
	
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	var fireOnChangeListeners = this.fireOnChangeListeners;
	
	var addMessage = this.addMessage; 
	
	var isValidationError = false;
	
	if (this.options.referenceName) {
		_mainController.registerReferenceName(this.options.referenceName, this);
	}
	
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
		options.altField = input;
		options.onClose = function(dateText, inst) {
			opened = false;
			if (isTriggerBootonHovered) {
				skipButtonClick = true;
			}
		}
		options.onSelect = function(dateText, inst) {
			datepickerElement.slideUp(ANIMATION_LENGTH);
			opened = false;
			inputDataChanged();
		}
		
		datepickerElement = $("<div>").css("position", "absolute").css("zIndex", 100).css("right", "15px");
		containerElement.css("position", "relative");
		datepickerElement.hide();
		
		$("#ui-datepicker-div").hide();
		
		containerElement.append(datepickerElement);
		
		datepickerElement.datepicker(options);
		
		input.val("");
		
		$(document).mousedown(function(event) {
			if(!opened) {
				return;
			}
			var target = $(event.target);
			if (target.attr("id") != input.attr("id") && target.attr("id") != calendar.attr("id")
					&& target.parents('.ui-datepicker').length == 0) {
				datepickerElement.slideUp(ANIMATION_LENGTH);
				opened = false;
			}
		});
		
		calendar.hover(function() {isTriggerBootonHovered = true;}, function() {isTriggerBootonHovered = false;})
		calendar.click(function() {
			if(calendar.hasClass("enabled")) {
				if (skipButtonClick) {
					skipButtonClick = false;
					return;
				}
				if(!opened) {
					
					if (input.val()) {
						try {
							$.datepicker.parseDate( "yy-mm-dd", input.val());
							datepickerElement.datepicker("setDate", input.val());
						} catch (e) {
							// do nothing
						}
					}
					
					var top = input.offset().top;
					var calendarHeight = datepickerElement.outerHeight();
					var inputHeight = input.outerHeight() + 10;
					var viewHeight = document.documentElement.clientHeight + $(document).scrollTop();
					
					if ((top+calendarHeight+inputHeight) > viewHeight) {
						datepickerElement.css("top", "");
						datepickerElement.css("bottom", inputHeight +"px");
						isOnTop = true;
					} else {
						datepickerElement.css("top", inputHeight +"px");
						datepickerElement.css("bottom", "");
						isOnTop = false;
					}
					
					datepickerElement.slideDown(ANIMATION_LENGTH).show();
					opened = true;
				} else {
					datepickerElement.slideUp(ANIMATION_LENGTH)
					opened = false;
				}
			}
		});
		
		input.focus(function() {
			calendar.addClass("lightHover");
		}).blur(function() {
			calendar.removeClass("lightHover");
		});
		
		input.change(function() {
			inputDataChanged();
		});
	}
	
	function inputDataChanged() {
		var date = getDate();
		if (!isValidationError) {
			if (date == null) {
				addMessage({
					title: "",
					content: "not ok"
				});
				element.addClass("error");
			} else {
					element.removeClass("error");
			}
		}
		fireOnChangeListeners("onChange", [date]);
		if (hasListeners) {
			mainController.callEvent("onChange", elementPath, null, null, null);
		}
	}
	
	this.setComponentError = function(isError) {
		isValidationError = isError;
		if (isError) {
			element.addClass("error");
		} else {
			element.removeClass("error");
		}
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
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().parent().parent().height(height);
	}
	
	function getDate() {
		var dateString = input.val();
		if ($.trim(dateString) == "") {
			return 0;
		}
		var parts = dateString.split("-");
		if (parts.length != 3 || parts[0].length != 4 || parts[1].length != 2 || parts[2].length != 2) {
			return null;
		}
		try {
			return $.datepicker.parseDate("yy-mm-dd", dateString);
		} catch (e) {
			return null;
		}
	}
	this.getDate = getDate;
	
	this.setDate = function(date) {
		var dateString = $.datepicker.formatDate("yy-mm-dd", date);
		input.val(dateString);
		inputDataChanged();
	}
	
	constructor(this);
}