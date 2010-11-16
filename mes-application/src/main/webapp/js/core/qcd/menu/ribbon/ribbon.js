/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.menu = QCD.menu || {};

QCD.menu.Ribbon = function(menuController, _windowController) {
	
	var windowController = _windowController;
	
	var element = $("#ribbonLevelMenu");
	
	this.setRibbon = function(ribbonModel) {
		element.children().remove();
		
		if (ribbonModel.items) {
			for (var groupIter in ribbonModel.items) {
				var groupModel = ribbonModel.items[groupIter];
				var groupElement = $("<div>").addClass("ribbonGroup");
				
				var smallElementsGroupElement = null;
				for (var itemsIter in groupModel.items) {
					var itemModel = groupModel.items[itemsIter];
					
					if (itemModel.type == "bigButton") {
						var itemElement = $("<div>").html(getItemLabel(itemModel)).addClass("ribbonBigElement").addClass("ribbonBigButton");
						itemElement.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
						groupElement.append(itemElement);
						smallElementsGroupElement = null;
						
					} else if (itemModel.type == "bigButtonDropdown") {
						var itemElement = $("<div>").addClass("ribbonBigElement").addClass("bigButtonDropdown");
							var itemElementButton = $("<div>").html(getItemLabel(itemModel)).addClass("bigButtonDropdownMainButton");
							itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
							itemElement.append(itemElementButton);
							var itemElementDropdownButton = $("<div>").addClass("bigButtonDropdownBottomButton").addClass("dropdownTrigger");
								var arrow = $("<img src='css/images/downArrow.png'>");
								itemElementDropdownButton.append(arrow);
							itemElement.append(itemElementDropdownButton);
							var dropdownMenu = $("<ul>").addClass("bigButtonDropdownMenu").addClass("dropdownMenu");
							for (var menuIter in itemModel.items) {
								var menuItemName = itemModel.items[menuIter].name;
								var menuItem = $("<li>").html(getItemLabel(itemModel.items[menuIter]));
								menuItem.bind('click', {itemName: itemModel.name+"."+menuItemName, clickAction: itemModel.items[menuIter].clickAction}, buttonClicked);
								dropdownMenu.append(menuItem);
							}
							itemElement.append(dropdownMenu);
						groupElement.append(itemElement);
						smallElementsGroupElement = null;
						
					} else if (itemModel.type == "smallButton") {
						var itemElement = $("<div>").html(getItemLabel(itemModel)).addClass("ribbonSmallElement").addClass("ribbonSmallButton");
						itemElement.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
						itemElement.click(menuController.ribbonSelected);
						if (smallElementsGroupElement) {
							smallElementsGroupElement.append(itemElement);
							smallElementsGroupElement = null;
						} else {
							smallElementsGroupElement = $("<div>").addClass("ribbonBigElement").addClass("smallElementsGroup");
							itemElement.addClass("ribbonSmallTopElement");
							smallElementsGroupElement.append(itemElement);
							groupElement.append(smallElementsGroupElement);
						}
						
					} else if (itemModel.type == "smallButtonDropdown") {
						var itemElement = $("<div>").addClass("ribbonSmallElement").addClass("ribbonSmallButtonDropdown");
						
						var wrapper = $("<table cellspacing=0 cellpadding=0>").addClass("ribbonSmallButtonDropdownWrapper");
						itemElement.append(wrapper);
						var wrapperRow = $("<tr>").addClass("ribbonSmallButtonDropdownWrapperRow");
						wrapper.append(wrapperRow);
						
							var itemElementButton = $("<td>").html(getItemLabel(itemModel)).addClass("smallButtonDropdownMainButton");
							itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
							wrapperRow.append(itemElementButton);
							var itemElementDropdownButton = $("<td>").addClass("smallButtonDropdownBottomButton").addClass("dropdownTrigger");
								itemElementDropdownButton.append($("<img src='css/images/downArrow2.png'>"));
								wrapperRow.append(itemElementDropdownButton);
							var dropdownMenu = $("<ul>").addClass("smallButtonDropdownMenu").addClass("dropdownMenu");
							for (var menuIter in itemModel.items) {
								var menuItemName = itemModel.items[menuIter].name;
								var menuItem = $("<li>").html(getItemLabel(itemModel.items[menuIter]));
								menuItem.bind('click', {itemName: itemModel.name+"."+menuItemName, clickAction: itemModel.items[menuIter].clickAction}, buttonClicked);
								dropdownMenu.append(menuItem);
							}
							itemElement.append(dropdownMenu);
						if (smallElementsGroupElement) {
							smallElementsGroupElement.append(itemElement);
							smallElementsGroupElement = null;
						} else {
							smallElementsGroupElement = $("<div>").addClass("ribbonBigElement").addClass("smallElementsGroup");
							itemElement.addClass("ribbonSmallTopElement");
							smallElementsGroupElement.append(itemElement);
							groupElement.append(smallElementsGroupElement);
						}
					}
					
				}
				element.append(groupElement);
			}
		} else {
			var group1 = $("<div>").addClass("ribbonGroup");
				var gotoButton =  $("<button>").html(ribbonModel.name+' - go').addClass("ribbonBigElement").addClass("ribbonBigButton");
				gotoButton.click(menuController.ribbonSelected);
				group1.append(gotoButton);
			element.append(group1);
		}
		
		$(".dropdownTrigger").click(function() {
			var parent = $(this).parent();
			if (!parent.find(".dropdownMenu").is("ul")) {
				parent = parent.parent().parent().parent();
			}
			if (parent.find(".dropdownMenu").is(":visible")) {
				parent.find(".dropdownMenu").slideUp('fast');
			} else {
				parent.find(".dropdownMenu").slideDown('fast').show();
				parent.hover(function() {}, function(){  
					parent.find(".dropdownMenu").slideUp('fast');
				});
			}
		});
	}
	
	function getItemLabel(itemModel) {
		return itemModel.label ? itemModel.label : itemModel.name;
	}
	
	function buttonClicked(e) {
		var action = e.data.clickAction;
		var name = e.data.itemName;
		QCD.info("clicked "+name+": "+action);
		
		if (action) {
			action = $.trim(action).toLowerCase();
			if ("gotoview:" == action.substring(0,9)) {
				var viewName = action.substring(9);
				QCD.info("GOTO: "+viewName);
			}
			
		}
	}
	
}