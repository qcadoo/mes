var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Ribbon = function(_model, _elementName, _mainController) {
	
	var ribbonModel = _model;
	var mainController = _mainController;
	var elementName = _elementName;
	
	var element;
	
	this.constructElement = function() {
		
		element = $("<div>");
		
		var contentWrapper = $("<div>").attr("id", "q_row3_out");
		element.append(contentWrapper);
		element.append($("<div>").attr("id", "q_row4_out"));
		
		var content = $("<div>").attr("id", "q_menu_row3");
		contentWrapper.append(content);
		
		if (ribbonModel.groups) {
			for (var groupIter in ribbonModel.groups) {
				var groupModel = ribbonModel.groups[groupIter];
				
				var groupTraslation = mainController.getTranslation(mainController.getPluginIdentifier() + "." + mainController.getViewName() + "." + _elementName + ".ribbon." + groupModel.name);
				
				var groupContent = $("<div>").addClass("ribbon_content");
				var groupTitle = $("<div>").addClass("ribbon_title").html(groupTraslation);
				
				var ribbonMenu_right = $("<div>").addClass("ribbonMenu_right").append(groupTitle).append(groupContent);
				var ribbonMenu_left = $("<div>").addClass("ribbonMenu_left").append(ribbonMenu_right);
				var groupElement = $("<div>").addClass("ribbonMenu").append(ribbonMenu_left);
				
				var smallElementsGroupElement = null;
				for (var itemsIter in groupModel.items) {
					var itemModel = groupModel.items[itemsIter];
					
					var itemElement = null;
					var isSmall = false;
					
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
				}
				content.append(groupElement);
			}
		}
		return element;
	}
	
	function createBigButton(path, itemModel) {
		var aElement = $("<a>").attr('href','#').html("<span><div"+getItemIconStyle(itemModel)+"><label>"+getItemLabel(path, itemModel)+"</label><div></div></div></span>");
		var liElement = $("<li>").append(aElement);
		var ribbonListElement = $("<ul>").addClass("ribbonListElement").append(liElement);
		var itemElement = $("<div>").addClass("ribbonBigElement").append(ribbonListElement);
		aElement.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
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
		var itemElementButton = $("<a>").attr('href','#').html("<span><div class='"+className+" bigDropdownButtonDiv' "+style+"><label>"+getItemLabel(path, itemModel)+"</label><div></div></div></span>");
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
		var itemElementButton = $("<a>").attr('href','#').html("<span><div"+getItemIconStyle(itemModel)+">"+getItemLabel(path, itemModel)+"</div></span>");
		var itemElement = $("<li>").addClass("btnOne").append(itemElementButton);
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createSmallButtonWithDropdown(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div "+getItemIconStyle(itemModel)+">"+getItemLabel(path, itemModel)+"</div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').addClass("twoB_down");
		var buttonDropdownLi = $("<li>").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var itemElement = $("<li>").addClass("twoB").addClass("ribbonDropdownContainer").append(spanElement);
		
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		
		var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("smallButtonDropdownMenu");
		addDropdownAction(itemElementDropdownButton);
		itemElement.append(dropdownMenu);
			
		return itemElement;
	}
	
	function createDropdownMenu(path, itemModel) {
		var dropdownMenuContent = $("<ul>");
		for (var menuIter in itemModel.items) {
			var menuItemName = itemModel.items[menuIter].name;
			var icon = (itemModel.items[menuIter].icon && $.trim(itemModel.items[menuIter].icon) != "") ? $.trim(itemModel.items[menuIter].icon) : null;
			var style = "";
			if (icon) {
				style = " style=\"background-image:url(\'../../images/icons/"+icon+"\')\"";
			}
			var menuItemButton = $("<a>").attr('href','#').html("<span "+style+">"+getItemLabel(path, itemModel.items[menuIter])+"</span>").addClass("icon");
			menuItemButton.bind('click', {itemName: itemModel.name+"."+menuItemName, clickAction: itemModel.items[menuIter].clickAction}, buttonClicked);
			var menuItem = $("<li>").append(menuItemButton);
			dropdownMenuContent.append(menuItem);
		}
		var dropdownMenu = $("<div>").addClass("dropdownMenu").addClass("m_module").append(dropdownMenuContent);
		return dropdownMenu;
	}
	
	function addDropdownAction(dropdownTriggerButton) {
		dropdownTriggerButton.addClass("dropdownTrigger");
		dropdownTriggerButton.click(function() {
			var parent = $(this);
			parent.blur();
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
	
	function getItemLabel(path, itemModel) {
		return mainController.getTranslation(mainController.getPluginIdentifier() + "." + mainController.getViewName() + "." + _elementName + ".ribbon." + path + "." + (itemModel.label ? itemModel.label : itemModel.name));
	}
	function getItemIconStyle(itemModel) {
		var icon = (itemModel.icon && $.trim(itemModel.icon) != "") ? $.trim(itemModel.icon) : null;
		var style = "";
		if (icon) {
			style = " class='hasIcon' style=\"background-image:url(\'/img/core/icons/"+icon+"\')\"";
		}
		return style;
	}
	
	function buttonClicked(e) {
		$(this).blur();
		var action = e.data.clickAction;
		var name = e.data.itemName;
		mainController.performRibbonAction(action);
	}

	this.updateSize = function(margin, innerWidth) {
		$("#q_menu_row3").css("margin-left", (margin)+"px");
		$("#q_row4_out").width(innerWidth);
	}
	
}