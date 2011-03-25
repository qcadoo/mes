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
QCD.menu = QCD.menu || {};

QCD.menu.MenuModel = function(menuItems) {
	
	this.selectedItem = null;
	
	this.items = new Array();
	this.itemsMap = new Object();
	for (var i in menuItems) {
		var button = new QCD.menu.FirstButton(menuItems[i]);
		this.items.push(button);
		this.itemsMap[button.name] = button;
		if (! this.selectedItem) {
			this.selectedItem = button;
			button.selectedItem = button.items[0]; 
		}
	}
}

QCD.menu.FirstButton = function(menuItem) {
	this.name = menuItem.name;
	this.label = menuItem.label;
	
	this.element = null;
	
	this.selectedItem = null;
	
	this.itemsMap = new Object();
	this.items = new Array();
	for (var i in menuItem.items) {
		var secondButton = new QCD.menu.SecondButton(menuItem.items[i], this);
		this.itemsMap[secondButton.name] = secondButton;
		this.items.push(secondButton);
	}
}

QCD.menu.SecondButton = function(menuItem, firstButton) {
	this.name = firstButton.name+"_"+menuItem.name;
	this.label = menuItem.label;
	
	this.page = menuItem.page;
	
	this.element = null;
	
}