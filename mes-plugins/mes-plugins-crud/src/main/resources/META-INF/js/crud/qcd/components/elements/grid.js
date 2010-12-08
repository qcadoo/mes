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
	
	var headerController;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	var elementSearchName = this.elementSearchName;
	
	var gridParameters;
	var grid;
	var belongsToFieldName;

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
				
				colModel.push({name:column.name, index:column.name, width:column.width, sortable: isSortable, resizable: true, 
					align: column.align, stype: stype, searchoptions: searchoptions
					});
			} else {
				hiddenColumnValues[column.name] = new Object();
			}
		}
		
		gridParameters.sortColumns = options.orderableColumns;
		
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.datatype = function(postdata) {
			onPostDataChange(postdata);
		}
		gridParameters.multiselect = true;
		gridParameters.shrinkToFit = true;
		
		gridParameters.listeners = options.listeners;
		gridParameters.canNew = options.creatable;
		gridParameters.canDelete = options.deletable;
		gridParameters.paging = options.paginable;
		gridParameters.filter = isfiltersEnabled;
		gridParameters.orderable = options.prioritizable;
		
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
		if (currentState.selectedEntityId == rowId) {
			currentState.selectedEntityId = null;
		} else {
			if (currentState.selectedEntityId) {
				grid.setSelection(currentState.selectedEntityId, false);
			}
			currentState.selectedEntityId = rowId;
		}
		
		var rowIndex = grid.jqGrid('getInd', currentState.selectedEntityId);
		if (rowIndex == false) {
			rowIndex = null;
		}
		headerController.onRowClicked(rowIndex);
		
		if (gridParameters.listeners.length > 0) {
			onSelectChange();
		}
	}
	
	this.setLinkListener = function(_linkListener) {
		linkListener = _linkListener;
	}
	
	function linkClicked(entityId) {
		if (linkListener) {
			linkListener.onGridLinkClicked(entityId);
		} else {
			var params = new Object();
			params[gridParameters.correspondingComponent+".id"] = entityId;
			redirectToCorrespondingPage(params);	
		}
	}
	
	function redirectToCorrespondingPage(params) {
		if (gridParameters.correspondingViewName && gridParameters.correspondingViewName != '') {
			var url = gridParameters.correspondingViewName + ".html";
			if (params) {
				url += "?context="+JSON.stringify(params);
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
		}
		if (state.order) {
			currentState.order = state.order;
			$("#"+gridParameters.modifiedPath+"_grid_"+currentState.order.column).addClass("sortColumn");
			if (currentState.order.direction == "asc") {
				$("#"+elementSearchName+"_sortArrow_"+currentState.order.column).addClass("upArrow");
			} else {
				$("#"+elementSearchName+"_sortArrow_"+currentState.order.column).addClass("downArrow");
			}
		}
	}
	
	this.setComponentValue = function(value) {
		
		if (value.belongsToEntityId) {
			currentState.belongsToEntityId = value.belongsToEntityId;
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
		}
		headerController.onRowClicked(rowIndex);
		
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
			element.block({ message: '<div class="loading_div">'+translations.loading+'</div>', showOverlay: false,  fadeOut: 0, fadeIn: 0,css: {
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
			element.unblock();
		}
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
		gridParameters.onSortCol = onSortColumnChange;
		
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
		
		$("#cb_"+gridParameters.element).hide(); // hide 'select add' checkbox
		$("#jqgh_cb").hide();
		
		
		for (var i in gridParameters.sortColumns) {
			$("#"+gridParameters.modifiedPath+"_grid_"+gridParameters.sortColumns[i]).addClass("sortableColumn");
		}
		
		if (gridParameters.width) {
			element.width(gridParameters.width);
		}
		if (gridParameters.fullScreen) {
			if (! gridParameters.height) {
				element.height("100%");
			}
		} else {
			grid.setGridWidth(gridParameters.width, true);
			grid.setGridHeight(gridParameters.height);
			$("#"+gridParameters.element+"Header").width(gridParameters.width);
			element.addClass("gridNotFullScreen");
		}
		
		blockGrid();
		
		grid.jqGrid('filterToolbar',{
			stringResult: true
		});
		if (gridParameters.isLookup) {
			headerController.setFilterActive();
			currentState.filtersEnabled = true;
		} else {
			grid[0].toggleToolbar();
			currentState.filtersEnabled = false;
		}
	}
	
	this.onPagingParametersChange = function() {
		blockGrid();
		currentState.firstEntity = headerController.getPagingParameters()[0];
		currentState.maxEntities = headerController.getPagingParameters()[1];
		onCurrentStateChange();
	}
	
	function onSortColumnChange(index,iCol,sortorder) {
		blockGrid();
		if (currentState.order && currentState.order.column) {
			$("#"+gridParameters.modifiedPath+"_grid_"+currentState.order.column).removeClass("sortColumn");
		}
		$("#"+gridParameters.modifiedPath+"_grid_"+index).addClass("sortColumn");
		if (currentState.order && currentState.order.column == index) {
			if (currentState.order.direction == "asc") {
				$("#"+elementSearchName+"_sortArrow_"+index).removeClass("upArrow");
				$("#"+elementSearchName+"_sortArrow_"+index).addClass("downArrow");
				currentState.order.direction = "desc";
			} else {
				$("#"+elementSearchName+"_sortArrow_"+index).removeClass("downArrow");
				$("#"+elementSearchName+"_sortArrow_"+index).addClass("upArrow");
				currentState.order.direction = "asc";
			}
		} else {
			$("#"+elementSearchName+"_sortArrow_"+index).addClass("upArrow");
			currentState.order = {
					column: index,
					direction: "asc"
				}
		}
		onCurrentStateChange();
		return 'stop';
	}
	
	function onPostDataChange(postdata) {
		blockGrid();
		if (currentState.filtersEnabled) {
			try {
				var postFilters = JSON.parse(postdata.filters);
			} catch (e) {
				QCD.info("error in filters");
				QCD.info(postdata.filters);
				mainController.showMessage("error", translations.wrongSearchCharacterError);
				unblockGrid();
				return;
			}
			currentState.filters = new Object();
			for (var i in postFilters.rules) {
				var filterRule = postFilters.rules[i];
				currentState.filters[filterRule.field] = filterRule.data;
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
			currentGridHeight -= 21;
		} else {
			currentGridHeight += 21;
		}
		grid.setGridHeight(currentGridHeight);
		onCurrentStateChange();
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
		if (! gridParameters.height && gridParameters.fullScreen) {
			element.height(_height - 40);
			var HEIGHT_DIFF = 140;
			currentGridHeight = _height - HEIGHT_DIFF;
			
			if (currentState.filtersEnabled) {
				currentGridHeight -= 21;
			}
			grid.setGridHeight(currentGridHeight);
		}
		if (! gridParameters.width && gridParameters.fullScreen) {
			grid.setGridWidth(_width-45, RESIZE_COLUMNS_ON_UPDATE_SIZE);
			element.width(_width - 40);
		}
	}
	
	function onCurrentStateChange() {
		if (componentEnabled) {
			mainController.callEvent("refresh", elementPath, function() {
				unblockGrid();
			});
		}
	}
	
	function onSelectChange() {
		if (componentEnabled) {
			mainController.callEvent("select", elementPath, null);
		}
	}
	
	this.performNew = function(actionsPerformer) {
		var params = new Object();
		params[gridParameters.correspondingComponent+"."+belongsToFieldName] = currentState.belongsToEntityId;
		redirectToCorrespondingPage(params);	
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
			mainController.showMessage("error", translations.noRowSelectedError);
		}	
	}
	var performDelete = this.performDelete;
	
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
			mainController.showMessage("error", translations.noRowSelectedError);
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