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

QCD.components.elements.EntityComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var mainController = _mainController;
	var options = this.options;
	var elementPath = this.elementPath;
	var input = this.input;
	
	if (this.options.referenceName) {
		mainController.registerReferenceName(this.options.referenceName, this);
	}
	
	function constructor(_this) {
		_this.input.change(onChange);
	}

	function onChange() {
		if (options.listeners.length > 0) {
			mainController.getUpdate(elementPath, input.val(), options.listeners);
		}
	}
	
	this.getComponentData = function() {
		var selected = this.input.val();
		if (!selected || $.trim(selected) == "") {
			selected = null;
		}
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		var previousSelected = this.input.val();
		
		if(data.values != null) {
			this.input.children().remove();
			var blankValue = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".blankValue";
			this.input.append("<option value=''>"+mainController.getTranslation(blankValue)+"</option>");
			for (var i in data.values) {
				var value = data.values[i];
				this.input.append("<option value='"+i+"'>"+value+"</option>");
			}
		}
		
		selected = data.value;
		
		if (selected != null) {
			this.input.val(selected);
		} else {
			this.input.val(previousSelected);
		}
	}
	
	constructor(this);
}