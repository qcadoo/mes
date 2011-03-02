/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

QCD.components.elements.CheckBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var mainController = _mainController;
	var textRepresentation = $("#" + this.elementSearchName + "_text");
	var currentValue;
	
	var element = this.element;
	
	var translations = this.options.translations; 
	
	if (this.options.referenceName) {
		mainController.registerReferenceName(this.options.referenceName, this);
	}
	
	this.getComponentData = function() {
		if (this.input.attr('checked')) {
			return { value: "1" };
		}
		return { value: "0" };
	}
	
	this.setComponentData = function(data) {
		if (data != null && (data.value == 1 || data.value == "true")) {
			this.input.attr('checked', true);
			textRepresentation.html(translations["true"]);
		} else {
			this.input.attr('checked', false);
			textRepresentation.html(translations["false"]);
		}
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if(this.options.textRepresentationOnDisabled) {
			if(isEnabled) {
				this.input.show();
				textRepresentation.hide();
			} else {
				this.input.hide();
				textRepresentation.show();
			}
		}
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			textRepresentation.removeClass("disabled");
			this.input.removeAttr('disabled');
			element.removeClass("disabled");
		} else {
			textRepresentation.addClass("disabled");
			this.input.attr('disabled', 'true');
			element.addClass("disabled");
		}
		if (this.setFormComponentEnabled) {
			this.setFormComponentEnabled(isEnabled);
		}
	}
	
	this.setSelected = function(actionsPerformer, isSelected) {
		this.input.attr('checked', isSelected);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.setCurrentValue = function(data) {
		currentValue = this.input.attr('checked');
	} 
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().height(height);
	}
	
}