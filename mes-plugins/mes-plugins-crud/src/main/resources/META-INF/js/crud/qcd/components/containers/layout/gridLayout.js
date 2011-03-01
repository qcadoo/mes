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
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.GridLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));
	
	var elementSearchName = this.elementSearchName;
	var rootElement = $("#"+elementSearchName+"_layoutComponents > table > tbody");
	
	var colsNumber = this.options.colsNumber;
	
	var fixedRowHeight = this.options.fixedRowHeight;
	
	function constructor(_this) {
		_this.constructChildren(getLayoutChildren());
	}
	
	function getLayoutChildren() {
		var components = rootElement.children().children().children();
		return components;
	}
	
	this.updateSize = function(_width, _height) {
		var baseWidth = _width/colsNumber;
		var baseHeight = 50;
		
		var tdElements = rootElement.children().children();
		
		for (var i=0; i<tdElements.length; i++) {
			var tdElement = $(tdElements[i]);
			var colspan = tdElement.attr("colspan") ? tdElement.attr("colspan") : 1;
			var elementWidth = baseWidth * colspan;
			tdElement.width(elementWidth);
		}
		
		for (var i in this.components) {
			var tdElement = this.components[i].element.parent();
			var rowspan = tdElement.attr("rowspan") ? tdElement.attr("rowspan") : 1;
			var colspan = tdElement.attr("colspan") ? tdElement.attr("colspan") : 1;
			
			var elementWidth = baseWidth * colspan;
			
			if (fixedRowHeight) {
				var elementHeight = baseHeight * rowspan;
				this.components[i].updateSize(elementWidth, elementHeight);
			} else {
				this.components[i].updateSize(elementWidth);
			}
		}
	}
	
	constructor(this);
}