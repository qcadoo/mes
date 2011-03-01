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

QCD.components.containers.layout.VerticalLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

	function constructor(_this) {
		_this.constructChildren(_this.getLayoutChildren());
	}
	
	this.getLayoutChildren = function() {
		return $("#"+this.elementSearchName+"_layoutComponents > div").children();
	}
	
	this.updateSize = function(_width, _height) {
		divCount = $("#"+this.elementSearchName+"_layoutComponents > div").length;
		
		if(divCount > 0) {
			divWidth = parseInt(_width / divCount);
			firstDivWidth = _width - (divWidth * (divCount-1));
			first = true;
			
			$("#"+this.elementSearchName+"_layoutComponents > div").each(function () {
				if(first) {
					$(this).width(firstDivWidth);
					first = false;
				} else {
					$(this).width(divWidth);
				}
			});
			
			for (var i in this.components) {
				this.components[i].updateSize(divWidth, _height);
			}
		}
	}
	
	constructor(this);
}