var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Ribbon = function(_model, _mainController) {
	
	var ribbonModel = _model;
	var mainController = _mainController;
	
	this.constructElement = function() {
		var element = $("<div>").addClass("ribbonMenu");
		
		//QCD.info(ribbonModel);
		
		if (ribbonModel.groups) {
			for (var groupIter in ribbonModel.groups) {
				var groupModel = ribbonModel.groups[groupIter];
				var groupElement = $("<div>").addClass("ribbonGroup");
				
				//QCD.info(groupModel);
				
				var smallElementsGroupElement = null;
				for (var itemsIter in groupModel.items) {
					var itemModel = groupModel.items[itemsIter];
					
					var itemElement = null;
					var isSmall = false;
					
					//QCD.info(itemModel);
					
					if (itemModel.type == "BIG_BUTTON") {
						if (itemModel.items) {
							itemElement = createBigButtonWithDropdown(itemModel);
						} else {
							itemElement = createBigButton(itemModel);
						}
					} else if (itemModel.type == "SMALL_BUTTON") {
						if (itemModel.items) {
							itemElement = createSmallButtonWithDropdown(itemModel);
						} else {
							itemElement = createSmallButton(itemModel);
						}
						isSmall = true;
					}
					
					if (itemElement) {
						if (isSmall) {
							if (smallElementsGroupElement) {
								smallElementsGroupElement.append(itemElement);
								smallElementsGroupElement = null;
							} else {
								smallElementsGroupElement = $("<div>").addClass("ribbonBigElement").addClass("smallElementsGroup");
								itemElement.addClass("ribbonSmallTopElement");
								smallElementsGroupElement.append(itemElement);
								groupElement.append(smallElementsGroupElement);
							}
						} else {
							groupElement.append(itemElement);
							smallElementsGroupElement = null;
						}
					}
				}
				element.append(groupElement);
			}
		}
		
		return element;
	}
	
	function createBigButton(itemModel) {
		var itemElement = $("<div>").html(getItemLabel(itemModel)).addClass("ribbonBigElement").addClass("ribbonBigButton");
		itemElement.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createBigButtonWithDropdown(itemModel) {
		var itemElement = $("<div>").addClass("ribbonBigElement").addClass("bigButtonDropdown");
		var itemElementButton = $("<div>").html(getItemLabel(itemModel)).addClass("bigButtonDropdownMainButton");
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		itemElement.append(itemElementButton);
		var itemElementDropdownButton = $("<div>").addClass("bigButtonDropdownBottomButton");
			var arrow = $("<img src='../../css/images/downArrow.png'>");
			addDropdownAction(itemElementDropdownButton);
			itemElementDropdownButton.append(arrow);
		itemElement.append(itemElementDropdownButton);
		var dropdownMenu = createDropdownMenu(itemModel);
		dropdownMenu.addClass("bigButtonDropdownMenu");
		itemElement.append(dropdownMenu);
		itemElement.append(dropdownMenu);
		return itemElement;
	}
	
	function createSmallButton(itemModel) {
		var itemElement = $("<div>").html(getItemLabel(itemModel)).addClass("ribbonSmallElement").addClass("ribbonSmallButton");
		itemElement.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createSmallButtonWithDropdown(itemModel) {
		var itemElement = $("<div>").addClass("ribbonSmallElement").addClass("ribbonSmallButtonDropdown");
		
		var wrapper = $("<table cellspacing=0 cellpadding=0>").addClass("ribbonSmallButtonDropdownWrapper");
		itemElement.append(wrapper);
		var wrapperRow = $("<tr>").addClass("ribbonSmallButtonDropdownWrapperRow");
		wrapper.append(wrapperRow);
		
			var itemElementButton = $("<td>").html(getItemLabel(itemModel)).addClass("smallButtonDropdownMainButton");
			itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
			wrapperRow.append(itemElementButton);
			var itemElementDropdownButton = $("<td>").addClass("smallButtonDropdownBottomButton");
				itemElementDropdownButton.append($("<img src='../../css/images/downArrow2.png'>"));
				addDropdownAction(itemElementDropdownButton);
				wrapperRow.append(itemElementDropdownButton);
			
			var dropdownMenu = createDropdownMenu(itemModel);
			dropdownMenu.addClass("smallButtonDropdownMenu");
			itemElement.append(dropdownMenu);
		return itemElement;
	}
	
	function createDropdownMenu(itemModel) {
		var dropdownMenu = $("<ul>").addClass("dropdownMenu");
		for (var menuIter in itemModel.items) {
			var menuItemName = itemModel.items[menuIter].name;
			var menuItem = $("<li>").html(getItemLabel(itemModel.items[menuIter]));
			menuItem.bind('click', {itemName: itemModel.name+"."+menuItemName, clickAction: itemModel.items[menuIter].clickAction}, buttonClicked);
			dropdownMenu.append(menuItem);
		}
		return dropdownMenu;
	}
	
	function addDropdownAction(dropdownTriggerButton) {
		dropdownTriggerButton.addClass("dropdownTrigger");
		dropdownTriggerButton.click(function() {
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
		
		mainController.performRibbonAction(action);
	}

	
	
}