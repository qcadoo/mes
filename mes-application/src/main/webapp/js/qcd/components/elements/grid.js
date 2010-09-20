var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Grid = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
//	var elementFullName = args.elementFullName;
//	this.elementName = args.elementName;
	
	var mainController = _mainController;
	var element = _element;
	var elementName = element.attr('id');
	
	var gridParameters;
	
	var grid;
	
	var defaultOptions = {
		paging: true,
	};
	
	function parseOptions(options) {
		gridParameters = new Object();

		var colNames = new Array();
		var colModel = new Array();
		
//		/QCD.info(options);
		
		//options.columns = new Array();
		//options.columns[0] = "aaa";
		
		for (var i in options.columns) {
			var nameToTranslate = elementName+".column."+options.columns[i];
			//colNames.push(mainController.getTranslation(nameToTranslate));
			//colModel.push({name:parameters.columns[i], index:parameters.columns[i], width:100, sortable: false});
			colNames.push(nameToTranslate);
			colModel.push({name:options.columns[i], index:options.columns[i], width:100, sortable: false});
		}
		
		gridParameters.element = elementName+"_grid";
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
		if (gridParameters.listeners.length > 0) {
			//QCD.info("SEND");
			mainController.getUpdate(elementName, rowId, gridParameters.listeners);
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
		grid.jqGrid('clearGridData');
		for (var entityNo in value.entities) {
			var entity = value.entities[entityNo];
			grid.jqGrid('addRowData', entity.id, entity.fields);
		}
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (!isEnabled) {
			QCD.error("QCD.components.elements.Grid.setComponentEnabled() not implemented yet");
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
	
	function constructor(_this) {
		parseOptions(_this.options);
//		
//		var topButtonsDiv = $("<div>").addClass('qcdGrid_top');
//			if (gridParameters.canNew) {
//				navigationButtons.newButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.new"));
//				navigationButtons.newButton.click(newClicked);
//				navigationButtons.newButton.attr("disabled", true);
//				topButtonsDiv.append(navigationButtons.newButton);
//			}
//			if (gridParameters.canDelete) {
//				navigationButtons.deleteButton =  $("<button>").html(mainController.getTranslation("commons.grid.button.delete"));
//				navigationButtons.deleteButton.click(deleteClicked);
//				navigationButtons.deleteButton.attr("disabled", true);
//				topButtonsDiv.append(navigationButtons.deleteButton);
//			}
//		element.before(topButtonsDiv);
//		
		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		gridParameters.ondblClickRow = function(id){
			rowDblClicked(id);
        }
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
//		
//		//if (! gridParameters.parent) {
//			//enable();
//			//refresh();
//		//}
	}
	
	constructor(this);
}