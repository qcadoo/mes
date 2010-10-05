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
		pagingVars.max = 30;
		pagingVars.totalNumberOfEntities = 1000;
		header = new QCD.components.elements.grid.GridHeaderElement(this)
		footer = new QCD.components.elements.grid.GridHeaderElement(this)
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
		var pagingElements = header.getPagingElements();
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
	
	this.getPagingParameters = function() {
		return pagingVars;
	}
	
	this.updatePagingParameters = function(_pagingVars) {
		pagingVars.first = _pagingVars.first;
		pagingVars.max = _pagingVars.max;
		pagingVars.totalNumberOfEntities = _pagingVars.totalNumberOfEntities;
	}
	
	this.getHeaderElement = function() {
		return $("<div>").addClass('grid_header').append(header.getHeaderElement(pagingVars));
	}
	
	this.getFooterElement = function() {
		return $("<div>").addClass('grid_footer').append(footer.getHeaderElement(pagingVars));
	}

	constructor();
}


QCD.components.elements.grid.GridHeaderElement = function(_gridHeader) {
	
	var gridHeader = _gridHeader;
	
	var pagingElements = new Object();
	pagingElements.prevButton = null;
	pagingElements.nextButton = null;
	pagingElements.firstButton = null;
	pagingElements.lastButton = null;
	pagingElements.recordsNoSelect = null;
	pagingElements.pageNo = null;
	pagingElements.allPagesNoSpan = null;
	
	function constructor() {
	}
	
	this.getPagingElements = function() {
		return pagingElements;
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
			pagingElements.recordsNoSelect.val(pagingVars.max);
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
		pagingElements.pageNo = currPage;
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
			pagingValueInput.val(pagingElements.pageNo);
			pageInfoSpan.append(pagingValueInput);
			pageInfoSpan.append('<span>z</span>');
			pagingElements.allPagesNoSpan = $("<span>");
			pageInfoSpan.append(pagingElements.allPagesNoSpan.html(pagesNo));
		pagingDiv.append(pageInfoSpan);
	
		pagingElements.nextButton =  $("<button>").html("next");
		pagingDiv.append(pagingElements.nextButton);
		pagingElements.lastButton =  $("<button>").html("last");
		pagingDiv.append(pagingElements.lastButton);
		
		pagingElements.firstButton.click(gridHeader.paging_first);
		pagingElements.prevButton.click(gridHeader.paging_prev);
		pagingElements.recordsNoSelect.change(gridHeader.paging_onRecordsNoSelectChange);
		pagingElements.nextButton.click(gridHeader.paging_next);
		pagingElements.lastButton.click(gridHeader.paging_last);
		if (pagingVars.first > 0) {
			pagingElements.prevButton.attr("disabled", false);
			pagingElements.firstButton.attr("disabled", false);
		} else {
			pagingElements.prevButton.attr("disabled", true);
			pagingElements.firstButton.attr("disabled", true);
		}
		if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
			pagingElements.nextButton.attr("disabled", false);
			pagingElements.lastButton.attr("disabled", false);
		} else {
			pagingElements.nextButton.attr("disabled", true);
			pagingElements.lastButton.attr("disabled", true);
		}
		pagingElements.recordsNoSelect.attr("disabled", false);

		return pagingDiv;
	}
	
	constructor();
}