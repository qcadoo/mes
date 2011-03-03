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

QCD.components.containers.layout.SmallTabLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

	var tabs;
	
	var activeTab;
	
	if (this.options.referenceName) {
		_mainController.registerReferenceName(this.options.referenceName, this);
	}
	
	function constructor(_this) {
		var children = $("#"+_this.elementSearchName+"_layoutComponents").children();
		
		tabs = new Object();
		var firstTab = null;
		for (var i=0; i<children.length; i++) {
			var kid = $(children[i]);
			var tabName = kid.attr("id").split("_")[2];
			
			var headerElement = $("#"+_this.elementSearchName+"_headerItem_"+tabName);
			headerElement.click(onHeaderElementClick);
			
			tabs[tabName] = {
				header: headerElement,
				content: kid
			}
			if (!firstTab) {
				firstTab = tabName;
			}
		}
		showTab(firstTab);
		
		_this.constructChildren(_this.getLayoutChildren());
	}
	
	this.setComponentValue = function(value) {
		for (var tabName in tabs) {
			tabs[tabName].header.removeClass("errorHeader");
			tabs[tabName].content.removeClass("errorContent");
		}
		for (var i in value.errors) {
			var tabName = value.errors[i];
			tabs[tabName].header.addClass("errorHeader");
			tabs[tabName].content.addClass("errorContent");
		}
	}
	
	this.getLayoutChildren = function() {
		return $("#"+this.elementSearchName+"_layoutComponents").children().children();
	}
	
	function onHeaderElementClick() {
		var element = $(this);
		element.blur();
		if (element.hasClass("active")) {
			return;
		}
		var tabName = element.attr("id").split("_")[2];
		showTab(tabName);
	}
	
	function showTab(tabName) {
		if (activeTab) {
			activeTab.header.removeClass("active");
			activeTab.content.hide();
		}
		var tab = tabs[tabName];
		tab.header.addClass("active");
		tab.content.show();
		activeTab = tab;
	}
	
	this.updateSize = function(_width, _height) {
		this.element.height(_height);
		for (var tabName in tabs) {
			tabs[tabName].content.height(_height - 34);
		}
		for (var i in this.components) {
			this.components[i].updateSize(_width-20, _height-32);
		}
	}
	
	constructor(this);
}