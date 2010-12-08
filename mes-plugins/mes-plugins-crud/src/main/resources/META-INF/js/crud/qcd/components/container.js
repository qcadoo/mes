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

QCD.components.Container = function(_element, _mainController, childrenElements) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	var mainController = _mainController;

	var components;
	
	this.constructChildren = function(childrenElements) {
		components = QCDPageConstructor.getChildrenComponents(childrenElements, mainController);
		this.components = components;
	}
	
	this.getComponentsValue = function() {
		var values = new Object();
		for (var i in components) {
			values[i] = components[i].getValue();
		}
		return values;
	}
	
	this.setComponentsValue = function(value) {
		for (var i in value.components) {
			var componentValue = value.components[i];
			components[i].setValue(componentValue);
		}
	}
	
	this.setComponentsState = function(state) {
		for (var i in state.components) {
			var componentState = state.components[i];
			components[i].setState(componentState);
		}
	}

	
	this.isChanged = function() {
		changed = this.isComponentChanged();
		if (changed == true) {
			return true;
		}
		for (var i in components) {
			if(components[i].isChanged()) {
				changed = true;
				break;
			}
		}
		return changed;
	}
	
}