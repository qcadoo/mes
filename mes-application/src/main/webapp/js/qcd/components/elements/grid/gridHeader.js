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
	
	var headerElement;
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
				setEnabledButton(header.getPagingElements().prevButton, true);
				setEnabledButton(header.getPagingElements().firstButton, true);
				setEnabledButton(footer.getPagingElements().prevButton, true);
				setEnabledButton(footer.getPagingElements().firstButton, true);
			} else {
				setEnabledButton(header.getPagingElements().prevButton, false);
				setEnabledButton(header.getPagingElements().firstButton, false);
				setEnabledButton(footer.getPagingElements().prevButton, false);
				setEnabledButton(footer.getPagingElements().firstButton, false);
			}
			if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
				setEnabledButton(header.getPagingElements().nextButton, true);
				setEnabledButton(header.getPagingElements().lastButton, true);
				setEnabledButton(footer.getPagingElements().nextButton, true);
				setEnabledButton(footer.getPagingElements().lastButton, true);
			} else {
				setEnabledButton(header.getPagingElements().nextButton, false);
				setEnabledButton(header.getPagingElements().lastButton, false);
				setEnabledButton(footer.getPagingElements().nextButton, false);
				setEnabledButton(footer.getPagingElements().lastButton, false);
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
		headerElement = $("<div>").addClass('grid_header').addClass("elementHeader").addClass("elementHeaderDisabled");
		headerElement.append($("<span>").html(gridName).addClass('grid_header_gridName').addClass('elementHeaderTitle'));
		entitiesNumberSpan = $("<span>").html("(0)").addClass('grid_header_totalNumberOfEntities').addClass('elementHeaderTitle');
		headerElement.append(entitiesNumberSpan);
		if (gridParameters.filter) {
			headerElements.filterButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("Filtruj",function(e) {
				if (headerElements.filterButton.hasClass("headerButtonEnabled")) {
					filterClicked();
				}
			}, "searchIcon16.png");
			headerElement.append(headerElements.filterButton);
			setEnabledButton(headerElements.filterButton, false);
		}
		if (gridParameters.canNew) {
			headerElements.newButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("new",function(e) {
				if (headerElements.newButton.hasClass("headerButtonEnabled")) {
					gridController.onNewButtonClicked();
				}
			}, "addIcon16.png");
			headerElement.append(headerElements.newButton);
			setEnabledButton(headerElements.newButton, false);
		}
		if (gridParameters.canDelete) {
			headerElements.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("delete", function(e) {
				if (headerElements.deleteButton.hasClass("headerButtonEnabled")) {
					gridController.onDeleteButtonClicked();
				}
			}, "deleteIcon16_disabled.png");
			headerElement.append(headerElements.deleteButton);
			setEnabledButton(headerElements.deleteButton, false);
		}
		if (gridParameters.orderable) {
			headerElements.upButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("up",function(e) {
				if (headerElements.upButton.hasClass("headerButtonEnabled")) {
					gridController.onDownButtonClicked();
				}
			}, "upIcon16.png");
			headerElement.append(headerElements.upButton);
			setEnabledButton(headerElements.upButton, false);
			headerElements.downButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("down", function(e) {
				if (headerElements.downButton.hasClass("headerButtonEnabled")) {
					gridController.onDownButtonClicked();
				}
			}, "downIcon16.png");
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
		if (enabled) {
			headerElement.removeClass("elementHeaderDisabled");
		} else {
			headerElement.addClass("elementHeaderDisabled");
		}
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
		if (headerElements.filterButton.hasClass("headerButtonActive")) {
			headerElements.filterButton.removeClass("headerButtonActive");
		} else {
			headerElements.filterButton.addClass("headerButtonActive");
		}
		gridController.onFilterButtonClicked();
	}

	this.setEnabledButton = function(button, enabled) {
		if (enabled) {
			button.addClass("headerButtonEnabled");
		} else {
			button.removeClass("headerButtonEnabled");
		}		
	} 
	var setEnabledButton = this.setEnabledButton;
	
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
		
		pagingElements.firstButton =  $("<div>").addClass("headerButton").addClass("headerButton_first");
		pagingDiv.append(pagingElements.firstButton);
		
		pagingElements.prevButton =  $("<div>").addClass("headerButton").addClass("headerButton_left");
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
	
		pagingElements.nextButton =  $("<div>").addClass("headerButton").addClass("headerButton_right");
		pagingDiv.append(pagingElements.nextButton);
		pagingElements.lastButton =  $("<div>").addClass("headerButton").addClass("headerButton_last");;
		pagingDiv.append(pagingElements.lastButton);
		
		pagingElements.firstButton.click(function(e) {
			if (!$(e.target).hasClass("headerButtonDisabled")) {
				gridHeader.paging_first();
			}
		});
		pagingElements.prevButton.click(function(e) {
			if (!$(e.target).hasClass("headerButtonDisabled")) {
				gridHeader.paging_prev();
			}
		});

		pagingElements.recordsNoSelect.change(function(e) {
			gridHeader.paging_onRecordsNoSelectChange($(this));
		});
		pagingElements.pageNo.change(function(e) {
			gridHeader.paging_setPageNo($(this));
		});
		
		
		pagingElements.nextButton.click(function(e) {
			if (!$(e.target).hasClass("headerButtonDisabled")) {
				gridHeader.paging_next();
			}
		});
		pagingElements.lastButton.click(function(e) {
			if (!$(e.target).hasClass("headerButtonDisabled")) {
				gridHeader.paging_last();
			}
		});
		if (pagingVars.first > 0) {
			gridHeader.setEnabledButton(pagingElements.prevButton, true);
			gridHeader.setEnabledButton(pagingElements.firstButton, true);
		} else {
			gridHeader.setEnabledButton(pagingElements.prevButton, false);
			gridHeader.setEnabledButton(pagingElements.firstButton, false);
		}
		if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
			gridHeader.setEnabledButton(pagingElements.nextButton, true);
			gridHeader.setEnabledButton(pagingElements.lastButton, true);
		} else {
			gridHeader.setEnabledButton(pagingElements.nextButton, false);
			gridHeader.setEnabledButton(pagingElements.lastButton, false);
		}
		pagingElements.recordsNoSelect.attr("disabled", false);

		return pagingDiv;
	}
	
	constructor();
}