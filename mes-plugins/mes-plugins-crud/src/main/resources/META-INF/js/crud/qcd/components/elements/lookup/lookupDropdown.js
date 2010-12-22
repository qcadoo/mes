
var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.lookup = QCD.components.elements.lookup || {};

QCD.components.elements.lookup.Dropdown = function(_lookupDropdownElement, _controller, _translations) {
	
	var MAX_RESULTS = 25;
	var PAGE_RESULT = 4;
	var RESULT_HEIGHT = 25;
	
	var lookupDropdownElement = _lookupDropdownElement;
	
	var controller = _controller;
	
	var translations = _translations;
	
	var selectedElement;
	
	var mouseSelectedElement;
	
	var autocompleteMatches;
	
	function constructor() {
		lookupDropdownElement.css("top", "21px");
	}
	
	this.updateAutocomplete = function(_autocompleteMatches, _autocompleteEntitiesNumber) {
		
		autocompleteMatches = _autocompleteMatches;
		
		selectedElement = null;
		
		lookupDropdownElement.children().remove();
		lookupDropdownElement.scrollTop(0);
		
		if (_autocompleteEntitiesNumber > MAX_RESULTS) {
			var noRecordsElement = $("<div>").addClass("lookupMatch_noRecords").html(translations.tooManyResultsInfo+" ("+_autocompleteEntitiesNumber+")");
			lookupDropdownElement.append(noRecordsElement);
			lookupDropdownElement.css("height", (RESULT_HEIGHT-1)+"px");
		} else if (autocompleteMatches.length == 0) {
			var noRecordsElement = $("<div>").addClass("lookupMatch_noRecords").html(translations.noResultsInfo);
			lookupDropdownElement.append(noRecordsElement);
			lookupDropdownElement.css("height", (RESULT_HEIGHT-1)+"px");
		} else {
			if (autocompleteMatches.length > PAGE_RESULT) {
				lookupDropdownElement.css("height", (RESULT_HEIGHT*PAGE_RESULT-1)+"px");
			} else {
				lookupDropdownElement.css("height", (autocompleteMatches.length*RESULT_HEIGHT-1)+"px");
				lookupDropdownElement.css("overflow", "hidden");
			}
			for (var i in autocompleteMatches) {
				var entity = autocompleteMatches[i];
				var matchElement = $("<div>").addClass("lookupMatch").html(entity.value).attr("id", controller.elementPath+"_autocompleteOption_"+i);
				
				matchElement.mouseover(function() {
					$(this).addClass("lookupMatchHover");
					mouseSelectedElement = $(this);
				});
				matchElement.mouseout(function() {
					$(this).removeClass("lookupMatchHover");
					mouseSelectedElement = null;
				});
				matchElement.click(function() {
					// do nothing, blur will perform action 
				});
				
				lookupDropdownElement.append(matchElement);
			}
		}
	}
	
	this.selectNext = function() {
		if (! selectedElement) {
			nextElement = $(lookupDropdownElement.children()[0]);
		} else {
			var nextElement = selectedElement.next();
		}
		if (! nextElement || nextElement.length == 0) {
			return;
		}
		if (selectedElement) {
			selectedElement.removeClass("lookupMatchHover");	
		}
		selectedElement = nextElement;
		selectedElement.addClass("lookupMatchHover");
		if (selectedElement.position().top < 0) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top);
		}
		if (selectedElement.position().top >= 100) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top - 75);
		}
	}
	
	this.selectPrevious = function() {
		if (! selectedElement) {
			nextElement = $(lookupDropdownElement.children()[lookupDropdownElement.children().length - 1]);
		} else {
			var nextElement = selectedElement.prev();
		}
		if (! nextElement || nextElement.length == 0) {
			return;
		}
		if (selectedElement) {
			selectedElement.removeClass("lookupMatchHover");	
		}
		selectedElement = nextElement;
		selectedElement.addClass("lookupMatchHover");
		if (selectedElement.position().top < 0) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top);
		}
		if (selectedElement.position().top >= 100) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top - 75);
		}
	}
	
	this.getSelected = function() {
		if (selectedElement) {
			var id = selectedElement.attr("id").substring((controller.elementPath+"_autocompleteOption_").length);
			return autocompleteMatches[id];
		}
		return null;
	}
	
	this.getMouseSelected = function() {
		if (mouseSelectedElement) {
			var id = mouseSelectedElement.attr("id").substring((controller.elementPath+"_autocompleteOption_").length);
			return autocompleteMatches[id];
		}
		return null;
	}
	
	this.hide = function() {
		//lookupDropdownElement.hide();
		lookupDropdownElement.slideUp(400);
	}
	
	this.show = function() {
		//lookupDropdownElement.show();
		lookupDropdownElement.slideDown(400);
	}
	
	this.isOpen = function() {
		return lookupDropdownElement.is(':visible');
	}
	
	constructor();
}