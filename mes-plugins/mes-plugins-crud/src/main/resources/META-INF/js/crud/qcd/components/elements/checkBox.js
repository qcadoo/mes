/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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
	var textRepresentation = $("#" + _element.attr('id') + "_text");
	var currentValue;
	
	this.getComponentData = function() {
		if (this.input.attr('checked')) {
			return { value: "1" };
		}
		return { value: "0" };
	}
	
	this.setComponentData = function(data) {
		if (data != null && data.value == 1) {
			this.input.attr('checked', true);
			textRepresentation.html(mainController.getTranslation('commons.true'));
		} else {
			this.input.attr('checked', false);
			textRepresentation.html(mainController.getTranslation('commons.false'));
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
	
	this.setSelected = function(actionsPerformer, isSelected) {
		this.input.attr('checked', isSelected);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.setCurrentValue = function(data) {
		currentValue = this.input.attr('checked');
	} 
	
	this.isChanged = function() {
		return currentValue != this.input.attr('checked');
	}

}