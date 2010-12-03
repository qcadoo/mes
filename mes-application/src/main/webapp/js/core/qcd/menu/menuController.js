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
QCD.menu = QCD.menu || {};

QCD.menu.MenuController = function(menuStructure, _windowController) {
	
	var windowController = _windowController;
	
	var firstLevelElement = $("#firstLevelMenu");
	var secondLevelElement = $("#secondLevelMenu");
	
	var previousActive = new Object();
	previousActive.first = null;
	previousActive.second = null;
	
	var currentActive = new Object();
	currentActive.first = null;
	currentActive.second = null;
	
	function constructor(menuStructure) {
		model = new QCD.menu.MenuModel(menuStructure.menuItems);
		
		var menuContentElement = $("<ul>").addClass("q_row1");
		var q_menu_row1 = $("<div>").attr("id", "q_menu_row1");
			q_menu_row1.append(menuContentElement);
		var q_row1 = $("<div>").attr("id", "q_row1");
			q_row1.append(q_menu_row1);
		var q_row1_out = $("<div>").attr("id", "q_row1_out");
			q_row1_out.append(q_row1);
		firstLevelElement.append(q_row1_out);
		
		for (var i in model.items) {
			var item = model.items[i];
			
			var firstLevelButton = $("<li>").html("<a href='#'><span>"+item.label+"</span></a>").attr("id", "firstLevelButton_"+item.name);
			menuContentElement.append(firstLevelButton);
			item.element = firstLevelButton;
			
			firstLevelButton.click(function(e) {
				onTopItemClick($(this), e);
			});
		}
		previousActive.first = model.selectedItem;
		model.selectedItem.element.addClass("path");
		previousActive.second = model.selectedItem.selectedItem;
		
		updateState();
		
		changePage(model.selectedItem.selectedItem.page);
	}
	
	
	
	function onTopItemClick(itemElement, e) {
		itemElement.children().blur();
		
		var buttonName = itemElement.attr("id").substring(17);
		var topItem = model.itemsMap[buttonName];
		
		model.selectedItem = topItem;
		if (model.selectedItem.selectedItem) {
			currentActive.second = model.selectedItem.selectedItem
		}
		
		updateState();
	}
	
	function onBottomItemClick(itemElement) {
		itemElement.children().blur();
		
		if (! canChangePage()) {
			return;
		}
		
		var buttonName = itemElement.attr("id").substring(18);
		var selectedItem = model.selectedItem.itemsMap[buttonName];
		
		model.selectedItem.selectedItem = selectedItem;
		
		previousActive.first.element.removeClass("path");
		previousActive.first = model.selectedItem;
		previousActive.second = model.selectedItem.selectedItem;
		previousActive.first.element.addClass("path");
		
		updateState();
		
		changePage(model.selectedItem.selectedItem.page);
	}
	
	this.goToMenuPosition = function(position) {
		var menuParts = position.split(".");
		
		var topItem = model.itemsMap[menuParts[0]];
		var bottomItem = topItem.itemsMap[menuParts[0]+"_"+menuParts[1]];
		
		model.selectedItem.element.removeClass("path");
		
		topItem.selectedItem = bottomItem;
		previousActive.first = topItem;
		previousActive.second = bottomItem;
		model.selectedItem = topItem;
		
		updateState();
		changePage(model.selectedItem.selectedItem.page);
	}
	
	this.hasMenuPosition = function(position) {
		var menuParts = position.split(".");
		var topItem = model.itemsMap[menuParts[0]];
		if (topItem == null) {
			return false;
		}
		var bottomItem = topItem.itemsMap[menuParts[0]+"_"+menuParts[1]];
		if (bottomItem == null) {
			return false;
		}
		return true;
	}
	
	this.restoreState = function() {
		model.selectedItem = previousActive.first;
		if (previousActive.second) {
			model.selectedItem.selectedItem = previousActive.second;
		}
		updateState();
	}
	
	function updateState() {
		if (currentActive.first != model.selectedItem) {
			if (currentActive.first) {
				currentActive.first.element.removeClass("activ");
				currentActive.first.selectedItem = null;
			}
			currentActive.first = model.selectedItem;
			currentActive.first.element.addClass("activ");
			
			updateSecondLevel();
			
		} else {
			if (currentActive.second != model.selectedItem.selectedItem) {
				if (currentActive.second) {
					currentActive.second.element.removeClass("activ");
				}
				currentActive.second = model.selectedItem.selectedItem;
				if (currentActive.second) {
					currentActive.second.element.addClass("activ");
				}
			}
		}
	}
	
	function updateSecondLevel() {
		secondLevelElement.children().remove();
		
		var menuContentElement = $("<ul>").addClass("q_row2");
		var q_menu_row2 = $("<div>").attr("id", "q_menu_row2");
			q_menu_row2.append(menuContentElement);
		var q_row2_out = $("<div>").attr("id", "q_row2_out");
			q_row2_out.append(q_menu_row2);
		secondLevelElement.append(q_row2_out);
		
		for (var i in model.selectedItem.items) {
			var secondLevelItem = model.selectedItem.items[i];
			var secondLevelButton = $("<li>").html("<a href='#'><span>"+secondLevelItem.label+"</span></a>").attr("id", "secondLevelButton_"+secondLevelItem.name);
			menuContentElement.append(secondLevelButton);
			secondLevelItem.element = secondLevelButton;

			secondLevelButton.click(function() {
				onBottomItemClick($(this));
			});
			
			if (previousActive.second && previousActive.second.name == secondLevelItem.name) {
				secondLevelItem.element.addClass("activ");
				currentActive.second = secondLevelItem;
			}
			
		}
	} 
	
	function changePage(page) {
		windowController.onMenuClicked(page);
	}
	
	function canChangePage() {
		return windowController.canChangePage();
	}
	
	constructor(menuStructure);
}
