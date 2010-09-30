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
		for (var i in model.items) {
			var item = model.items[i];
			var firstLevelButton = $("<div>").html(item.label).addClass("firstLevelButton").attr("id", "firstLevelButton_"+item.name);
			var firstLevelButtonWrapper =  $("<div>").addClass("firstLevelButtonWrapper").append(firstLevelButton);
			firstLevelElement.append(firstLevelButtonWrapper);
			item.element = firstLevelButton;
			
			firstLevelButton.hover(
				function() {
					onTopItemOver($(this));
				},
				function() {
					onTopItemOut($(this));
				}
			);
			firstLevelButton.click(function() {
				onTopItemClick($(this));
			});
		}
		previousActive.first = model.selectedItem;
		model.selectedItem.element.addClass("previousActive");
		previousActive.second = model.selectedItem.selectedItem;
		
		updateState();
		
		changePage(model.selectedItem.selectedItem.page);
	}
	
	function onTopItemOver(itemElement) {
		itemElement.addClass("hover");
	}
	
	function onTopItemOut(itemElement) {
		itemElement.removeClass("hover");
	}
	
	function onBottomItemOver(itemElement) {
		itemElement.addClass("hover");
	}
	
	function onBottomItemOut(itemElement) {
		itemElement.removeClass("hover");
	}
	
	function onTopItemClick(itemElement) {
		var buttonName = itemElement.attr("id").substring(17);
		var topItem = model.itemsMap[buttonName];
		
		model.selectedItem = topItem;
		if (model.selectedItem.selectedItem) {
			currentActive.second = model.selectedItem.selectedItem
		}
		
		updateState();
	}
	
	function onBottomItemClick(itemElement) {
		var buttonName = itemElement.attr("id").substring(18);
		var selectedItem = model.selectedItem.itemsMap[buttonName];
		
		model.selectedItem.selectedItem = selectedItem;
		
		previousActive.first.element.removeClass("previousActive");
		if (previousActive.second) {
			previousActive.second.element.removeClass("previousActive");
		}
		previousActive.first = model.selectedItem;
		previousActive.second = model.selectedItem.selectedItem;
		previousActive.first.element.addClass("previousActive");
		if (previousActive.second) {
			previousActive.second.element.addClass("previousActive");
		}
		
		updateState();
		
		changePage(model.selectedItem.selectedItem.page);
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
				currentActive.first.element.removeClass("active");
			}
			currentActive.first = model.selectedItem;
			currentActive.first.element.addClass("active");
			
			if (model.selectedItem.items.length > 0) {
				updateSecondLevel();
			} else {
				secondLevelElement.hide();
				//ribbon.setRibbon(model.selectedItem.ribbon);
			}
			
		} else {
			if (currentActive.second != model.selectedItem.selectedItem) {
				if (currentActive.second) {
					currentActive.second.element.removeClass("active");
				}
				currentActive.second = model.selectedItem.selectedItem;
				if (currentActive.second) {
					currentActive.second.element.addClass("active");
					//ribbon.setRibbon(model.selectedItem.selectedItem.ribbon);
				} else {
					//ribbon.setRibbon(model.selectedItem.ribbon);
				}
			}
		}
		
	}
	
	function updateSecondLevel() {
		if (model.selectedItem.items.length > 0) {
			secondLevelElement.children().remove();
			for (var i in model.selectedItem.items) {
				var secondLevelItem = model.selectedItem.items[i];
				var secondLevelButton = $("<div>").html(secondLevelItem.label).addClass("secondLevelButton").attr("id", "secondLevelButton_"+secondLevelItem.name);
				var secondLevelButtonWrapper =  $("<div>").addClass("secondLevelButtonWrapper").append(secondLevelButton);
				secondLevelElement.append(secondLevelButtonWrapper);
				secondLevelItem.element = secondLevelButton;
				
				secondLevelButton.hover(
					function() {
						onBottomItemOver($(this));
					},
					function() {
						onBottomItemOut($(this));
					}
				);
				secondLevelButton.click(function() {
					onBottomItemClick($(this));
				});
				
				if (previousActive.second && previousActive.second.name == secondLevelItem.name) {
					//secondLevelItem.element.addClass("previousActive");
					secondLevelItem.element.addClass("active");
				}
				
			}
			secondLevelElement.show();
			//if (model.selectedItem.selectedItem) {
			//	model.selectedItem.selectedItem.element.addClass("active");	
			//}
			
			//currentActive.second = model.selectedItem.selectedItem;
			//ribbon.setRibbon(model.selectedItem.selectedItem.ribbon);
		} else {
			secondLevelElement.hide();
			//ribbon.setRibbon(model.selectedItem.ribbon);
		}
	} 
	
	function changePage(page) {
		QCD.info(page);
		windowController.onMenuClicked(page);
	}
	
	constructor(menuStructure);
}
