var QCD = QCD || {};
QCD.elements = QCD.elements || {};

QCD.elements.GridElement = function(args, _mainController) {
	
	// PRIVATE
	
	var mainController;
	
	var gridParameters;
	
	var grid;
	
	var pagingVars = new Object();
		pagingVars.first = null;
		pagingVars.max = null;
		pagingVars.totalNumberOfEntities = null;
	
	var sortVars = new Object();
		sortVars.column = null;
		sortVars.order = null;
		
	var filterArray = null;
		
	var pagingElements = new Object();
		pagingElements.prevButton = null;
		pagingElements.nextButton = null;
		pagingElements.recordsNoSelect = null;
		pagingElements.pageNoSpan = null;
		pagingElements.allPagesNoSpan = null;
		
	var sortElements = new Object();
		sortElements.columnChooser = null;
		sortElements.orderChooser = null;
	
	var filterElements = new Object();
		filterElements.filterDiv = null;
		
	var navigationButtons = new Object();
		navigationButtons.newButton = null;
		navigationButtons.deleteButton = null;
	
	var parentId = null;
	
	var children = new Array();
	
	var dataFromSerialization = null;
	
	var currentRefreshId = 0;
	var rowsToSelect = null;
	
	var defaultOptions = {
		paging: true,
	};
	
	function parseOptions(opts) {
		if (!opts.element) {
			throw("no element definied");
		}
		for (var opName in defaultOptions) {
			if (opts[opName] == undefined) {
				opts[opName] = defaultOptions[opName];
			}
		}
		return opts;
	}
	
	
	function paging_prev() {
		pagingVars.first -= pagingVars.max;
		if (pagingVars.first < 0) {
			pagingVars.first = 0;
		}
		refresh();
	}

	function paging_next() {
		pagingVars.first += pagingVars.max;
		refresh();
	}

	function paging_onRecordsNoSelectChange() {
		pagingVars.max = parseInt(pagingElements.recordsNoSelect.val());
		refresh();
	}
	
	function updateSortElements() {
		if (sortElements.columnChooser && sortElements.orderChooser) {
			sortElements.columnChooser.val(sortVars.column);
			sortElements.orderChooser.val(sortVars.order);
		}
	}
	
	function updateSortVars() {
		if (sortElements.columnChooser && sortElements.orderChooser) {
			sortVars.column = sortElements.columnChooser.val();
			sortVars.order = sortElements.orderChooser.val();
		}
	}
	
	function performSort() {
		updateSortVars();
		refresh();
	}
	
	function createFilterRow(rowObject) {
		var filterDiv = $("<div>").addClass('qcdGrid_filterButtons_row');
		var filterDivFieldChooser = $("<select>");
			for (var i in gridParameters.fields) {
				var fieldName = gridParameters.fields[i];
				filterDivFieldChooser.append("<option value='"+fieldName+"'>"+fieldName+"</option>");
			}
			filterDiv.append(filterDivFieldChooser);
		var operatorChooser = $("<select>");
			operatorChooser.append("<option selected='selected' value='='>=</option>");
			operatorChooser.append("<option value='<'><</option>");
			operatorChooser.append("<option value='>'>></option>");
			operatorChooser.append("<option value='<='><=</option>");
			operatorChooser.append("<option value='>='>>=</option>");
			operatorChooser.append("<option value='<>'><></option>");
			operatorChooser.append("<option value='null'>"+mainController.getTranslation("commons.grid.button.filter.null")+"</option>");
			operatorChooser.append("<option value='not null'>"+mainController.getTranslation("commons.grid.button.filter.notNull")+"</option>");
			filterDiv.append(operatorChooser);
		var filterValueInput = $("<input type='text'></input>");
			filterDiv.append(filterValueInput);
		var removeRowButton =  $("<button>").html('-');
			removeRowButton.bind("click", {row: filterDiv}, function(event) {event.data.row.remove(); });
			filterDiv.append(removeRowButton);
		filterDiv.append("<br/>");
		var addRowButton =  $("<button>").html('+');
			addRowButton.bind("click", {prevRow: filterDiv}, function(event) {addFilterButtonClicked(event.data.prevRow);});
			filterDiv.append(addRowButton);
		operatorChooser.bind("change", {operatorChooser: operatorChooser, filterValueInput: filterValueInput},
					function(event) {onFilterOperatorChange(event.data.operatorChooser, event.data.filterValueInput);});
		if (rowObject) {
			filterDivFieldChooser.val(rowObject.fieldName);
			operatorChooser.val(rowObject.operator);
			filterValueInput.val(rowObject.filterValue);
		}
		return filterDiv;
	}
	
	function addFilterButtonClicked(prevRow) {
		prevRow.after(createFilterRow());
	}
	
	function onFilterOperatorChange(operatorChooser, filterValueInput) {
		if (operatorChooser.val() == "null" || operatorChooser.val() == "not null") {
			filterValueInput.hide();
		} else {
			filterValueInput.show();
		}
	}
	
	function updateFiltersVars() {
		if (filterElements.filterDiv) {
			for (var i in filterArray) {
				filterElements.filterDiv.append(createFilterRow(filterArray[i]));
			}
		}
	}
	
	function updateFiltersArray() {
		var newFilterArray = new Array();
		if (filterElements.filterDiv) {
			filterElements.filterDiv.children().each(function() {
				if ($(this).children().length > 3) {
					var rowObject = new Object();
					rowObject.fieldName = $($(this).children().get(0)).val();
					rowObject.operator = $($(this).children().get(1)).val();
					rowObject.filterValue = $($(this).children().get(2)).val().trim();
					//if (!(rowObject.operator != "null" && rowObject.operator != "not null" && rowObject.filterValue == '')) {
						newFilterArray.push(rowObject);
					//}
				}
			});
		}
		filterArray = newFilterArray;
	}
	
	function performFilter() {
		updateFiltersArray();
		refresh();
	}
	
	function onDiselect() {
		if (navigationButtons.deleteButton)
			navigationButtons.deleteButton.attr("disabled", true);
		for (var i in children) {
			children[i].insertParentId(null);
		}
	}
	
	function rowClicked(rowId) {
		for (var i in children) {
			children[i].insertParentId(rowId);
		}
		if (navigationButtons.deleteButton) {
			if (gridParameters.multiselect) {
				selectedRows = grid.getGridParam("selarrrow");
				if (selectedRows.length > 0 ) {
					navigationButtons.deleteButton.attr("disabled", false);
				} else {
					navigationButtons.deleteButton.attr("disabled", true);
				}
			} else {
				navigationButtons.deleteButton.attr("disabled", false);
			}
		}
	}
	
	function rowDblClicked(rowId) {
		redirectToCorrespondingPage(rowId);
	}
	
	function newClicked() {
		redirectToCorrespondingPage();
	}
	
	function deleteClicked() {
		deleteSelectedRecords();
	}
	
	function redirectToCorrespondingPage(rowId) {
		if (gridParameters.correspondingViewName && gridParameters.correspondingViewName != '') {
			var url = gridParameters.correspondingViewName + ".html";
			if (parentId || rowId) {
				url += "?";
				if (parentId) {
					url += "contextEntityId="+parentId;
				}
				if (rowId) {
					if (parentId) {
						url += "&";
					}
					url += "entityId="+rowId;
				}
			}
			
			if (gridParameters.isCorrespondingViewModal) {
				QCDLogger.info("modal: "+url);
			} else {
				QCDLogger.info("not modal: "+url);
				//window.location = url;
				mainController.goToPage(url);
			}
		}
	}
	
	function enable() {
		if (navigationButtons.newButton)
			navigationButtons.newButton.attr("disabled", false);
		//navigationButtons.deleteButton.attr("disabled", false);
	}
	
	function disable() {
		grid.jqGrid('clearGridData');
		if (navigationButtons.newButton)
			navigationButtons.newButton.attr("disabled", true);
		if (navigationButtons.deleteButton)
			navigationButtons.deleteButton.attr("disabled", true);
	}
	
	function refresh() {
		grid.jqGrid('clearGridData');
		blockList();
		var parameters = new Object();
		if (pagingVars.max) {
			parameters.maxResults = pagingVars.max;
		}
		if (pagingVars.first != null) {
			parameters.firstResult = pagingVars.first;
		}
		if (sortVars.column && sortVars.order) {
			parameters.sortColumn = sortVars.column;
			parameters.sortOrder = sortVars.order;
		}
		if (filterArray && filterArray.length > 0) {
			parameters.filterObject = filterArray;
		}
		if (parentId) {
			parameters.entityId = parentId;
		}
		currentRefreshId++;
		var thisRefreshId = currentRefreshId;
		$.ajax({
			url: gridParameters.viewName+"/"+gridParameters.viewElementName+"/list.html",
			type: 'GET',
			data: parameters,
			dataType: 'json',
			data: parameters,
			contentType: 'application/json; charset=utf-8',
			success: function(response) {
				if (response) {
					if (thisRefreshId != currentRefreshId) {
						return;
					}
					pagingVars.totalNumberOfEntities = response.totalNumberOfEntities;
					for (var entityNo in response.entities) {
						var entity = response.entities[entityNo];
						grid.jqGrid('addRowData',entity.id,entity.fields);
					}
					if (rowsToSelect) {
						setSelectedRows(rowsToSelect);
						rowsToSelect = null;
					} else {
						onDiselect();
					}
					unblockList();
				}
			},
			error: function(xhr, textStatus, errorThrown){
				alert(textStatus);
				unblockList();
			}

		});
	}
	
	function blockList() {
		grid.block({ message: mainController.getTranslation("commons.loading.gridLoading"), showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: '#000', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: .5, 
            color: '#fff' } });
		if (gridParameters.paging) {
			pagingElements.prevButton.attr("disabled", true);
			pagingElements.nextButton.attr("disabled", true);
			pagingElements.recordsNoSelect.attr("disabled", true);
		}
	}

	function unblockList() {
		grid.unblock();
		paging_refreshBottomButtons();
	}
	
	function paging_refreshBottomButtons() {
		if (gridParameters.paging) {
			if (pagingVars.first > 0) {
				pagingElements.prevButton.attr("disabled", false);
			}
			if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
				pagingElements.nextButton.attr("disabled", false);
			}
			pagingElements.recordsNoSelect.attr("disabled", false);
			var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
			if (pagesNo == 0) {
				pagesNo = 1;
			}
			var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
			pagingElements.pageNoSpan.html(currPage);
			pagingElements.allPagesNoSpan.html(pagesNo);
		}
	}
	
	function getSelectedRows() {
		var selectedRows;
		if (gridParameters.multiselect) {
			selectedRows = grid.getGridParam("selarrrow");
		} else {
			selectedRows = new Array();
			selectedRows.push(grid.getGridParam('selrow'));
		}
		return selectedRows;
	}
	
	function setSelectedRows(selectedRows) {
		if (gridParameters.multiselect) {
			for (var i in selectedRows) {
				grid.setSelection(selectedRows[i]);
			}
		}  else {
			grid.setSelection(selectedRows);
		}
	}
	
	function deleteSelectedRecords() {
		if (window.confirm(mainController.getTranslation("commons.confirm.deleteMessage"))) {
			blockList();
			
			var selectedRows = getSelectedRows();
			
			var dataArray = new Array();
			for (var i in selectedRows) {
				dataArray.push(parseInt(selectedRows[i]));
			}
			var dataString = JSON.stringify(dataArray);
			$.ajax({
				url: gridParameters.viewName+"/"+gridParameters.viewElementName+"/delete.html",
				type: 'POST',
				dataType: 'json',
				data: dataString,
				contentType: 'application/json; charset=utf-8',
				success: function(response) {
					refresh();
				},
				error: function(xhr, textStatus, errorThrown){
					alert(textStatus);
					unblockList();
				}

			});
		}
	}
	
	// CONSTRUCTOR
	
	function constructor(args, _mainController) {
		
		gridParameters = parseOptions(args);
		mainController = _mainController;
		
		var element = $("#"+gridParameters.element);
		
		var topButtonsDiv = $("<div>").addClass('qcdGrid_top');
			if (gridParameters.canNew) {
				navigationButtons.newButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.new"));
				navigationButtons.newButton.click(newClicked);
				navigationButtons.newButton.attr("disabled", true);
				topButtonsDiv.append(navigationButtons.newButton);
			}
			if (gridParameters.canDelete) {
				navigationButtons.deleteButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.delete"));
				navigationButtons.deleteButton.click(deleteClicked);
				navigationButtons.deleteButton.attr("disabled", true);
				topButtonsDiv.append(navigationButtons.deleteButton);
			}
		element.before(topButtonsDiv);
		
		if (gridParameters.sortable) {
			var topSortDiv = $("<div>").addClass('qcdGrid_sortButtons');
				sortElements.columnChooser = $("<select>");
					for (var i in gridParameters.colNames) {
						var colName = gridParameters.colNames[i];
						sortElements.columnChooser.append("<option value='"+colName+"'>"+colName+"</option>");
					}
					topSortDiv.append(sortElements.columnChooser);
				sortElements.orderChooser = $("<select>");
					sortElements.orderChooser.append("<option value='asc'>"+mainController.getTranslation("commons.grid.button.sort.asc")+"</option>");
					sortElements.orderChooser.append("<option value='desc'>"+mainController.getTranslation("commons.grid.button.sort.desc")+"</option>");
					topSortDiv.append(sortElements.orderChooser);
				var sortButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.sort"));
					sortButton.click(function() {performSort();});
					topSortDiv.append(sortButton);
			element.before(topSortDiv);
		}
		
		if (gridParameters.filter) {
			filterElements.filterDiv = $("<div>").addClass('qcdGrid_filterButtons');
				var firstRow = $("<div>").addClass('qcdGrid_filterButtons_row');
					var addFilterButton =  $("<button>").html('+');
					addFilterButton.bind("click", {prevRow: firstRow}, function(event) {addFilterButtonClicked(event.data.prevRow);});
					firstRow.append(addFilterButton);
				filterElements.filterDiv.append(firstRow);
			element.before(filterElements.filterDiv);
			var filterButtonDiv = $("<div>").addClass('qcdGrid_filterButtons');
				var performFilterButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.filter"));
				performFilterButton.click(function() {performFilter();});
				filterButtonDiv.append(performFilterButton);
			element.before(performFilterButton);
		}
		
		if (gridParameters.paging) {
			var pagingDiv = $("<div>").addClass('qcdGrid_paging');
				pagingElements.prevButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.prev"));
				pagingDiv.append(pagingElements.prevButton);
				
				pagingElements.recordsNoSelect = $("<select>");
					pagingElements.recordsNoSelect.append("<option value=10>10</option>");
					pagingElements.recordsNoSelect.append("<option value=20>20</option>");
					pagingElements.recordsNoSelect.append("<option value=50>50</option>");
					pagingElements.recordsNoSelect.append("<option value=100>100</option>");
				pagingDiv.append(pagingElements.recordsNoSelect);
				
				var pageInfoSpan = $("<span>").addClass('qcdGrid_paging_pageInfo');
					pageInfoSpan.append('<span>'+mainController.getTranslation("commons.grid.button.pageInfo")+'</span>');
					pagingElements.pageNoSpan = $("<span>");
					pageInfoSpan.append(pagingElements.pageNoSpan);
					pageInfoSpan.append('<span>/</span>');
					pagingElements.allPagesNoSpan = $("<span>");
					pageInfoSpan.append(pagingElements.allPagesNoSpan);
				pagingDiv.append(pageInfoSpan);
				
				pagingElements.nextButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.next"));
				pagingDiv.append(pagingElements.nextButton);
			
				pagingElements.prevButton.click(function() {paging_prev();});
				pagingElements.recordsNoSelect.change(function() {paging_onRecordsNoSelectChange();});
				pagingElements.nextButton.click(function() {paging_next(); });
				
			element.after(pagingDiv);
			
			pagingVars.first = 0;
			pagingVars.max = 10;
		}
		
		gridParameters.datatype = 'local';
		gridParameters.ondblClickRow = function(id){
			rowDblClicked(id);
        }
		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		grid = element.jqGrid(gridParameters);
		
		if (! gridParameters.parent) {
			enable();
			refresh();
		}
	}
	
	this.insertParentId = function(_parentId) {
		parentId = _parentId;
		if (_parentId) {
			enable();
			//if (! dataFromSerialization) {
				refresh();
			//}
		} else {
			disable();
		}
	}
	
	this.insertContext = function(contextEntityId) {
		
	}
	
	this.getParent = function() {
		return gridParameters.parent;
	}
	
	this.addChild = function(child) {
		children.push(child);
	}
	
	this.serialize = function() {
		updateSortVars();
		updateFiltersArray();
		var serializationObject = new Object();
		serializationObject.parentId = parentId;
		serializationObject.pagingVars = pagingVars;
		serializationObject.sortVars = sortVars;
		serializationObject.filterArray = filterArray;
		serializationObject.selectedRows = getSelectedRows();
		//QCDLogger.info(serializationObject);
		return serializationObject;
	}
	
	this.deserialize = function(serializationObject) {
		dataFromSerialization = serializationObject;
		parentId = serializationObject.parentId;
		pagingVars = serializationObject.pagingVars;
		sortVars = serializationObject.sortVars;
		filterArray = serializationObject.filterArray;
		rowsToSelect = serializationObject.selectedRows;
		updateSortElements();
		updateFiltersVars();
		refresh();
		//QCDLogger.info(serializationObject);
	}
	
	constructor(args, _mainController);
	
};
