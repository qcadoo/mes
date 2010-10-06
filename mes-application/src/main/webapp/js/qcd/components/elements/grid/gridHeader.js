var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.grid = QCD.components.elements.grid || {};

QCD.components.elements.grid.GridHeader = function(_gridController, _gridName, _gridParameters) {
	
	var gridController = _gridController;
	var gridName = _gridName;
	var gridParameters = _gridParameters;
	
	var pagingVars = new Object();
	pagingVars.first = null;
	pagingVars.max = null;
	pagingVars.totalNumberOfEntities = null;
	
	var headerElements = new Object();
	headerElements.filtrButton = null;
	headerElements.newButton = null;
	headerElements.deleteButton = null;
	headerElements.upButton = null;
	headerElements.downButton = null;
	
	var header = null;
	var footer = null;
	
	var entitiesNumberSpan;
	
	function constructor(_this) {
		pagingVars.first = 0;
		pagingVars.max = 30;
		pagingVars.totalNumberOfEntities = 0;
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
		var currPage = Math.ceil(pagingVars.first / pagingVars.max);
		if (pagingVars.first % pagingVars.max == 0) {
			currPage += 1;
		}
		header.getPagingElements().pageNo.val(currPage);
		footer.getPagingElements().pageNo.val(currPage);
		header.getPagingElements().recordsNoSelect.val(pagingVars.max);
		footer.getPagingElements().recordsNoSelect.val(pagingVars.max);
		
		entitiesNumberSpan.html("("+pagingVars.totalNumberOfEntities+")");
	}
		
	this.paging_prev = function() {
		pagingVars.first -= pagingVars.max;
		if (pagingVars.first < 0) {
			pagingVars.first = 0;
		}
		header.getPagingElements().pageNo.removeClass("inputError");
		footer.getPagingElements().pageNo.removeClass("inputError");
		gridController.onPagingParametersChange();
	}

	this.paging_next = function() {
		pagingVars.first += pagingVars.max;
		header.getPagingElements().pageNo.removeClass("inputError");
		footer.getPagingElements().pageNo.removeClass("inputError");
		gridController.onPagingParametersChange();
	}
	
	this.paging_first = function() {
		pagingVars.first = 0;
		header.getPagingElements().pageNo.removeClass("inputError");
		footer.getPagingElements().pageNo.removeClass("inputError");
		gridController.onPagingParametersChange();
	}

	this.paging_last = function() {
		if (pagingVars.totalNumberOfEntities % pagingVars.max > 0) {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.totalNumberOfEntities % pagingVars.max;
		} else {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.max;
		}
		header.getPagingElements().pageNo.removeClass("inputError");
		footer.getPagingElements().pageNo.removeClass("inputError");
		gridController.onPagingParametersChange();
	}

	this.paging_onRecordsNoSelectChange = function(recordsNoSelectElement) {
		var recordsNoSelectValue = recordsNoSelectElement.val();
		pagingVars.max = parseInt(recordsNoSelectValue);
		header.getPagingElements().pageNo.removeClass("inputError");
		footer.getPagingElements().pageNo.removeClass("inputError");
		gridController.onPagingParametersChange();
	}
	
	this.paging_setPageNo = function(pageNoElement) {
		var pageNoValue = pageNoElement.val();
		if (! /^\d*$/.test(pageNoValue)) {
			pageNoElement.addClass("inputError");
			return;
		}
		var intValue = parseInt(pageNoValue);
		if (intValue <= 0) {
			pageNoElement.addClass("inputError");
			return;
		}
		if (intValue > Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max)) {
			pageNoElement.addClass("inputError");
			return;
		}
		pagingVars.first = pagingVars.max * (pageNoValue - 1);
		header.getPagingElements().pageNo.removeClass("inputError");
		footer.getPagingElements().pageNo.removeClass("inputError");
		gridController.onPagingParametersChange();
	}
	
	this.getPagingParameters = function() {
		return pagingVars;
	}
	
	this.updatePagingParameters = function(_pagingVars, _totalNumberOfEntities) {
		pagingVars.first = _pagingVars.first;
		pagingVars.max = _pagingVars.max;
		pagingVars.totalNumberOfEntities = _totalNumberOfEntities;
		paging_refresh();
	}
	
	this.getHeaderElement = function() {
		var headerElement = $("<div>").addClass('grid_header');
		headerElement.append($("<span>").html(gridName).addClass('grid_header_gridName'));
		entitiesNumberSpan = $("<span>").html("(0)").addClass('grid_header_totalNumberOfEntities');
		headerElement.append(entitiesNumberSpan);
		if (gridParameters.canFiltr) {
			headerElements.filtrButton = $("<button>").html("filtr").click(gridController.onFilterButtonClicked);
			headerElement.append(headerElements.filtrButton);
		}
		if (gridParameters.canNew) {
			headerElements.newButton = $("<button>").html("new").click(gridController.onNewButtonClicked);
			headerElement.append(headerElements.newButton);
		}
		if (gridParameters.canDelete) {
			headerElements.deleteButton = $("<button>").html("delete").click(gridController.onDeleteButtonClicked);
			headerElement.append(headerElements.deleteButton);
		}
		if (gridParameters.canUp) {
			headerElements.upButton = $("<button>").html("up").click(gridController.onUpButtonClicked);
			headerElement.append(headerElements.upButton);
		}
		if (gridParameters.canDown) {
			headerElements.downButton = $("<button>").html("down").click(gridController.onDownButtonClicked);
			headerElement.append(headerElements.downButton);
		}
		if (gridParameters.paging) {
			headerElement.append(header.getHeaderElement(pagingVars));
		}
		return headerElement;
	}
	
	this.getFooterElement = function() {
		if (!gridParameters.paging) {
			return null;
		}
		return $("<div>").addClass('grid_footer').append(footer.getHeaderElement(pagingVars));
	}
	
	this.setEnabled = function(enabled) {
		if (headerElements.filtrButton != null) {
			headerElements.filtrButton.attr("disabled", !enabled);
		}
		if (headerElements.newButton != null) {
			headerElements.newButton.attr("disabled", !enabled);
		}
		if (headerElements.deleteButton != null) {
			headerElements.deleteButton.attr("disabled", !enabled);
		}
		if (headerElements.upButton != null) {
			headerElements.upButton.attr("disabled", !enabled);
		}
		if (headerElements.downButton != null) {
			headerElements.downButton.attr("disabled", !enabled);
		}
	}
	
	this.onRowClicked = function(rowIndex) {
		//paging
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		if (headerElements.upButton != null) {
			if (rowIndex == 1 && currPage == 1) {
				headerElements.upButton.attr("disabled", true);
			} else {
				headerElements.upButton.attr("disabled", false);
			}
		}
		if (headerElements.downButton != null) {
			if (rowIndex == pagingVars.totalNumberOfEntities % pagingVars.max && currPage == pagesNo) {
				headerElements.downButton.attr("disabled", true);	
			} else {
				headerElements.downButton.attr("disabled", false);
			}
		}
		if (headerElements.deleteButton != null) {
			headerElements.deleteButton.attr("disabled", false);
		}
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

		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
			pagingElements.pageNo = $("<input type='text'></input>").addClass('pageInput');
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

		pagingElements.recordsNoSelect.change(function(e) {
			gridHeader.paging_onRecordsNoSelectChange($(this));
		});
		pagingElements.pageNo.change(function(e) {
			gridHeader.paging_setPageNo($(this));
		});
		
		
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