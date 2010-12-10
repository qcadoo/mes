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

QCD.components.elements.DynamicComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var mainController = _mainController;
	var elementPath = this.elementPath;
	var stateSelectedValue = null;
	
	var input = this.input;

	this.getComponentData = function() {
		var selected = this.input.val();
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		setData(data);
	}
	
	function setData(data) {
		if (data == null) {
			return;
		}
		if (data.value && ! data.values) { // is setState
			stateSelectedValue = data.value;
			return;
		}
		var previousSelected = input.val();
		input.children().remove();
		for (var i in data.values) {
			var value = data.values[i];
			input.append("<option value='"+value.key+"'>"+value.value+"</option>");
		}
		
		if (stateSelectedValue) {
			selected = stateSelectedValue;
		} else {
			selected = data.value;
		}
		
		if (!selected || $.trim(selected) == "") {
			input.val(previousSelected);
		} else {
			input.val(selected);
		}
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().height(height);
	}
}
