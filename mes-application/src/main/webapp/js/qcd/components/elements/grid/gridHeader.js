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
	
	function constructor(_this) {
		pagingVars.first = 0;
		pagingVars.max = 30;
		pagingVars.totalNumberOfEntities = 200;
		header = new QCD.components.elements.grid.GridHeaderElement(_this)
		footer = new QCD.components.elements.grid.GridHeaderElement(_this)
	}
	
	function paging_refresh() {
		header.getPagingElements().pageNo.val(pagingVars.first);
		footer.getPagingElements().pageNo.val(pagingVars.first);
		if (pagingVars.first > 0) {
			header.getPagingElements().prevButton.attr("disabled", false);
			header.getPagingElements().firstButton.attr("disabled", false);
			footer.getPagingElements().prevButton.attr("disabled", false);
			footer.getPagingElements().firstButton.attr("disabled", false);
		} else {
			header.getPagingElements().prevButton.attr("disabled", true);
			header.getPagingElements().firstButton.attr("disabled", true);
			footer.getPagingElements().prevButton.attr("disabled", true);
			footer.getPagingElements().firstButton.attr("disabled", true);
		}
		if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
			header.getPagingElements().nextButton.attr("disabled", false);
			header.getPagingElements().lastButton.attr("disabled", false);
			footer.getPagingElements().nextButton.attr("disabled", false);
			footer.getPagingElements().lastButton.attr("disabled", false);
			
		} else {
			header.getPagingElements().nextButton.attr("disabled", true);
			header.getPagingElements().lastButton.attr("disabled", true);
			footer.getPagingElements().nextButton.attr("disabled", true);
			footer.getPagingElements().lastButton.attr("disabled", true);
		}
		header.getPagingElements().recordsNoSelect.attr("disabled", false);
		footer.getPagingElements().recordsNoSelect.attr("disabled", false);
		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		header.getPagingElements().allPagesNoSpan.html(pagesNo);
		footer.getPagingElements().allPagesNoSpan.html(pagesNo);
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		header.getPagingElements().pageNo.val(currPage);
		footer.getPagingElements().pageNo.val(currPage);
		header.getPagingElements().recordsNoSelect.val(pagingVars.max);
		footer.getPagingElements().recordsNoSelect.val(pagingVars.max);
	}
		
	this.paging_prev = function() {
		pagingVars.first -= pagingVars.max;
		if (pagingVars.first < 0) {
			pagingVars.first = 0;
		}
		paging_refresh();
	}

	this.paging_next = function() {
		pagingVars.first += pagingVars.max;
		paging_refresh();
	}
	
	this.paging_first = function() {
		pagingVars.first = 0;
		paging_refresh();
	}

	this.paging_last = function() {
		if (pagingVars.totalNumberOfEntities % pagingVars.max > 0) {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.totalNumberOfEntities % pagingVars.max;
		} else {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.max;
		}
		QCD.info(pagingVars.first);
		paging_refresh();
	}

	this.paging_onRecordsNoSelectChangeHeader = function() {
		//pagingVars.first = 0;
		pagingVars.max = parseInt(header.getPagingElements().recordsNoSelect.val());
		paging_refresh();
	}
	
	this.paging_onRecordsNoSelectChangeFooter = function() {
		//pagingVars.first = 0;
		pagingVars.max = parseInt(footer.getPagingElements().recordsNoSelect.val());
		paging_refresh();
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
		var headerElement = $("<div>").addClass('grid_header');
		var filterButton = $("<button>").html("filtr").click(filterClicked);
		headerElement.append($("<span>").html("GRID"));
		headerElement.append(filterButton);
		headerElement.append(header.getHeaderElement(pagingVars, true));
		return headerElement;
	}
	
	this.getFooterElement = function() {
		return $("<div>").addClass('grid_footer').append(footer.getHeaderElement(pagingVars, false));
	}
	
	function filterClicked() {
		gridController.onFilterButtonClicked();
	}

	constructor(this);
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
	
	this.getHeaderElement = function(pagingVars, isHeader) {
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

		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
			pagingElements.pageNo = $("<input type='text'></input>");
			pageInfoSpan.append(pagingElements.pageNo.val(currPage));
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
		if(isHeader){
			pagingElements.recordsNoSelect.change(gridHeader.paging_onRecordsNoSelectChangeHeader);
		} else {
			pagingElements.recordsNoSelect.change(gridHeader.paging_onRecordsNoSelectChangeFooter);
		}
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