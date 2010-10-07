var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Grid = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	
	var headerController;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	
	var gridParameters;
	var grid;
	var contextFieldName;
	var contextId;
	
	var componentEnabled = false;
	
	var searchEnabled = false;
	
	var currentState = {
		selectedEntityId: null
	}
	
	var columnModel = new Object();
	
	var defaultOptions = {
		paging: true,
		fullScreen: false
	};
	
	function parseOptions(options) {
		gridParameters = new Object();

		var colNames = new Array();
		var colModel = new Array();
		
		for (var i in options.columns) {
			var column = JSON.parse(options.columns[i]);
			columnModel[column.name] = column;
			var nameToTranslate = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".column."+column.name;
			var isSortable = false;
			for (var sortColIter in options.sortColumns) {
				if (options.sortColumns[sortColIter] == column.name) {
					isSortable = true;
					break;
				}
			}
			colNames.push(mainController.getTranslation(nameToTranslate)+"<div class='sortArrow' id='"+elementPath+"_sortArrow_"+column.name+"'></div>");
			colModel.push({name:column.name, index:column.name, width:column.width, sortable: isSortable});
		}
		gridParameters.sortColumns = options.sortColumns;
		gridParameters.element = elementPath+"_grid";
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.datatype = function(postdata) {
			onPostDataChange(postdata);
		}
		
		gridParameters.listeners = options.listeners;
		gridParameters.canNew = options.canNew;
		gridParameters.canDelete = options.canDelete;
		gridParameters.paging = options.paginable;
		gridParameters.filter = options.filter ? true : false;
		gridParameters.orderable = options.prioritizable;
		
		gridParameters.fullScreen = options.fullScreen;
		if (options.height) { gridParameters.height = parseInt(options.height); }
		if (options.width) { gridParameters.width = parseInt(options.width); }
		if (! gridParameters.width && ! gridParameters.fullScreen) {
			gridParameters.width = 300;
		}

		gridParameters.correspondingViewName = options.correspondingViewName;
		
		for (var opName in defaultOptions) {
			if (gridParameters[opName] == undefined) {
				gridParameters[opName] = defaultOptions[opName];
			}
		}
		
	};
	function rowClicked(rowId) {
		var rowIndex = grid.jqGrid('getInd', rowId);
		headerController.onRowClicked(rowIndex);
		currentState.selectedEntityId = rowId;
		if (gridParameters.listeners.length > 0) {
			mainController.getUpdate(elementPath, rowId, gridParameters.listeners);
		}
	}
	
	function linkClicked(entityId) {
		QCD.info("linkClicked: "+entityId);
		redirectToCorrespondingPage("entityId="+entityId);
	}
	
	function redirectToCorrespondingPage(params) {
		if (gridParameters.correspondingViewName && gridParameters.correspondingViewName != '') {
			var url = gridParameters.correspondingViewName + ".html";
			if (params) {
				url += "?"+params;
			}
			mainController.goToPage(url);
		}
	}
	
	this.getComponentValue = function() {
		return currentState;
	}
	
	this.setComponentState = function(state) {
		if (state.selectedEntityId) {
			currentState.selectedEntityId = state.selectedEntityId;
		}
		if (state.paging && state.paging.first) {
			currentState.paging = state.paging;
			headerController.updatePagingParameters(currentState.paging, 0);
		}
		if (state.filters && state.filters.length > 0) {
			currentState.filters = state.filters;
			grid[0].toggleToolbar();
			searchEnabled = true;
			for (var filterIndex in currentState.filters) {
				var filter = currentState.filters[filterIndex];
				$("#gs_"+filter.column).val(filter.value);
			}
			updateFullScreenSize();
		}
		if (state.sort) {
			currentState.sort = state.sort;
			$("#"+elementPath+"_grid_"+currentState.sort.column).addClass("sortColumn");
			if (currentState.sort.order == "asc") {
				$("#"+elementPath+"_sortArrow_"+currentState.sort.column).addClass("upArrow");
			} else {
				$("#"+elementPath+"_sortArrow_"+currentState.sort.column).addClass("downArrow");
			}
		}
	}
	
	this.setComponentValue = function(value) {
		if(value.contextFieldName || value.contextId) {
			contextFieldName = value.contextFieldName;
			contextId = value.contextId; 
		}
		
		if (value.entities == null) {
			return;
		}
		grid.jqGrid('clearGridData');
		var rowCounter = 1;
		for (var entityNo in value.entities) {
			var entity = value.entities[entityNo];
			var fields = new Object();
			for (var fieldName in entity.fields) {
				if (columnModel[fieldName].link) {
					fields[fieldName] = "<a href=# id='"+elementPath+"_"+fieldName+"_"+entity.id+"' class='"+elementPath+"_link gridLink'>" + entity.fields[fieldName] + "</a>";
					
				} else {
					fields[fieldName] = entity.fields[fieldName];
				}
			}
			grid.jqGrid('addRowData', entity.id, fields);
			if (rowCounter % 2 == 0) {
				grid.jqGrid('setRowData', entity.id, false, "darkRow");
			} else {
				grid.jqGrid('setRowData', entity.id, false, "lightRow");
			}
			rowCounter++;
		}
		$("."+elementPath+"_link").click(function(e) {
			var idArr = e.target.id.split("_");
			var entityId = idArr[idArr.length-1];
			linkClicked(entityId);
		});
		
		if (currentState.selectedEntityId) {
			grid.setSelection(currentState.selectedEntityId);
			var rowIndex = grid.jqGrid('getInd', currentState.selectedEntityId);
			if (rowIndex != false) {
				headerController.onRowClicked(rowIndex);	
			} else {
				headerController.onRowClicked(null);
			}
		} else {
			headerController.onRowClicked(null);
		}
		
		headerController.updatePagingParameters(currentState.paging, value.totalNumberOfEntities);
		
		unblockGrid();
	}
	
	this.setComponentEnabled = function(isEnabled) {
		componentEnabled = isEnabled;
		headerController.setEnabled(isEnabled);
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			blockGrid();
		} else {
			unblockGrid();
		}
	}

	
	function blockGrid() {
		if (grid) {
			grid.block({ message: mainController.getTranslation("commons.loading.gridLoading"), showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
	            border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#000', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: .5, 
	            color: '#fff' } });
		}
	}
	
	function unblockGrid() {
		if (grid) {
			grid.unblock();
		}
	}

	function constructor(_this) {
		
		parseOptions(_this.options, _this);
		
		var gridName = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".header";
		headerController = new QCD.components.elements.grid.GridHeader(_this, mainController.getTranslation(gridName), gridParameters);
		
		$("#"+gridParameters.element+"Header").append(headerController.getHeaderElement());
		$("#"+gridParameters.element+"Footer").append(headerController.getFooterElement());
		currentState.paging = headerController.getPagingParameters();
		
		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		gridParameters.onSortCol = onSortColumnChange;
		
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
		
		for (var i in gridParameters.sortColumns) {
			$("#"+elementPath+"_grid_"+gridParameters.sortColumns[i]).addClass("sortableColumn");
		}
		
		if (gridParameters.width) {
			element.width(gridParameters.width);
		}
		if (gridParameters.fullScreen) {
			if (! gridParameters.height) {
				element.height("100%");
			}
			$(window).bind('resize', function() {
				updateFullScreenSize();
			});
			updateFullScreenSize();
		} else {
			grid.setGridWidth(gridParameters.width, true);
			grid.setGridHeight(gridParameters.height);
			$("#"+gridParameters.element+"Header").width(gridParameters.width);
			element.addClass("gridNotFullScreen");
		}
		
		grid.jqGrid('filterToolbar',{
			stringResult: true
		});
		grid[0].toggleToolbar();
	}
	
	this.onPagingParametersChange = function() {
		blockGrid();
		currentState.paging = headerController.getPagingParameters();
		onCurrentStateChange();
	}
	
	 function onSortColumnChange(index,iCol,sortorder) {
		//QCD.info(index+"-"+iCol);
		blockGrid();
		if (currentState.sort && currentState.sort.column) {
			$("#"+elementPath+"_grid_"+currentState.sort.column).removeClass("sortColumn");
		}
		$("#"+elementPath+"_grid_"+index).addClass("sortColumn");
		if (currentState.sort && currentState.sort.column == index) {
			if (currentState.sort.order == "asc") {
				$("#"+elementPath+"_sortArrow_"+index).removeClass("upArrow");
				$("#"+elementPath+"_sortArrow_"+index).addClass("downArrow");
				currentState.sort.order = "desc";
			} else {
				$("#"+elementPath+"_sortArrow_"+index).removeClass("downArrow");
				$("#"+elementPath+"_sortArrow_"+index).addClass("upArrow");
				currentState.sort.order = "asc";
			}
		} else {
			$("#"+elementPath+"_sortArrow_"+index).addClass("upArrow");
			currentState.sort = {
					column: index,
					order: "asc"
				}
		}
		onCurrentStateChange();
		return 'stop';
	}
	
	function onPostDataChange(postdata) {
		blockGrid();
		if (searchEnabled) {
			var postFilters = JSON.parse(postdata.filters);
			var filterArray = new Array();
			for (var i in postFilters.rules) {
				var filterRule = postFilters.rules[i];
				filterArray.push({
					column: filterRule.field,
					value: filterRule.data
				});
			}
			currentState.filters = filterArray;
		} else {
			currentState.filters = null;
		}
		onCurrentStateChange();
	}
	
	this.onFilterButtonClicked = function() {
		grid[0].toggleToolbar();
		searchEnabled = !searchEnabled;
		updateFullScreenSize();
		if (! searchEnabled) {
			currentState.filters = null;
		}
		onCurrentStateChange();
	}
	
	this.onNewButtonClicked = function() {
		performNew();
	}
	
	this.onDeleteButtonClicked = function() {
		 performDelete()
	}
	
	this.onUpButtonClicked = function() {
		blockGrid();
		mainController.performChangePriority(elementPath, currentState.selectedEntityId, -1);
	}
	
	this.onDownButtonClicked = function() {
		blockGrid();
		mainController.performChangePriority(elementPath, currentState.selectedEntityId, 1);
	}
	
	function updateFullScreenSize() {
		if (! gridParameters.height && gridParameters.fullScreen) {
			if (searchEnabled) {
				grid.setGridHeight(element.height() - 96);	
			} else {
				grid.setGridHeight(element.height() - 88);
			}
		}
		if (! gridParameters.width && gridParameters.fullScreen) {
			grid.setGridWidth(element.width()-10, true);
		}
	}
	
	function onCurrentStateChange() {
		if (componentEnabled) {
			mainController.getUpdate(elementPath, currentState, gridParameters.listeners);
		}
	}
	
	this.performNew = function(actionsPerformer) {
		redirectToCorrespondingPage((contextFieldName && contextId) ? "contextFieldName="+contextFieldName+"&contextEntityId="+contextId : null);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	var performNew = this.performNew;
	
	
	this.performDelete = function(actionsPerformer) {
		blockGrid();
		mainController.performDelete(elementPath, currentState.selectedEntityId);
	}
	var performDelete = this.performDelete;
	
	constructor(this);
}