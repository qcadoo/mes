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
	
	var calendar = $("#"+this.elementPath+"_calendar");
	
	var input = this.input;
	
	var datepicker;
	var datepickerElement;
	
	var opened = false;
	
	var skipButtonClick = false;
	
	var isTriggerBootonHovered = false;
	
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
		}
		
		datepickerElement = $("<div>").css("position", "absolute").css("zIndex", 100).css("right", "15px");
		containerElement.css("position", "relative");
		datepickerElement.hide();
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
					var inputHeight = input.outerHeight() - 1;
					var viewHeight = document.documentElement.clientHeight + $(document).scrollTop();
					
					if ((top+calendarHeight+inputHeight) > viewHeight) {
						datepickerElement.css("top", "");
						datepickerElement.css("bottom", inputHeight+"px");
						isOnTop = true;
					} else {
						datepickerElement.css("top", inputHeight+"px");
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