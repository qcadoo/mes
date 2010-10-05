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
	}
	
	var defaultOptions = {
		paging: true,
		fullScreen: false
	};
	
	function parseOptions(options) {
		gridParameters = new Object();

		var colNames = new Array();
		var colModel = new Array();
		
		for (var i in options.columns) {
			var nameToTranslate = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".column."+options.columns[i];
			colNames.push(mainController.getTranslation(nameToTranslate));
			colModel.push({name:options.columns[i], index:options.columns[i], width:100, sortable: true});
		}
		gridParameters.element = elementPath+"_grid";
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.datatype = function(postdata) {
			onPostDataChange(postdata);
		}
		
		gridParameters.listeners = options.listeners;
		
//		gridParameters.paging = parameters.options.paging == "true" ? true : false;
//		gridParameters.parentDefinition = parameters.parentDefinition ? parameters.parentDefinition : null;
//		gridParameters.isDataDefinitionProritizable = parameters.isDataDefinitionProritizable;
//		if (parameters.options) {
//			gridParameters.paging = parameters.options.paging == "true" ? true : false;
//			gridParameters.sortable = parameters.options.sortable == "true" ? true : false;
//			gridParameters.filter = parameters.options.filter == "true" ? true : false;
//			gridParameters.multiselect = parameters.options.multiselect == "true" ? true : false;
//			gridParameters.canNew = parameters.options.canNew == "false" ? false : true;
//			gridParameters.canDelete = parameters.options.canDelete == "false" ? false : true;
//			if (parameters.options.height) { gridParameters.height = parseInt(parameters.options.height); }
//		}
//		gridParameters.events = parameters.events;
//		gridParameters.parent = parameters.parent;
		
		gridParameters.canNew = options.canNew;
		gridParameters.canDelete = options.canDelete;
		gridParameters.fullScreen = options.fullScreen;
		if (options.height) { gridParameters.height = parseInt(options.height); }
		if (options.width) { gridParameters.width = parseInt(options.width); }

		gridParameters.correspondingViewName = options.correspondingViewName;
		
		for (var opName in defaultOptions) {
			if (gridParameters[opName] == undefined) {
				gridParameters[opName] = defaultOptions[opName];
			}
		}
		
	};
	function rowClicked(rowId) {
//		if (actionButtons.deleteButton) {
//			actionButtons.deleteButton.removeAttr('disabled');
//		}
		currentState.selectedEntityId = rowId;
		if (gridParameters.listeners.length > 0) {
			mainController.getUpdate(elementPath, rowId, gridParameters.listeners);
		}
	}
	
	function rowDblClicked(rowId) {
		redirectToCorrespondingPage(rowId ? "entityId="+rowId : null);
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
	
	this.setComponentValue = function(value) {
		if(value.contextFieldName || value.contextId) {
			contextFieldName = value.contextFieldName;
			contextId = value.contextId; 
		}
		//pagingVars.totalNumberOfEntities = response.totalNumberOfEntities;
		if (value.entities == null) {
			return;
		}
//		if (actionButtons.deleteButton) {
//			actionButtons.deleteButton.attr('disabled', 'true');
//		}
		grid.jqGrid('clearGridData');
		for (var entityNo in value.entities) {
			var entity = value.entities[entityNo];
			//var fields = new Object();
			//for (var fieldName in entity.fields) {
				//fields[fieldName] = "<a href=# onclick=''>" + entity.fields[fieldName] + "</a>";
			//}
			//grid.jqGrid('addRowData', entity.id, fields);
			grid.jqGrid('addRowData', entity.id, entity.fields);
		}
		headerController.updatePagingParameters(currentState.paging, value.totalNumberOfEntities);
		//updateFullScreenSize();
	}
	
	this.setComponentEnabled = function(isEnabled) {
		componentEnabled = isEnabled;
//		if (actionButtons.newButton) {
//			if (isEnabled) {
//				actionButtons.newButton.removeAttr('disabled');
//			} else {
//				actionButtons.newButton.attr('disabled', 'true');
//			}
//		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			grid.block({ message: mainController.getTranslation("commons.loading.gridLoading"), showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
	            border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#000', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: .5, 
	            color: '#fff' } });
		} else {
			grid.unblock();
		}
	}
	
	function deleteClicked() {
		var selectedId = grid.getGridParam('selrow');
		if (selectedId) {
			//alert("delete "+selectedId);
			mainController.performDelete(elementPath, selectedId);
		}
	}
	
	function constructor(_this) {
		parseOptions(_this.options, _this);
		
		headerController = new QCD.components.elements.grid.GridHeader(_this)
		$("#"+gridParameters.element+"Header").append(headerController.getHeaderElement());
		$("#"+gridParameters.element+"Footer").append(headerController.getFooterElement());
		currentState.paging = headerController.getPagingParameters();
		
		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		gridParameters.ondblClickRow = function(id){
			rowDblClicked(id);
        }
		
		gridParameters.beforeRequest = function(id){
			QCD.info("aaa");
			return false;
        }
		
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
		
		if (gridParameters.fullScreen) {
			if (! gridParameters.height) {
				element.height("100%");
			}
			
			//QCD.info($("#"+gridParameters.element+"Cell").height());
			$(window).bind('resize', function() {
				updateFullScreenSize();
			});
			updateFullScreenSize();
		} else {
			grid.setGridWidth(gridParameters.width, true);
			grid.setGridHeight(gridParameters.height);
		}
		
		QCD.info(grid.jqGrid('filterToolbar',{
			stringResult: true
		}));
		grid[0].toggleToolbar();
	}
	
	this.onPagingParametersChange = function() {
		currentState.paging = headerController.getPagingParameters();
		onCurrentStateChange();
	}
	
	function onPostDataChange(postdata) {
		//QCD.info(postdata);
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
		
		var sortCol = postdata.sidx;
		if (sortCol && sortCol != "") {
			currentState.sort = {
				column: sortCol,
				order: postdata.sord
			}
		} else {
			currentState.sort = null;
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
		QCD.error("to implement: QCD.components.elements.Grid.onUpButtonClicked()");
	}
	
	this.onDownButtonClicked = function() {
		QCD.error("to implement: QCD.components.elements.Grid.onDownButtonClicked()");
	}
	
	function updateFullScreenSize() {
		if (! gridParameters.height && gridParameters.fullScreen) {
			if (searchEnabled) {
				grid.setGridHeight(element.height() - 108);	
			} else {
				grid.setGridHeight(element.height() - 100);
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
		redirectToCorrespondingPage(null);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	var performNew = this.performNew;
	
	
	this.performDelete = function(actionsPerformer) {
		QCD.error("to implement: QCD.components.elements.Grid.performDelete()");
	}
	var performDelete = this.performDelete;
	
	constructor(this);
}