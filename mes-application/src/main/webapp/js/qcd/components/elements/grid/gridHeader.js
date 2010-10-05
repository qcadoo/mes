var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.grid = QCD.components.elements.grid || {};

QCD.components.elements.grid.GridHeader = function(_gridController) {
	
	var gridController = _gridController;
	// gridController.onPagingParametersChange();
	
	var pagingVars = new Object();
	pagingVars.first = null;
	pagingVars.max = null;
	pagingVars.totalNumberOfEntities = null;
	
	var header = null;
	var footer = null;
	
	function constructor() {
		pagingVars.first = 0;
		pagingVars.max = 10;
		pagingVars.totalNumberOfEntities = 0;
		header = new QCD.components.elements.grid.GridHeaderElement(pagingVars)
		footer = new QCD.components.elements.grid.GridHeaderElement(pagingVars)
	}
	

	

	this.getPagingParameters = function() {
		
		return {
			first : 0,
			max : 10
		}
	}
	
	this.updatePagingParameters = function(first, max, totalNumberOfEntities) {
		pagingVars.first = first;
		pagingVars.max = max;
		pagingVars.totalNumberOfEntities = totalNumberOfEntities;
	}
	
	this.getHeaderElement = function() {
		var headerElement = $("<div>").addClass('grid_header');
		var filterButton = $("<button>").html("filtr").click(filterClicked);
		headerElement.append($("<span>").html("GRID"));
		headerElement.append(filterButton);
		headerElement.append(header.getHeaderElement(pagingVars));
		return headerElement;
	}
	
	this.getFooterElement = function() {
		return $("<div>").addClass('grid_footer').append(footer.getHeaderElement(pagingVars));
	}
	
	function filterClicked() {
		gridController.onFilterButtonClicked();
	}

	constructor();
}


QCD.components.elements.grid.GridHeaderElement = function() {
	
	var pagingElements = new Object();
	pagingElements.prevButton = null;
	pagingElements.nextButton = null;
	pagingElements.firstButton = null;
	pagingElements.lastButton = null;
	pagingElements.recordsNoSelect = null;
	pagingElements.pageNoSpan = null;
	pagingElements.allPagesNoSpan = null;
	
	function constructor() {
	}
	
	function paging_prev() {
		pagingVars.first -= pagingVars.max;
		if (pagingVars.first < 0) {
			pagingVars.first = 0;
		}
		// gridController.onPagingParametersChange();
	}

	function paging_next() {
		pagingVars.first += pagingVars.max;
		// gridController.onPagingParametersChange();
	}

	function paging_onRecordsNoSelectChange() {
		pagingVars.max = parseInt(pagingElements.recordsNoSelect.val());
		// gridController.onPagingParametersChange();
	}
	
	function paging_first() {
		pagingVars.first = 0;
		// gridController.onPagingParametersChange();
	}

	function paging_last() {
		pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.totalNumberOfEntities % pagingVars.max;
		// gridController.onPagingParametersChange();
	}
	
	this.getHeaderElement = function(pagingVars) {
		
		var pagingDiv = $("<div>").addClass('grid_paging');
		pagingDiv.append('<span>Na stronie: </span>');
		pagingElements.recordsNoSelect = $("<select>");
			pagingElements.recordsNoSelect.append("<option value=10>10</option>");
			pagingElements.recordsNoSelect.append("<option value=20>20</option>");
			pagingElements.recordsNoSelect.append("<option value=30>30</option>");
			pagingElements.recordsNoSelect.append("<option value=50>50</option>");
			pagingElements.recordsNoSelect.append("<option value=100>100</option>");
		pagingDiv.append(pagingElements.recordsNoSelect);
		
		pagingElements.firstButton =  $("<button>").html("first");
		pagingDiv.append(pagingElements.firstButton);
		
		pagingElements.prevButton =  $("<button>").html("prev");
		pagingDiv.append(pagingElements.prevButton);
		
		var pagingValueInput = $("<input type='text'></input>");
		
		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		QCD.info(currPage);
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
			pagingElements.pageNoSpan = $("<span>");
			pagingValueInput.val(currPage);
			pageInfoSpan.append(pagingValueInput);
			pageInfoSpan.append('<span>z</span>');
			pagingElements.allPagesNoSpan = $("<span>");
			pageInfoSpan.append(pagingElements.allPagesNoSpan.html(pagesNo));
		pagingDiv.append(pageInfoSpan);
	
		pagingElements.nextButton =  $("<button>").html("next");
		pagingDiv.append(pagingElements.nextButton);
		pagingElements.lastButton =  $("<button>").html("last");
		pagingDiv.append(pagingElements.lastButton);
		
		pagingElements.firstButton.click(function() {paging_first();});
		pagingElements.prevButton.click(function() {paging_prev();});
		pagingElements.recordsNoSelect.change(function() {paging_onRecordsNoSelectChange();});
		pagingElements.nextButton.click(function() {paging_next();});
		pagingElements.lastButton.click(function() {paging_last();});
		if (pagingVars.first > 0) {
			pagingElements.prevButton.attr("disabled", false);
			pagingElements.firstButton.attr("disabled", false);
		}
		if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
			pagingElements.nextButton.attr("disabled", false);
			pagingElements.lastButton.attr("disabled", false);
		}
		pagingElements.recordsNoSelect.attr("disabled", false);

		
		return pagingDiv;
	}
	
	constructor();
}