var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Grid = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
//	var elementFullName = args.elementFullName;
//	this.elementName = args.elementName;
	
	var mainController = _mainController;
	var element = _element;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	
	QCD.info(this.elementName);
	
	var gridParameters;
	
	var grid;
	
	var actionButtons = new Object();
	actionButtons.newButton = null;
	actionButtons.deleteButton = null;
	
	var defaultOptions = {
		paging: true,
	};
	
	function parseOptions(options) {
		gridParameters = new Object();

		var colNames = new Array();
		var colModel = new Array();
		
		for (var i in options.columns) {
			var nameToTranslate = mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".column."+options.columns[i];
			colNames.push(mainController.getTranslation(nameToTranslate));
			colModel.push({name:options.columns[i], index:options.columns[i], width:100, sortable: false});
		}
		
		gridParameters.element = elementPath+"_grid";
//		gridParameters.viewName = viewName,
//		gridParameters.viewElementName = gridName;
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.datatype = 'local';
		
		gridParameters.listeners = options.listeners;
		
//		gridParameters.fields = new Array();
//		for (var i in parameters.fields) {
//			var nameToTranslate = viewName+"."+gridName+".field."+parameters.fields[i];
//			gridParameters.fields.push({
//				name: parameters.fields[i],
//				label: mainController.getTranslation(nameToTranslate)
//			});
//		}
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
		gridParameters.canNew = options.canNew == "false" ? false : true;
		gridParameters.canDelete = options.canDelete == "false" ? false : true;
		if (options.height) { gridParameters.height = parseInt(options.height); }

		gridParameters.correspondingViewName = options.correspondingViewName;
//		gridParameters.isCorrespondingViewModal = parameters.isCorrespondingViewModal;
		
		for (var opName in defaultOptions) {
			if (gridParameters[opName] == undefined) {
				gridParameters[opName] = defaultOptions[opName];
			}
		}
		
	};
//	
////	function refresh() {
////		
////		grid.jqGrid('clearGridData');
////		//blockList();
//////		var parameters = new Object();
//////		if (thisMax) {
//////			parameters.maxResults = thisMax;
//////		}
//////		if (thisFirst != null) {
//////			parameters.firstResult = thisFirst;
//////		}
//////		if (sortColumn && sortOrder) {
//////			parameters.sortColumn = sortColumn;
//////			parameters.sortOrder = sortOrder;
//////		}
//////		if (parentId) {
//////			parameters.parentId = parentId;
//////		}
////		QCDConnector.sendGet(gridParameters.elementName, null, function(response) {
////			alert("ok");
////		});
////	}
//	
////	this.init = function() {
////		refresh();
////	}
//	
	function rowClicked(rowId) {
		actionButtons.deleteButton.removeAttr('disabled');
		if (gridParameters.listeners.length > 0) {
			//QCD.info("SEND");
			mainController.getUpdate(elementPath, rowId, gridParameters.listeners);
		}
	}
	
	function rowDblClicked(rowId) {
		redirectToCorrespondingPage(rowId);
	}
	
	function redirectToCorrespondingPage(rowId) {
		if (gridParameters.correspondingViewName && gridParameters.correspondingViewName != '') {
			var url = gridParameters.correspondingViewName + ".html";
//			if (parentId || rowId) {
//				url += "?";
//				if (parentId) {
//					url += "contextEntityId="+parentId;
//				}
				if (rowId) {
//					if (parentId) {
//						url += "&";
//					}
					url += "?entityId="+rowId;
				}
//			}
			mainController.goToPage(url);
		}
	}
	
	this.getComponentValue = function() {
		return {
			selectedEntityId: grid.getGridParam('selrow')
		}
	}
	
	this.setComponentValue = function(value) {
		//pagingVars.totalNumberOfEntities = response.totalNumberOfEntities;
		actionButtons.deleteButton.attr('disabled', 'true');
		grid.jqGrid('clearGridData');
		for (var entityNo in value.entities) {
			var entity = value.entities[entityNo];
			grid.jqGrid('addRowData', entity.id, entity.fields);
		}
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (actionButtons.newButton) {
			if (isEnabled) {
				actionButtons.newButton.removeAttr('disabled');
			} else {
				actionButtons.newButton.attr('disabled', 'true');
			}
		}
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
	
	function newClicked() {
		redirectToCorrespondingPage(null);
	}
	
	function deleteClicked() {
		var selectedId = grid.getGridParam('selrow');
		if (selectedId) {
			alert("delete "+selectedId);
		}
	}
	
	function constructor(_this) {
		parseOptions(_this.options, _this);
		
		var topButtonsDiv = $("<div>").addClass('qcdGrid_top');
			if (gridParameters.canNew) {
				actionButtons.newButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.new"));
				actionButtons.newButton.click(newClicked);
				actionButtons.newButton.attr("disabled", true);
				topButtonsDiv.append(actionButtons.newButton);
			}
			if (gridParameters.canDelete) {
				actionButtons.deleteButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.delete"));
				actionButtons.deleteButton.click(deleteClicked);
				actionButtons.deleteButton.attr("disabled", true);
				topButtonsDiv.append(actionButtons.deleteButton);
			}
		element.append(topButtonsDiv);
		
		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		gridParameters.ondblClickRow = function(id){
			rowDblClicked(id);
        }
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
	}
	
	constructor(this);
}