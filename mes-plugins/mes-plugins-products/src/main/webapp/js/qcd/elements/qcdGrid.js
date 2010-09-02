var QCD = QCD || {};
QCD.elements = QCD.elements || {};

QCD.elements.GridElement = function(args) {
	
	// PRIVATE
	
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
	
	
	var defaultOptions = {
		paging: true,
		deleteConfirmMessage: 'delete?'
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
	
	function performSort() {
		sortVars.column = sortElements.columnChooser.val();
		sortVars.order = sortElements.orderChooser.val();
		refresh();
	}
	
	function addFilterButtonClicked(prevRow) {
		var filterDiv = $("<div>").addClass('qcdGrid_filterButtons_row');
			var filterDivColumnChooser = $("<select>");
				for (var i in gridParameters.colNames) {
					var colName = gridParameters.colNames[i];
					filterDivColumnChooser.append("<option value='"+colName+"'>"+colName+"</option>");
				}
				filterDiv.append(filterDivColumnChooser);
			var operatorChooser = $("<select>");
				operatorChooser.append("<option selected='selected' value='='>=</option>");
				operatorChooser.append("<option value='<'><</option>");
				operatorChooser.append("<option value='>'>></option>");
				operatorChooser.append("<option value='<='><=</option>");
				operatorChooser.append("<option value='>='>>=</option>");
				operatorChooser.append("<option value='<>'><></option>");
				operatorChooser.append("<option value='null'>null</option>");
				operatorChooser.append("<option value='not null'>not null</option>");
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
		prevRow.after(filterDiv);
	}
	
	function onFilterOperatorChange(operatorChooser, filterValueInput) {
		if (operatorChooser.val() == "null" || operatorChooser.val() == "not null") {
			filterValueInput.hide();
		} else {
			filterValueInput.show();
		}
	}
	
	function performFilter() {
		var newFilterArray = new Array();
		filterElements.filterDiv.children().each(function() {
			if ($(this).children().length > 3) {
				var rowObject = new Object();
				rowObject.column = $($(this).children().get(0)).val();
				rowObject.operator = $($(this).children().get(1)).val();
				rowObject.filterValue = $($(this).children().get(2)).val().trim();
				if (!(rowObject.operator != "null" && rowObject.operator != "not null" && rowObject.filterValue == '')) {
					newFilterArray.push(rowObject);
				}
			}
		});
		filterArray = newFilterArray;
		refresh();
	}
	
	function onDiselect() {
		navigationButtons.deleteButton.attr("disabled", true);
		for (var i in children) {
			children[i].insertParentId(null);
		}
	}
	
	function rowClicked(rowId) {
		for (var i in children) {
			children[i].insertParentId(rowId);
		}
		if (gridParameters.multiselect) {
			selectedRows = grid.getGridParam("selarrrow");
			if (selectedRows.length > 0) {
				navigationButtons.deleteButton.attr("disabled", false);
			} else {
				navigationButtons.deleteButton.attr("disabled", true);
			}
		} else {
			navigationButtons.deleteButton.attr("disabled", false);
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
			window.location = url;
		}
	}
	
	function enable() {
		navigationButtons.newButton.attr("disabled", false);
		//navigationButtons.deleteButton.attr("disabled", false);
	}
	
	function disable() {
		grid.jqGrid('clearGridData');
		navigationButtons.newButton.attr("disabled", true);
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
		$.ajax({
			url: gridParameters.viewName+"/"+gridParameters.viewElementName+"/list.html",
			type: 'GET',
			data: parameters,
			dataType: 'json',
			data: parameters,
			contentType: 'application/json; charset=utf-8',
			success: function(response) {
				pagingVars.totalNumberOfEntities = response.totalNumberOfEntities;
				for (var entityNo in response.entities) {
					var entity = response.entities[entityNo];
					grid.jqGrid('addRowData',entity.id,entity.fields);
				}
				onDiselect();
				unblockList();
			},
			error: function(xhr, textStatus, errorThrown){
				alert(textStatus);
				unblockList();
			}

		});
	}
	
	function blockList() {
		grid.block({ message: gridParameters.loadingText, showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
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
	
	function deleteSelectedRecords() {
		if (window.confirm(gridParameters.deleteConfirmMessage)) {
			blockList();
			var selectedRows;
			if (gridParameters.multiselect) {
				selectedRows = grid.getGridParam("selarrrow");
			} else {
				selectedRows = new Array();
				selectedRows.push(grid.getGridParam('selrow'));
			}
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
	
	function constructor(args) {
		
		gridParameters = parseOptions(args);
		
		
		var element = $("#"+gridParameters.element);
		
		var topButtonsDiv = $("<div>").addClass('qcdGrid_top');
			navigationButtons.newButton =  $("<button>").html('new');
			navigationButtons.newButton.click(newClicked);
			navigationButtons.newButton.attr("disabled", true);
			topButtonsDiv.append(navigationButtons.newButton);
			navigationButtons.deleteButton =  $("<button>").html('delete');
			navigationButtons.deleteButton.click(deleteClicked);
			navigationButtons.deleteButton.attr("disabled", true);
			topButtonsDiv.append(navigationButtons.deleteButton);
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
					sortElements.orderChooser.append("<option value='asc'>asc</option>");
					sortElements.orderChooser.append("<option value='desc'>desc</option>");
					topSortDiv.append(sortElements.orderChooser);
				var sortButton =  $("<button>").html('sort');
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
				var performFilterButton =  $("<button>").html('Filtruj');
				performFilterButton.click(function() {performFilter();});
				filterButtonDiv.append(performFilterButton);
			element.before(performFilterButton);
		}
		
		if (gridParameters.paging) {
			var pagingDiv = $("<div>").addClass('qcdGrid_paging');
				pagingElements.prevButton =  $("<button>").html('prev');
				pagingDiv.append(pagingElements.prevButton);
				
				pagingElements.recordsNoSelect = $("<select>");
					pagingElements.recordsNoSelect.append("<option value=10>10</option>");
					pagingElements.recordsNoSelect.append("<option value=20>20</option>");
					pagingElements.recordsNoSelect.append("<option value=50>50</option>");
					pagingElements.recordsNoSelect.append("<option value=100>100</option>");
				pagingDiv.append(pagingElements.recordsNoSelect);
				
				var pageInfoSpan = $("<span>").addClass('qcdGrid_paging_pageInfo');
					pageInfoSpan.append('<span>page</span>');
					pagingElements.pageNoSpan = $("<span>");
					pageInfoSpan.append(pagingElements.pageNoSpan);
					pageInfoSpan.append('<span>/</span>');
					pagingElements.allPagesNoSpan = $("<span>");
					pageInfoSpan.append(pagingElements.allPagesNoSpan);
				pagingDiv.append(pageInfoSpan);
				
				pagingElements.nextButton =  $("<button>").html('next');
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
			refresh();
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
	
	constructor(args);
	
};
