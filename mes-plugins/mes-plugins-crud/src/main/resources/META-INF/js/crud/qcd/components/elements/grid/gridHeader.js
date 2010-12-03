/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.grid = QCD.components.elements.grid || {};

QCD.components.elements.grid.GridHeaderController = function(_gridController, _mainController, _gridParameters, _messagesPath) {
	
	var gridController = _gridController;
	var mainController = _mainController;
	var gridParameters = _gridParameters;
	var messagesPath = _messagesPath;
	
	var pagingVars = new Object();
	pagingVars.first = null;
	pagingVars.max = null;
	pagingVars.totalNumberOfEntities = null;
	
	var headerElement;
	var footerElement;
	
	var headerElements = new Object();
	headerElements.filterButton = null;
	headerElements.newButton = null;
	headerElements.deleteButton = null;
	headerElements.upButton = null;
	headerElements.downButton = null;
	
	var headerPagingController = null;
	var footerPagingController = null;
	
	var entitiesNumberSpan;
	
	var enabled = false;
	var rowIndex = null;
	
	function constructor(_this) {
		pagingVars.first = 0;
		pagingVars.max = 30;
		pagingVars.totalNumberOfEntities = 0;
	}
	
	function paging_refresh() {
		if (gridParameters.paging && enabled) {
			var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
			if (pagesNo == 0) {
				pagesNo = 1;
			}
			var currPage = Math.ceil(pagingVars.first / pagingVars.max);
			if (pagingVars.first % pagingVars.max == 0) {
				currPage += 1;
			}
			headerPagingController.setPageData(currPage, pagesNo, pagingVars.max);
			footerPagingController.setPageData(currPage, pagesNo, pagingVars.max);
			if (currPage > 1) {
				headerPagingController.enablePreviousButtons();
				footerPagingController.enablePreviousButtons();
			} else {
				headerPagingController.disablePreviousButtons();
				footerPagingController.disablePreviousButtons();
			}
			if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
				headerPagingController.enableNextButtons();
				footerPagingController.enableNextButtons();
			} else {
				headerPagingController.disableNextButtons();
				footerPagingController.disableNextButtons();
			}
			headerPagingController.enableRecordsNoSelect();
			footerPagingController.enableRecordsNoSelect();
			headerPagingController.enableInput();
			footerPagingController.enableInput();
		}
	}
		
	this.paging_prev = function() {
		pagingVars.first -= pagingVars.max;
		if (pagingVars.first < 0) {
			pagingVars.first = 0;
		}
		onPagingEvent();
	}

	this.paging_next = function() {
		pagingVars.first += pagingVars.max;
		onPagingEvent();
	}
	
	this.paging_first = function() {
		pagingVars.first = 0;
		onPagingEvent();
	}

	this.paging_last = function() {
		if (pagingVars.totalNumberOfEntities % pagingVars.max > 0) {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.totalNumberOfEntities % pagingVars.max;
		} else {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.max;
		}
		onPagingEvent();
	}

	this.paging_onRecordsNoSelectChange = function(recordsNoSelectElement) {
		var recordsNoSelectValue = recordsNoSelectElement.val();
		pagingVars.max = parseInt(recordsNoSelectValue);
		pagingVars.first = 0;
		onPagingEvent();
	}
	
	this.paging_setPageNo = function(pageNoElement) {
		var pageNoValue = pageNoElement.val();
		if (! pageNoValue || $.trim(pageNoValue) == "") {
			pageNoElement.addClass("inputError");
			return;
		}
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
		pagingVars.first = pagingVars.max * (intValue - 1);
		onPagingEvent();
	}
	
	function onPagingEvent() {
		headerPagingController.hideInputError();
		footerPagingController.hideInputError();
		gridController.onPagingParametersChange();
	}
	
	this.getPagingParameters = function() {
		return [pagingVars.first, pagingVars.max];
	}
	
	this.updatePagingParameters = function(_first, _max, _totalNumberOfEntities) {
		if (_first > _totalNumberOfEntities) {
			pagingVars.first = 0;
			gridController.onPagingParametersChange();
		} else {
			pagingVars.first = _first;
		}
		pagingVars.max = _max;
		pagingVars.totalNumberOfEntities = _totalNumberOfEntities;
		entitiesNumberSpan.html("("+pagingVars.totalNumberOfEntities+")");
		paging_refresh();
	}
	
	this.getHeaderElement = function() {
		headerElement = $("<div>").addClass('grid_header').addClass("elementHeader").addClass("elementHeaderDisabled");
		headerElement.append($("<span>").html(mainController.getTranslation(messagesPath + ".header")).addClass('grid_header_gridName').addClass('elementHeaderTitle'));
		entitiesNumberSpan = $("<span>").html("(0)").addClass('grid_header_totalNumberOfEntities').addClass('elementHeaderTitle');
		headerElement.append(entitiesNumberSpan);
		if (gridParameters.filter) {
			headerElements.filterButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + ".addFilter"), function(e) {
				if (headerElements.filterButton.hasClass("headerButtonEnabled")) {
					filterClicked();
				}
			}, "filterIcon16_dis.png");
			headerElement.append(headerElements.filterButton);
			setEnabledButton(headerElements.filterButton, false);
		}
		if (gridParameters.canNew) {
			headerElements.newButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + ".new"),function(e) {
				if (headerElements.newButton.hasClass("headerButtonEnabled")) {
					gridController.onNewButtonClicked();
				}
			}, "newIcon16_dis.png");
			headerElement.append(headerElements.newButton);
			setEnabledButton(headerElements.newButton, false);
		}
		if (gridParameters.canDelete) {
			headerElements.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + ".delete"), function(e) {
				if (headerElements.deleteButton.hasClass("headerButtonEnabled")) {
					gridController.onDeleteButtonClicked();
				}
			}, "deleteIcon16_dis.png");
			headerElement.append(headerElements.deleteButton);
			setEnabledButton(headerElements.deleteButton, false);
		}
		if (gridParameters.orderable) {
			headerElements.upButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + ".up"),function(e) {
				if (headerElements.upButton.hasClass("headerButtonEnabled")) {
					gridController.onUpButtonClicked();
				}
			}, "upIcon16_dis.png");
			headerElement.append(headerElements.upButton);
			setEnabledButton(headerElements.upButton, false);
			headerElements.downButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + ".down"), function(e) {
				if (headerElements.downButton.hasClass("headerButtonEnabled")) {
					gridController.onDownButtonClicked();
				}
			}, "downIcon16_dis.png");
			headerElement.append(headerElements.downButton);
			setEnabledButton(headerElements.downButton, false);
		}
		if (gridParameters.paging) {
			headerPagingController = new QCD.components.elements.grid.GridPagingElement(this, mainController, messagesPath);
			headerElement.append(headerPagingController.getPagingElement(pagingVars));
		}
		return headerElement;
	}
	
	this.getFooterElement = function() {
		if (!gridParameters.paging) {
			return null;
		}
		footerPagingController = new QCD.components.elements.grid.GridPagingElement(this, mainController, messagesPath);
		footerElement = $("<div>").addClass('grid_footer').append(footerPagingController.getPagingElement(pagingVars)); 
		return footerElement;
	}
	
	this.setEnabled = function(_enabled) {
		enabled = _enabled;
		if (enabled) {
			headerElement.removeClass("elementHeaderDisabled");
			if (footerElement) {
				footerElement.removeClass("elementHeaderDisabled");
			}
		} else {
			headerElement.addClass("elementHeaderDisabled");
			if (footerElement) {
				footerElement.addClass("elementHeaderDisabled");
			}
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
			if (gridParameters.paging) {
				headerPagingController.disablePreviousButtons();
				footerPagingController.disablePreviousButtons();
				headerPagingController.disableNextButtons();
				footerPagingController.disableNextButtons();
				headerPagingController.disableRecordsNoSelect();
				footerPagingController.disableRecordsNoSelect();
				headerPagingController.disableInput();
				footerPagingController.disableInput();
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
			} 
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
	
	function filterClicked() {
		if (headerElements.filterButton.hasClass("headerButtonActive")) {
			headerElements.filterButton.removeClass("headerButtonActive");
			headerElements.filterButton.label.html(mainController.getTranslation(messagesPath + ".addFilter"));
		} else {
			headerElements.filterButton.addClass("headerButtonActive");
			headerElements.filterButton.label.html(mainController.getTranslation(messagesPath + ".removeFilter"));
		}
		gridController.onFilterButtonClicked();
	}
	
	this.setFilterActive = function() {
		headerElements.filterButton.addClass("headerButtonActive");
		headerElements.filterButton.label.html(mainController.getTranslation(messagesPath + ".removeFilter"));
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


QCD.components.elements.grid.GridPagingElement = function(_gridHeaderController, _mainController, _messagesPath) {
	
	var gridHeaderController = _gridHeaderController;
	var mainController = _mainController;
	var messagesPath = _messagesPath;
	
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
	
	this.getPagingElement = function(pagingVars) {
		var pagingDiv = $("<div>").addClass('grid_paging');
		var onPageSpan = $("<span>").html(mainController.getTranslation(messagesPath + ".perPage")).addClass('onPageSpan');
		pagingDiv.append(onPageSpan);
		pagingElements.recordsNoSelect = $("<select>").addClass('recordsNoSelect');
			pagingElements.recordsNoSelect.append("<option value=10>10</option>");
			pagingElements.recordsNoSelect.append("<option value=20>20</option>");
			pagingElements.recordsNoSelect.append("<option value=30>30</option>");
			pagingElements.recordsNoSelect.append("<option value=50>50</option>");
			pagingElements.recordsNoSelect.append("<option value=100>100</option>");
			pagingElements.recordsNoSelect.val(pagingVars.max);
		pagingDiv.append(pagingElements.recordsNoSelect);
		
		pagingElements.firstButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_first");
		pagingDiv.append(pagingElements.firstButton);
		
		pagingElements.prevButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_left");
		pagingDiv.append(pagingElements.prevButton);

		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
		
//		<div class="component_container_form_w">
//			<div class="component_container_form_inner">
//				<div class="component_container_form_x"></div>
//				<div class="component_container_form_y"></div>
//				<input type='text' id="usernameInput" name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/>
//			</div>
//		</div>
		
			pagingElements.pageNo = $("<input type='text'></input>").addClass('pageInput');
				var component_container_form_inner = $("<div>").addClass("component_container_form_inner");
				component_container_form_inner.append('<div class="component_container_form_x"></div>');
				component_container_form_inner.append('<div class="component_container_form_y"></div>');
				component_container_form_inner.append(pagingElements.pageNo.val(currPage));
				var component_container_form_w = $("<div>").addClass('component_container_form_w').append(component_container_form_inner);
			pageInfoSpan.append(component_container_form_w);
			var ofPagesInfoSpan = $("<span>").addClass("ofPagesSpan");
			ofPagesInfoSpan.append('<span> z </span>'); // TODO masz translate
			pagingElements.allPagesNoSpan = $("<span>");
			ofPagesInfoSpan.append(pagingElements.allPagesNoSpan.html(pagesNo));
			pageInfoSpan.append(ofPagesInfoSpan);
		pagingDiv.append(pageInfoSpan);
	
		pagingElements.nextButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_right");
		pagingDiv.append(pagingElements.nextButton);
		pagingElements.lastButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_last");;
		pagingDiv.append(pagingElements.lastButton);
		
		pagingElements.firstButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_first();
			}
		});
		pagingElements.prevButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_prev();
			}
		});

		pagingElements.recordsNoSelect.change(function(e) {
			gridHeaderController.paging_onRecordsNoSelectChange($(this));
		});
		pagingElements.pageNo.change(function(e) {
			gridHeaderController.paging_setPageNo($(this));
		});
		
		pagingElements.nextButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_next();
			}
		});
		pagingElements.lastButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_last();
			}
		});
		
		gridHeaderController.setEnabledButton(pagingElements.prevButton, false);
		gridHeaderController.setEnabledButton(pagingElements.firstButton, false);
		gridHeaderController.setEnabledButton(pagingElements.nextButton, false);
		gridHeaderController.setEnabledButton(pagingElements.lastButton, false);
		
		return pagingDiv;
	}
	
	this.setPageData = function(currPage, pagesNo, max) {
		pagingElements.allPagesNoSpan.html(pagesNo);
		pagingElements.pageNo.val(currPage);
		pagingElements.recordsNoSelect.val(max);
	}
	
	this.enablePreviousButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.prevButton, true);
		gridHeaderController.setEnabledButton(pagingElements.firstButton, true);
	}
	this.disablePreviousButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.prevButton, false);
		gridHeaderController.setEnabledButton(pagingElements.firstButton, false);
	}
	this.enableNextButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.nextButton, true);
		gridHeaderController.setEnabledButton(pagingElements.lastButton, true);
	}
	this.disableNextButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.nextButton, false);
		gridHeaderController.setEnabledButton(pagingElements.lastButton, false);
	}
	this.enableRecordsNoSelect = function() {
		pagingElements.recordsNoSelect.attr("disabled", false);
	}
	this.disableRecordsNoSelect = function() {
		pagingElements.recordsNoSelect.attr("disabled", true);
	}
	this.enableInput = function() {
		pagingElements.pageNo.attr("disabled", false);
	}
	this.disableInput = function() {
		pagingElements.pageNo.attr("disabled", true);
	}
	
	this.showInputError = function() {
		pagingElements.pageNo.addClass("inputError");
	}
	this.hideInputError = function() {
		pagingElements.pageNo.removeClass("inputError");
	}
	
	constructor();
}