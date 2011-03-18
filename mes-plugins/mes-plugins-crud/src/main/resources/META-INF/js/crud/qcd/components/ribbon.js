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

QCD.components.Ribbon = function(_model, _elementName, _mainController, _translations) {
	
	var ribbonModel = _model;
	var mainController = _mainController;
	var elementName = _elementName;
	var translations = _translations;
	
	var element;
	
	var jsObjects = new Object();
	
	this.constructElement = function() {
		
		element = $("<div>");
		
		var contentWrapper = $("<div>").attr("id", "q_row3_out");
		element.append(contentWrapper);
		element.append($("<div>").attr("id", "q_row4_out"));
		
		var content = $("<div>").attr("id", "q_menu_row3");
		content.append(this.constructElementContent());
		contentWrapper.append(content);
		
		return element;
	}
	
	this.constructElementContent = function() {
		var content = $("<div>"); 
		if (ribbonModel.groups) {
			for (var groupIter in ribbonModel.groups) {
				var groupModel = ribbonModel.groups[groupIter];
				var groupContent = $("<div>").addClass("ribbon_content");
				var groupTitle = $("<div>").addClass("ribbon_title").html(groupModel.label);
				var ribbonMenu_right = $("<div>").addClass("ribbonMenu_right").append(groupTitle).append(groupContent);
				var ribbonMenu_left = $("<div>").addClass("ribbonMenu_left").append(ribbonMenu_right);
				var groupElement = $("<div>").addClass("ribbonMenu").append(ribbonMenu_left);
				
				var smallElementsGroupElement = null;
				for (var itemsIter in groupModel.items) {
					var itemModel = groupModel.items[itemsIter];
					var itemElement = null;
					var isSmall = false;
					
					itemModel.group = groupModel.name;
					
					if (itemModel.type == "BIG_BUTTON") {
						if (itemModel.items) {
							itemElement = createBigButtonWithDropdown(groupModel.name, itemModel);
						} else {
							itemElement = createBigButton(groupModel.name, itemModel);
						}
					} else if (itemModel.type == "SMALL_BUTTON") {
						if (itemModel.items) {
							itemElement = createSmallButtonWithDropdown(groupModel.name, itemModel);
						} else {
							itemElement = createSmallButton(groupModel.name, itemModel);
						}
						isSmall = true;
					} else if (itemModel.type == "COMBOBOX") {
						itemElement = createComboBox(groupModel.name, itemModel);
						isSmall = true;
					} else if (itemModel.type == "SMALL_EMPTY_SPACE") {
						itemElement = $("<div>");
						isSmall = true;
					}
					
					if (itemElement) {
						if (isSmall) {
							if (smallElementsGroupElement) {
								smallElementsGroupElement.append(itemElement);
								smallElementsGroupElement = null;
							} else {
								smallElementsGroupElement = $("<ul>").addClass("ribbon_list");
								smallElementsGroupElement.append(itemElement);
								groupContent.append(smallElementsGroupElement);
							}
						} else {
							groupContent.append(itemElement);
							smallElementsGroupElement = null;
						}
					}
					
					itemModel.element = itemElement;
					
					if (itemModel.enabled) {
						itemElement.addClass("enabled");
					}
					if (itemModel.message) {
						if (itemModel.tooltipElement) {
							if (itemModel.message && itemModel.message != "") {
								itemModel.tooltipElement.show();
								itemModel.tooltipMessageElementContent.html(itemModel.message);
							} else {
								itemModel.tooltipElement.hide();
							}
						}
					} else {
						if (itemModel.tooltipElement) {
							itemModel.tooltipElement.hide();
						}
					}
					
				}
				content.append(groupElement);
			}
		}
		return content;
	}
	
	function createBigButton(path, itemModel) {
		var aElement = $("<a>").attr('href','#').html("<span><div"+getItemIconStyle(itemModel)+"></div><label class='ribbonLabel'>"+itemModel.label+"</label></div></div></span>");
		var liElement = $("<li>").append(aElement);
		var ribbonListElement = $("<ul>").addClass("ribbonListElement").append(liElement);
		var itemElement = $("<div>").addClass("ribbonBigElement").append(ribbonListElement);
		createTooltip(itemModel,aElement);
		aElement.bind('click', {itemElement: itemElement, itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createBigButtonWithDropdown(path, itemModel) {
		var icon = (itemModel.icon && $.trim(itemModel.icon) != "") ? $.trim(itemModel.icon) : null;
		var style = "";
		var className = "";
		if (icon) {
			style = " style=\"background-image:url(\'../../images/icons/"+icon+"\')\"";
			className = " hasIcon";
		}
		var itemElementButton = $("<a>").attr('href','#').html("<span><div class='"+className+" bigDropdownButtonDiv' "+style+"><label class='ribbonLabel'>"+itemModel.label+"</label><div></div></div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').html("<span><div class='icon_btn_addB'></div></span>");
		var buttonDropdownLi = $("<li>").addClass("addB").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var liElement = $("<li>").append(spanElement);
		var ribbonAddElement = $("<ul>").addClass("ribbonAddElement").append(liElement);
		var itemElement = $("<div>").addClass("ribbonBigElement").addClass("ribbonDropdownContainer").append(ribbonAddElement);
		
		var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("bigButtonDropdownMenu");
		addDropdownAction(itemElementDropdownButton);
		itemElement.append(dropdownMenu);
		
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);

		return itemElement;
	}
	
	function createSmallButton(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div"+getItemIconStyle(itemModel)+"></div><div class='btnOneLabel ribbonLabel'>"+itemModel.label+"</div></span>");
		createTooltip(itemModel,itemElementButton);
		var itemElement = $("<li>").addClass("btnOne").append(itemElementButton);
		itemElementButton.bind('click', {itemElement: itemElement, itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createSmallButtonWithDropdown(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div "+getItemIconStyle(itemModel)+"></div><div class='btnOneLabel'>"+itemModel.label+"</div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').addClass("twoB_down");
		var buttonDropdownLi = $("<li>").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var itemElement = $("<li>").addClass("twoB").addClass("ribbonDropdownContainer").append(spanElement);
		
		itemElementButton.bind('click', {itemElement: itemElement, itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		
		var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("smallButtonDropdownMenu");
		addDropdownAction(itemElementDropdownButton, itemElement);
		itemElement.append(dropdownMenu);
			
		return itemElement;
	}
	
	function createDropdownMenu(path, itemModel) {
		var dropdownMenuContent = $("<ul>");
		for (var menuIter in itemModel.items) {
			var dropdownItem = itemModel.items[menuIter];
			var menuItemName = dropdownItem.name;
			var icon = (dropdownItem.icon && $.trim(dropdownItem.icon) != "") ? $.trim(dropdownItem.icon) : null;
			var style = "";
			if (icon) {
				style = " style=\"background-image:url(\'/img/core/icons/"+icon+"\')\"";
			}
			var menuItemButton = $("<a>").attr('href','#').html("<span class='dropdownItemIcon' "+style+"></span><span class='dropdownItemLabel'>"+dropdownItem.label+"</span>").addClass("icon");
			var menuItem = $("<li>").append(menuItemButton);
			menuItemButton.bind('click', {itemElement: menuItem, itemName: itemModel.name+"."+menuItemName, clickAction: dropdownItem.clickAction}, buttonClicked);
			dropdownItem.element = menuItem;
			dropdownItem.group = itemModel.group;
			dropdownMenuContent.append(menuItem);
		}
		var dropdownMenu = $("<div>").addClass("dropdownMenu").addClass("m_module").append(dropdownMenuContent);
		return dropdownMenu;
	}
	
	function addDropdownAction(dropdownTriggerButton, itemElement) {
		dropdownTriggerButton.addClass("dropdownTrigger");
		dropdownTriggerButton.click(function() {
			var parent = $(this);
			parent.blur();
			if (! itemElement.hasClass("enabled")) {
				return;
			}
			while(! parent.hasClass("ribbonDropdownContainer")) {
				parent = parent.parent();
			}
			if (parent.find(".dropdownMenu").is(":visible")) {
				parent.find(".dropdownMenu").slideUp(100);
			} else {
				parent.find(".dropdownMenu").slideDown(100).show();
				parent.hover(function() {}, function(){  
					parent.find(".dropdownMenu").slideUp(100);
				});
			}
		});
	}
	
	function createComboBox(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div "+getItemIconStyle(itemModel)+">"+itemModel.label+"</div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').addClass("twoB_down");
		var buttonDropdownLi = $("<li>").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var itemElement = $("<li>").addClass("twoB").addClass("ribbonDropdownContainer").append(spanElement);
		
		//itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		
		//var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("smallButtonDropdownMenu");
		//addDropdownAction(itemElementDropdownButton);
		//itemElement.append(dropdownMenu);
			
		return itemElement;
	}
	
	function createTooltip(itemModel, itemElementButton) {
		var tooltipElement = $("<div>").addClass("ribbon_description_icon").css("display", "none");
		var tooltipMessageElement = $("<div>").addClass("description_message").css("display", "none");
		var tooltipMessageElementContent = $("<p>").html("");
		tooltipMessageElement.append(tooltipMessageElementContent);
		itemElementButton.append(tooltipElement);
		itemElementButton.append(tooltipMessageElement);
		tooltipElement.hover(function() {
			tooltipMessageElement.show();
		}, function() {
			tooltipMessageElement.hide();
		});
		itemModel.tooltipElement = tooltipElement;
		itemModel.tooltipMessageElementContent = tooltipMessageElementContent;
	}
	
	function getItemIconStyle(itemModel) {
		var icon = (itemModel.icon && $.trim(itemModel.icon) != "") ? $.trim(itemModel.icon) : null;
		var style = "";
		if (icon) {
			style = " class='iconElement hasIcon' style=\"background-image:url(\'/img/core/icons/"+icon+"\')\"";
		} else {
			style = "class='iconElement' ";
		}
		
		return style;
	}
	
	function buttonClicked(e) {
		$(this).blur();
		if (e.data.itemElement) {
			if (! e.data.itemElement.hasClass("enabled")) {
				return;
			}
		}
		var action = e.data.clickAction;
		var name = e.data.itemName;
		var onChangeListeners = e.data.itemElement.onChangeListeners;
		if (onChangeListeners) {
			for (var i in onChangeListeners) {
				onChangeListeners[i].onClick();
			} 
		}
		if (action) {
			mainController.getActionEvaluator().performRibbonAction(action);
		}
	}
	
	function createJsObject(item) {
		if (jsObjects[item.group+"-"+item.name]) {
			return jsObjects[item.group+"-"+item.name];
		}
		var jsObject = {
			element: item.element,
			tooltipElement: item.tooltipElement,
			tooltipMessageElementContent: item.tooltipMessageElementContent,
			disable: function(msg) {
				this.element.removeClass("enabled");
				if (this.tooltipElement) {
					if (msg && msg != "") {
						this.tooltipElement.show();
						this.tooltipMessageElementContent.html(msg);
					} else {
						this.tooltipElement.hide();
					}
				}
			},
			enable: function(msg) {
				this.element.addClass("enabled");
				if (this.tooltipElement) {
					if (msg && msg != "") {
						this.tooltipElement.show();
						this.tooltipMessageElementContent.html(msg);
					} else {
						this.tooltipElement.hide();
					}
				}
			},
			isEnabled: function() {
				return this.element.hasClass("enabled");
			},
			setLabel: function(label) {
				this.element.find('.ribbonLabel').html(label);
			},
			setIcon: function(icon) {
				var iconEl = this.element.find('.iconElement');
				if (icon) {
					iconEl.addClass("hasIcon");
					iconEl.css("backgroundImage", "url(\'/img/core/icons/"+icon+"\')");
				} else {
					iconEl.removeClass("hasIcon");
				}
			},
			addOnChangeListener: function(listener) {
				if (! this.element.onChangeListeners) {
					this.element.onChangeListeners = new Array();
				}
				this.element.onChangeListeners.push(listener);
			},
			removeOnChangeListeners: function() {
				this.element.onChangeListeners = new Array();
			}
		};
		jsObjects[item.group+"-"+item.name] = jsObject;
		return jsObject;
	}
	
	this.performScripts = function() {
		for (var groupIter in ribbonModel.groups) {
			var group = ribbonModel.groups[groupIter];
			for (var itemsIter in group.items) {
				var item = group.items[itemsIter];
				if (item.script) {
					var scriptObject = createJsObject(item);
					mainController.getActionEvaluator().performJsAction(item.script, scriptObject);
				}
			}
		}
	}
	
	this.updateRibbonState = function(ribbonState) {
		for (var g in ribbonState.groups) {
			var group = ribbonState.groups[g];
			for (var i in group.items) {
				var item = group.items[i];
				var itemObject = this.getRibbonItem(group.name+"."+item.name);
				if (item.label) {
					itemObject.setLabel(item.label);
				}
				if (item.enabled != undefined) {
					if (item.enabled) {
						itemObject.enable();
					} else {
						itemObject.disable();
					}
				}
				if (item.message) {
					if (itemObject.isEnabled()) {
						itemObject.enable(item.message);
					} else {
						itemObject.disable(item.message);
					}
				}
			}
		}
	}
	
	this.getRibbonItem = function(ribbonItemPath) {
		var pathParts = ribbonItemPath.split(".");
		if (pathParts.length != 2 && pathParts.length != 3) {
			QCD.error("wrong path: '"+ribbonItemPath+"'");
			return null;
		}
		var group = null;
		for (var groupIter in ribbonModel.groups) {
			if (ribbonModel.groups[groupIter].name == pathParts[0]) {
				group = ribbonModel.groups[groupIter];
				break;
			}
		}
		if (!group) {
			return null;
		}
		var item = null;
		for (var itemsIter in group.items) {
			if (group.items[itemsIter].name == pathParts[1]) {
				item = group.items[itemsIter];
				break;
			}
		}
		if (!item) {
			return null;
		}
		if (pathParts.length == 3) {
			if (! item.items) {
				return null;
			}
			var dropdownItem = null;
			for (var dropdownItemsIter in item.items) {
				if (item.items[dropdownItemsIter].name == pathParts[2]) {
					dropdownItem = item.items[dropdownItemsIter];
					break;
				}
			}
			if (! dropdownItem) {
				return null;
			}
			return createJsObject(dropdownItem);
		}
		return createJsObject(item);
	}

	this.updateSize = function(margin, innerWidth) {
		$("#q_menu_row3").css("margin-left", (margin)+"px");
		$("#q_row4_out").width(innerWidth);
	}
	
}