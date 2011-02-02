/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Grid = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	
	var options = this.options;
	
	var headerController;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	var elementSearchName = this.elementSearchName;
	
	var gridParameters;
	var grid;
	var belongsToFieldName;
	var currentOrder;

	var translations;
	
	var componentEnabled = false;
	
	var currentGridHeight;
	
	var linkListener;
	
	var currentState = {
		selectedEntityId: null,
		filtersEnabled: false
	}
	
	var RESIZE_COLUMNS_ON_UPDATE_SIZE = true;
	
	var columnModel = new Object();
	
	var hiddenColumnValues = new Object();
	
	var defaultOptions = {
		paging: true,
		fullScreen: false,
		shrinkToFit: false
	};
	
	var globalColumnTranslations = {};
	
	var currentEntities;
	
	var noRecordsDiv;
	
	var FILTER_TIMEOUT = 200;
	var filterRefreshTimeout = null;
	
	var fireOnChangeListeners = this.fireOnChangeListeners;
	
	if (this.options.referenceName) {
		mainController.registerReferenceName(this.options.referenceName, this);
	}
	
	function parseOptions(options) {
		gridParameters = new Object();

		var colNames = new Array();
		var colModel = new Array();
		var isfiltersEnabled = false;
		
		for (var i in options.columns) {
			var column = options.columns[i];
			columnModel[column.name] = column;
			var isSortable = false;
			var isSerchable = false;
			for (var sortColIter in options.orderableColumns) {
				if (options.orderableColumns[sortColIter] == column.name) {
					isSortable = true;
					break;
				}
			}
			for (var sortColIter in options.searchableColumns) {
				if (options.searchableColumns[sortColIter] == column.name) {
					isSerchable = true;
					isfiltersEnabled = true;
					break;
				}
			}
			
			column.isSerchable = isSerchable;
			
			if (!column.hidden) {
				colNames.push(column.label+"<div class='sortArrow' id='"+elementPath+"_sortArrow_"+column.name+"'></div>");
				
				var stype = 'text';
				var searchoptions = {};
				if (column.filterValues) {
					var possibleValues = new Object();
					possibleValues[""] = "";
					for (var i in column.filterValues) {
						possibleValues[i] = column.filterValues[i];
					}
					stype = 'select';
					searchoptions.value = possibleValues;
					searchoptions.defaultValue = "";
				}
				
				var col = {name:column.name, index:column.name, width:column.width, sortable: isSortable, resizable: true, 
						align: column.align, stype: stype, searchoptions: searchoptions
				};
				
				if (searchoptions.value) {
					globalColumnTranslations[column.name] = searchoptions.value;
					col.formatter = function(cellvalue, options, rowObject) {
						return globalColumnTranslations[options.colModel.name][cellvalue];
					}
				}
				
				colModel.push(col);
			} else {
				hiddenColumnValues[column.name] = new Object();
			}
		}
		
		gridParameters.hasPredefinedFilters = options.hasPredefinedFilters;
		gridParameters.predefinedFilters = options.predefinedFilters;
		
		gridParameters.sortColumns = options.orderableColumns;
		
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.datatype = function(postdata) {
			//onPostDataChange(postdata);
		}
		gridParameters.multiselect = true;
		gridParameters.shrinkToFit = true;
		
		gridParameters.listeners = options.listeners;
		gridParameters.canNew = options.creatable;
		gridParameters.canDelete = options.deletable;
		gridParameters.paging = options.paginable;
		gridParameters.filter = isfiltersEnabled;
		gridParameters.orderable = options.prioritizable;
		gridParameters.allowMultiselect = options.multiselect;
		
		gridParameters.fullScreen = options.fullscreen;
		if (options.height) { 
			gridParameters.height = parseInt(options.height);
			if (gridParameters.height <= 0) {
				gridParameters.height = null;
			}
		}
		if (options.width) { gridParameters.width = parseInt(options.width); }
		if (! gridParameters.width && ! gridParameters.fullScreen) {
			gridParameters.width = 300;
		}
		gridParameters.correspondingViewName = options.correspondingView;
		gridParameters.correspondingComponent = options.correspondingComponent;
		
		for (var opName in defaultOptions) {
			if (gridParameters[opName] == undefined) {
				gridParameters[opName] = defaultOptions[opName];
			}
		}
		
	};
	function rowClicked(rowId) {
		if (!gridParameters.allowMultiselect) {
			if (currentState.selectedEntityId == rowId) {
				currentState.selectedEntityId = null;
			} else {
				if (currentState.selectedEntityId) {
					grid.setSelection(currentState.selectedEntityId, false);
				}
				currentState.selectedEntityId = rowId;
			}
		} else {
			if (! currentState.selectedEntityId) {
				currentState.selectedEntityId = new Object();
			}
			if (currentState.selectedEntityId[rowId]) {
				currentState.selectedEntityId[rowId] = null;
			} else {
				currentState.selectedEntityId[rowId] = true;
			}
		}
		
		var rowIndex = grid.jqGrid('getInd', currentState.selectedEntityId);
		if (rowIndex == false) {
			rowIndex = null;
		}
		headerController.onRowClicked(rowIndex);

		var selectedEntity = null;
		if (rowIndex) {
			selectedEntity = currentEntities[rowId];
		}
		fireOnChangeListeners("onChange", [selectedEntity]);
		
		if (gridParameters.listeners.length > 0) {
			onSelectChange();
		}
	}
	
	this.setLinkListener = function(_linkListener) {
		linkListener = _linkListener;
	}
	
	function linkClicked(entityId) {
		if (!componentEnabled) {
			return;
		}
		if (linkListener) {
			linkListener.onGridLinkClicked(entityId);
		} else {
			var params = {};
			params[gridParameters.correspondingComponent+".id"] = entityId;
			redirectToCorrespondingPage(params);	
		}
	}
	
	function redirectToCorrespondingPage(params) {
		if (gridParameters.correspondingViewName && gridParameters.correspondingViewName != '') {
			params[gridParameters.correspondingComponent+"."+belongsToFieldName] = currentState.belongsToEntityId;
			mainController.goToPage(gridParameters.correspondingViewName + ".html?context="+JSON.stringify(params));
		}
	}
	
	this.getComponentValue = function() {
		return currentState;
	}
	
	this.setComponentState = function(state) {
		currentState.selectedEntityId = state.selectedEntityId;
		
		if (state.belongsToEntityId) {
			currentState.belongsToEntityId = state.belongsToEntityId;
		}
		if (state.firstEntity) {
			currentState.firstEntity = state.firstEntity;
		}
		if (state.maxEntities) {
			currentState.maxEntities = state.maxEntities;
		}
		if (state.filtersEnabled) {
			currentState.filtersEnabled = state.filtersEnabled;
			headerController.setFilterActive();
			grid[0].toggleToolbar();
			updateSearchFields();
			if (currentState.filtersEnabled) {
				currentGridHeight -= 21;
			} else {
				currentGridHeight += 21;
			}
			grid.setGridHeight(currentGridHeight);
		}
		if (state.filters) {
			currentState.filters = state.filters;
			for (var filterIndex in currentState.filters) {
				$("#gs_"+filterIndex).val(currentState.filters[filterIndex]);
			}
			findMatchingPredefiniedFilter();
		}
		if (state.order) {
			setSortColumnAndDirection(state.order);
		}
	}
	
	this.setComponentValue = function(value) {
		currentState.selectedEntityId = value.selectedEntityId;

		if (value.belongsToEntityId) {
			currentState.belongsToEntityId = value.belongsToEntityId;
		}
		if (value.firstEntity) {
			currentState.firstEntity = value.firstEntity;
		}
		if (value.maxEntities) {
			currentState.maxEntities = value.maxEntities;
		}
		
		if (value.entities == null) {
			return;
		}
		grid.jqGrid('clearGridData');
		var rowCounter = 1;
		currentEntities = new Object();
		for (var entityNo in value.entities) {
			var entity = value.entities[entityNo];
			currentEntities[entity.id] = entity;
			var fields = new Object();
			for (var fieldName in entity.fields) {
				if (hiddenColumnValues[fieldName]) {
					hiddenColumnValues[fieldName][entity.id] = entity.fields[fieldName];
				} else {
					if (columnModel[fieldName].link && entity.fields[fieldName] && entity.fields[fieldName] != "") {
						fields[fieldName] = "<a href=# id='"+elementPath+"_"+fieldName+"_"+entity.id+"' class='"+elementPath+"_link gridLink'>" + entity.fields[fieldName] + "</a>";
						
					} else {
						fields[fieldName] = entity.fields[fieldName];
					}
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
		if (rowCounter == 1) {
			noRecordsDiv.show();
		} else {
			noRecordsDiv.hide();
		}
		
		$("."+elementSearchName+"_link").click(function(e) {
			var idArr = e.target.id.split("_");
			var entityId = idArr[idArr.length-1];
			linkClicked(entityId);
		});
		
		headerController.updatePagingParameters(currentState.firstEntity, currentState.maxEntities, value.totalEntities);
		
		grid.setSelection(currentState.selectedEntityId, false);
		var rowIndex = grid.jqGrid('getInd', currentState.selectedEntityId);
		
		if (rowIndex == false) {
			currentState.selectedEntityId = null;
			rowIndex = null;
			fireOnChangeListeners("onChange", [null]);
		} else {
			fireOnChangeListeners("onChange", [currentEntities[currentState.selectedEntityId]]);
		}
		headerController.onRowClicked(rowIndex);
		
		if (value.order) {			
			setSortColumnAndDirection(value.order);			
		}
		
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
		QCD.components.elements.utils.LoadingIndicator.blockElement(element);
	}
	
	function unblockGrid() {
		QCD.components.elements.utils.LoadingIndicator.unblockElement(element);
	}

	function constructor(_this) {
		
		parseOptions(_this.options, _this);
		
		gridParameters.modifiedPath = elementPath.replace(/\./g,"_");
		gridParameters.element = gridParameters.modifiedPath+"_grid";
		
		$("#"+elementSearchName+"_grid").attr('id', gridParameters.element);
		
		translations = _this.options.translations;
		belongsToFieldName = _this.options.belongsToFieldName;	
		
		headerController = new QCD.components.elements.grid.GridHeaderController(_this, mainController, gridParameters, _this.options.translations);
		
		$("#"+elementSearchName+"_gridHeader").append(headerController.getHeaderElement());
		$("#"+elementSearchName+"_gridFooter").append(headerController.getFooterElement());
		
		currentState.firstEntity = headerController.getPagingParameters()[0];
		currentState.maxEntities = headerController.getPagingParameters()[1];

		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		gridParameters.ondblClickRow = function(id){
			linkClicked(id);
        }
		gridParameters.onSortCol = onSortColumnChange;
		
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
		
		$("#cb_"+gridParameters.element).hide(); // hide 'select add' checkbox
		$("#jqgh_cb").hide();
		
		for (var i in gridParameters.sortColumns) {
			$("#"+gridParameters.modifiedPath+"_grid_"+gridParameters.sortColumns[i]).addClass("sortableColumn");
		}
		
		element.width("100%");
		
		grid.jqGrid('filterToolbar',{
			stringResult: true
		});
		if (gridParameters.isLookup) {
			headerController.setFilterActive();
			currentState.filtersEnabled = true;
			$("#gs_"+options.columns[0].name).focus();
		} else {
			grid[0].toggleToolbar();
			currentState.filtersEnabled = false;
		}
		
		noRecordsDiv = $("<div>").html(translations.noResults).addClass("noRecordsBox");
		noRecordsDiv.hide();
		$("#"+gridParameters.element).parent().append(noRecordsDiv);
		
	}
	
	this.onPagingParametersChange = function() {
		blockGrid();
		currentState.firstEntity = headerController.getPagingParameters()[0];
		currentState.maxEntities = headerController.getPagingParameters()[1];
		onCurrentStateChange();
	}
	
	function setSortColumnAndDirection(order) {
		if(currentOrder && currentOrder.column == order.column) {
			if (order.direction == "asc") {
				$("#"+elementSearchName+"_sortArrow_"+order.column).removeClass("downArrow");
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("upArrow");
				currentState.order.direction = "asc";
			} else {
				$("#"+elementSearchName+"_sortArrow_"+order.column).removeClass("upArrow");
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("downArrow");
				currentState.order.direction = "desc";
			}
		} else {
			if(currentOrder) {
				$("#"+gridParameters.modifiedPath+"_grid_"+currentOrder.column).removeClass("sortColumn");
			}
			
			$("#"+gridParameters.modifiedPath+"_grid_"+order.column).addClass("sortColumn");
			
			currentState.order = { column: order.column }
			
			if (order.direction == "asc") {
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("upArrow");
				currentState.order.direction = "asc";
			} else {
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("downArrow");
				currentState.order.direction = "desc";
			}
		}
		currentOrder = { column: order.column, direction: order.direction };
	}
	
	function onSortColumnChange(index,iCol,sortorder) {		
		blockGrid();
		currentState.order.column = index;
		if (currentState.order.direction == "asc") {
			currentState.order.direction = "desc";
		} else {
			currentState.order.direction = "asc";
		}
		onCurrentStateChange();
		return 'stop';
	}
	
	function onFilterChange() {
		if (filterRefreshTimeout) {
			window.clearTimeout(filterRefreshTimeout);
			filterRefreshTimeout = null;
		}
		filterRefreshTimeout = window.setTimeout(function() {
			filterRefreshTimeout = null;
			performFilter();
		}, FILTER_TIMEOUT);	
	}
	
	function performFilter() {
		blockGrid();
		if (currentState.filtersEnabled) {
			currentState.filters = new Object();
			for (var i in columnModel) {
				var column = columnModel[i];
				if (column.isSerchable) {
					var filterValue = $("#gs_"+column.name).val();
					filterValue = $.trim(filterValue);
					if (filterValue && filterValue != "") {
						currentState.filters[column.name] = filterValue;
					}
				}
			}
		} else {
			currentState.filters = null;
		}
		onCurrentStateChange();
	}
	
	this.onFilterButtonClicked = function() {
		grid[0].toggleToolbar();
		currentState.filtersEnabled = ! currentState.filtersEnabled;
		if (currentState.filtersEnabled) {
			currentGridHeight -= 23;
			updateSearchFields()
			$("#gs_"+options.columns[0].name).focus();
		} else {
			currentGridHeight += 23;
		}
		grid.setGridHeight(currentGridHeight);
		onCurrentStateChange();
	}
	
	function updateSearchFields() {
		for (var i in columnModel) {
			var column = columnModel[i];
			if (column.isSerchable) {
				var columnElement = $("#gs_"+column.name);
				columnElement.unbind('change keyup');
				if (column.filterValues) {
					columnElement.change(onFilterChange);
				} else {
					columnElement.keyup(function(e) {
						var val = $(this).val();
						var columnName = $(this).attr("id").substring(3);
						var currentFilter = "";
						if (currentState.filters && currentState.filters[columnName]) {
							currentFilter = currentState.filters[columnName];
						}
						if (currentState.filters && val == currentFilter) {
							return;
						}
						onFilterChange();
					});
				}
			} else {
				$("#gs_"+column.name).hide();
			}
		}
	}
	
	this.setFilterState = function(column, filterText) {
		if (! currentState.filtersEnabled) {
			grid[0].toggleToolbar();
			currentState.filtersEnabled = true;
			headerController.setFilterActive();
			currentGridHeight -= 21;
			grid.setGridHeight(currentGridHeight);
		}
		currentState.filters = new Object();
		currentState.filters[column] = filterText;
		$("#gs_"+column).val(filterText);
		$("#gs_"+column).focus();
		updateSearchFields();
	}
	
	this.setFilterObject = function(filter) {
		blockGrid();
		
		var filterObject = filter.filter
		for (var i in columnModel) {
			var column = columnModel[i];
			$("#gs_"+column.name).val("");
		}
		var fieldsNo = 0;
		for (var col in filterObject) {
			filterObject[col] = Encoder.htmlDecode(filterObject[col]);
			$("#gs_"+col).val(filterObject[col]);
			fieldsNo++;
		}
		currentState.filters = filterObject;
		
		if (fieldsNo == 0) {
			if (currentState.filtersEnabled) {
				currentGridHeight += 23;
				grid.setGridHeight(currentGridHeight);
				grid[0].toggleToolbar();
			}
			headerController.setFilterNotActive();
			currentState.filtersEnabled = false;
		} else {
			if (! currentState.filtersEnabled) {
				currentGridHeight -= 23	;
				grid.setGridHeight(currentGridHeight);
				grid[0].toggleToolbar();
				$("#gs_"+options.columns[0].name).focus();
			}
			headerController.setFilterActive();
			currentState.filtersEnabled = true;
		}
		
		setSortColumnAndDirection({column: filter.orderColumn, direction: filter.orderDirection});
		
		updateSearchFields();
		onCurrentStateChange(true);
	}
	
	this.onNewButtonClicked = function() {
		performNew();
	}
	
	this.onDeleteButtonClicked = function() {
		 performDelete();
	}
	
	this.onUpButtonClicked = function() {
		blockGrid();
		mainController.callEvent("moveUp", elementPath, function() {
			unblockGrid();
		});
	}
	
	this.onDownButtonClicked = function() {
		blockGrid();
		mainController.callEvent("moveDown", elementPath, function() {
			unblockGrid();
		});
	}
	
	this.updateSize = function(_width, _height) {
		if (! _width) {
			_width = 300;
		}
		if (! _height) {
			_height = 300;
		}
		
		element.css("height",_height+"px")
		
		var HEIGHT_DIFF = 120;
		currentGridHeight = _height - HEIGHT_DIFF;
		if (currentState.filtersEnabled) {
			currentGridHeight -= 21;
		}
		if (! gridParameters.paging) {
			currentGridHeight += 35;
		}
		grid.setGridHeight(currentGridHeight);
		
		grid.setGridWidth(_width-24, RESIZE_COLUMNS_ON_UPDATE_SIZE);
	}
	
	function onCurrentStateChange(forceUpdate) {
		if (!forceUpdate) {
			findMatchingPredefiniedFilter();
		}
		if (componentEnabled) {
			mainController.callEvent("refresh", elementPath, function() {
				unblockGrid();
			});
		}
	}
	
	function findMatchingPredefiniedFilter() {
		var filterToSearch = {};
		if (currentState.filtersEnabled && currentState.filters) {
			filterToSearch = currentState.filters;
		}
		var isIdentical = true;
		for (var i in gridParameters.predefinedFilters) {
			var predefiniedFilter = gridParameters.predefinedFilters[i].filter;
			isIdentical = true;
			
			if (gridParameters.predefinedFilters[i].orderColumn) {
				if (currentState.order.column != gridParameters.predefinedFilters[i].orderColumn) {
					isIdentical = false;
					continue;
				}
				if (currentState.order.direction != gridParameters.predefinedFilters[i].orderDirection) {
					isIdentical = false;
					continue;
				}
			}
			
			for (var col in columnModel) {
				var column = columnModel[col];
				if (predefiniedFilter[column.name] != filterToSearch[column.name]) {
					isIdentical = false;
					break;
				}
			}
			if (isIdentical) {
				headerController.setPredefinedFilter(i);
				break;
			}
		}
		if (! isIdentical) {
			headerController.setPredefinedFilter(null);
		}
	}
	
	function onSelectChange() {
		if (componentEnabled) {
			mainController.callEvent("select", elementPath, null);
		}
	}
	
	this.performNew = function(actionsPerformer) {
		redirectToCorrespondingPage({});	
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	var performNew = this.performNew;
	
	
	this.performDelete = function(actionsPerformer) {
		if (currentState.selectedEntityId) {
			if (window.confirm(translations.confirmDeleteMessage)) {
				blockGrid();
				mainController.callEvent("remove", elementPath, function() {
					unblockGrid();
				}, null, actionsPerformer);
			}
		} else {
			mainController.showMessage({type: "error", content: translations.noRowSelectedError});
		}	
	}
	var performDelete = this.performDelete;

	this.performCopy = function(actionsPerformer) {
		if (currentState.selectedEntityId) {
			blockGrid();
			mainController.callEvent("copy", elementPath, function() {
				unblockGrid();
			}, null, actionsPerformer);
		} else {
			mainController.showMessage({type: "error", content: translations.noRowSelectedError});
		}	
	}
	var performCopy = this.performCopy;
	
	this.performEvent = function(eventName, args) {
		this.fireEvent(null, eventName, args);
	}
	
	this.fireEvent = function(actionsPerformer, eventName, args) {
		blockGrid();
		mainController.callEvent(eventName, elementPath, function() {
			unblockGrid();
		}, args, actionsPerformer);
	}
	
	this.performLinkClicked = function(actionsPerformer) {
		if (currentState.selectedEntityId) {
			
			linkClicked(currentState.selectedEntityId);
			
			if (actionsPerformer) {
				actionsPerformer.performNext();
			}
		} else {
			mainController.showMessage({type: "error", content: translations.noRowSelectedError});
		}	
	}
	
	this.getLookupData = function(entityId) {
		var result = Object();
		result.entityId = entityId;
		result.lookupValue = hiddenColumnValues["lookupValue"][entityId];
		var lookupCodeLink = grid.getRowData(entityId).lookupCode;
		lookupCodeLink = lookupCodeLink.replace(/^<a[^>]*>/,"");
		lookupCodeLink = lookupCodeLink.replace(/<\/a>$/,"");
		result.lookupCode = lookupCodeLink;
		return result;
	}

	constructor(this);
}