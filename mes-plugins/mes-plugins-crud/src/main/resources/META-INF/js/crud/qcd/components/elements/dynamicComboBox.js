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

QCD.components.elements.DynamicComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var element = _element;
	var mainController = _mainController;
	var elementPath = this.elementPath;
	
	var input = this.input;
	var values = new Array();
	
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	if (this.options.referenceName) {
		mainController.registerReferenceName(this.options.referenceName, this);
	}
	
	function constructor(_this) {
		input.change(function() {
			setTitle();
			inputDataChanged();
		});
	}
	
	function inputDataChanged() {
		if (hasListeners) {
			mainController.callEvent("onSelectedEntityChange", elementPath, null, null, null);
		}
	}
	
	function setTitle() {
		title = input.find(':selected').text();
		value = input.val();
		
		if(title && value) {		
			input.attr('title', title);
		} else {
			input.removeAttr('title');
		}
	}
	
	this.getComponentData = function() {
		var selected = this.input.val();
		return {
			value: selected,
			values: values
		}
	}
	
	this.setComponentData = function(data) {
		setData(data);
	}
	
	function setData(data) {
		if (data.values) {
			values = data.values;
			input.children().remove();
			for (var i in data.values) {
				var value = data.values[i];
				input.append("<option value='"+value.key+"'>"+value.value+"</option>");
			}
		}
		input.val(data.value);
		setTitle();
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
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().height(height);
	}
		
	constructor(this);
	
}
