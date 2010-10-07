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
	headerElements.filterButton = null;
	headerElements.newButton = null;
	headerElements.deleteButton = null;
	headerElements.upButton = null;
	headerElements.downButton = null;
	
	var header = null;
	var footer = null;
	
	var entitiesNumberSpan;
	
	var enabled = false;
	var rowIndex = null;
	
	function constructor(_this) {
		pagingVars.first = 0;
		pagingVars.max = 30;
		pagingVars.totalNumberOfEntities = 0;
		header = new QCD.components.elements.grid.GridHeaderElement(_this)
		footer = new QCD.components.elements.grid.GridHeaderElement(_this)
	}
	
	function paging_refresh() {
		if (gridParameters.paging) {
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
			if (currPage > 1) {
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
		}
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
		if (_pagingVars.first > _totalNumberOfEntities) {
			pagingVars.first = 0;
			gridController.onPagingParametersChange();
		} else {
			pagingVars.first = _pagingVars.first;
		}
		pagingVars.max = _pagingVars.max;
		pagingVars.totalNumberOfEntities = _totalNumberOfEntities;
		paging_refresh();
	}
	
	this.getHeaderElement = function() {
		var headerElement = $("<div>").addClass('grid_header');
		headerElement.append($("<span>").html(gridName).addClass('grid_header_gridName'));
		entitiesNumberSpan = $("<span>").html("(0)").addClass('grid_header_totalNumberOfEntities');
		headerElement.append(entitiesNumberSpan);
		if (gridParameters.filter) {
			headerElements.filterButton = $("<div>").html("Filtruj").addClass("headerButton").click(function(e) {
				if (!$(e.target).hasClass("headerButtonDisabled")) {
					gridController.onFilterButtonClicked();
				}
			});
			headerElement.append(headerElements.filterButton);
			setEnabledButton(headerElements.filterButton, false);
		}
		if (gridParameters.canNew) {
			headerElements.newButton = $("<div>").html("Nowy").addClass("headerButton").click(function(e) {
				if (!$(e.target).hasClass("headerButtonDisabled")) {
					gridController.onNewButtonClicked();
				}
			});
			headerElement.append(headerElements.newButton);
			setEnabledButton(headerElements.newButton, false);
		}
		if (gridParameters.canDelete) {
			headerElements.deleteButton = $("<div>").html("Usuń").addClass("headerButton").click(function(e) {
				if (!$(e.target).hasClass("headerButtonDisabled")) {
					gridController.onDeleteButtonClicked();
				}
			});
			headerElement.append(headerElements.deleteButton);
			setEnabledButton(headerElements.deleteButton, false);
		}
		if (gridParameters.orderable) {
			headerElements.upButton = $("<div>").html("Góra").addClass("headerButton").click(function(e) {
				if (!$(e.target).hasClass("headerButtonDisabled")) {
					gridController.onUpButtonClicked();
				}
			});
			headerElement.append(headerElements.upButton);
			setEnabledButton(headerElements.upButton, false);
			headerElements.downButton = $("<div>").html("Dół").addClass("headerButton").click(function(e) {
				if (!$(e.target).hasClass("headerButtonDisabled")) {
					gridController.onDownButtonClicked();
				}
			});
			headerElement.append(headerElements.downButton);
			setEnabledButton(headerElements.downButton, false);
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
	
	this.setEnabled = function(_enabled) {
		enabled = _enabled;
		refreshButtons();
	}
	
	this.onRowClicked = function(_rowIndex) {
		rowIndex = _rowIndex;
		refreshButtons();
	}
	
	function refreshButtons() {
		if (!enabled) {
			if (headerElements.filterButton != null) {
				setEnabledButton(headerElements.filterButton, false);
			}
			if (headerElements.newButton != null) {
				setEnabledButton(headerElements.newButton, false);
			}
			if (headerElements.deleteButton != null) {
				setEnabledButton(headerElements.deleteButton, false);
			} 
			if (headerElements.upButton != null) {
				setEnabledButton(headerElements.upButton, false);
			}
			if (headerElements.downButton != null) {
				setEnabledButton(headerElements.downButton, false);
			}
		} else {
			if (headerElements.filterButton != null) {
				setEnabledButton(headerElements.filterButton, true);
			}
			if (headerElements.newButton != null) {
				setEnabledButton(headerElements.newButton, true);
			}
			if (headerElements.deleteButton != null) {
				if (rowIndex != null) {
					setEnabledButton(headerElements.deleteButton, true);
				} else {
					setEnabledButton(headerElements.deleteButton, false);
				}
			}
			if (gridParameters.paging) {
				var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
				var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
				if (pagesNo == 0) {
					pagesNo = 1;
				}
				if (headerElements.upButton != null) {
					if (rowIndex == 1 && currPage == 1 || rowIndex == null) {
						setEnabledButton(headerElements.upButton, false);
					} else {
						setEnabledButton(headerElements.upButton, true);
					}
				}
				if (headerElements.downButton != null) {
					if (rowIndex == pagingVars.totalNumberOfEntities % pagingVars.max && currPage == pagesNo || rowIndex == null) {
						setEnabledButton(headerElements.downButton, false);
					} else {
						setEnabledButton(headerElements.downButton, true);
					}
				}
			} else {
				if (headerElements.upButton != null) {
					if (rowIndex == 1 || rowIndex == null) {
						setEnabledButton(headerElements.upButton, false);
					} else {
						setEnabledButton(headerElements.upButton, true);
					}
				}
				if (headerElements.downButton != null) {
					if (rowIndex == pagingVars.totalNumberOfEntities || rowIndex == null) {	
						setEnabledButton(headerElements.downButton, false);
					} else {
						setEnabledButton(headerElements.downButton, true);
					}
				}
			}
		}
		
	}
	
	function filterClicked() {
		gridController.onFilterButtonClicked();
	}

	function setEnabledButton(button, enabled) {
		if (enabled) {
			button.removeClass("headerButtonDisabled");
		} else {
			button.addClass("headerButtonDisabled");
		}		
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
		
		pagingElements.firstButton =  $("<div>").html("<<").addClass("headerButton");
		pagingDiv.append(pagingElements.firstButton);
		
		pagingElements.prevButton =  $("<div>").html("<").addClass("headerButton");
		pagingDiv.append(pagingElements.prevButton);

		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
			pagingElements.pageNo = $("<input type='text'></input>").addClass('pageInput');
			pageInfoSpan.append(pagingElements.pageNo.val(currPage));
			pageInfoSpan.append('<span> z </span>');
			pagingElements.allPagesNoSpan = $("<span>");
			pageInfoSpan.append(pagingElements.allPagesNoSpan.html(pagesNo));
		pagingDiv.append(pageInfoSpan);
	
		pagingElements.nextButton =  $("<div>").html(">").addClass("headerButton");
		pagingDiv.append(pagingElements.nextButton);
		pagingElements.lastButton =  $("<div>").html(">>").addClass("headerButton");
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