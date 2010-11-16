/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
		if (! this.selectedItem) {
			this.selectedItem = secondButton;
		}
	}
	if (this.items.length == 0) {
		this.ribbon = new QCD.menu.RibbonModel(this.name, menuItem.ribbonItems);
	}
}

QCD.menu.SecondButton = function(menuItem, firstButton) {
	this.name = firstButton.name+"_"+menuItem.name;
	this.label = menuItem.label;
	
	this.element = null;
	
	this.ribbon = new QCD.menu.RibbonModel(this.name, menuItem.ribbonItems);
}

QCD.menu.RibbonModel = function(parentName, ribbonItems) {
	this.name = parentName+"_ribbon";
	this.items = ribbonItems;
}
