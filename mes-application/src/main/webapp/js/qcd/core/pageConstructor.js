var QCD = QCD || {};

QCD.PageConstructor = function(_viewName, _mainController) {
	
	var windowName = _viewName;
	var mainController = _mainController;
	
	function constructGrid(gridName, mainController) {
		var parameters = getElementParameters(gridName);
		
		var colNames = new Array();
		var colModel = new Array();
		for (var i in parameters.columns) {
			var nameToTranslate = viewName+"."+gridName+".column."+parameters.columns[i];
			colNames.push(mainController.getTranslation(nameToTranslate));
			colModel.push({name:parameters.columns[i], index:parameters.columns[i], width:100, sortable: false});
		}
		
		var gridParameters = new Object();
		gridParameters.element = gridName;
		gridParameters.viewName = viewName,
		gridParameters.viewElementName = gridName;
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.fields = new Array();
		for (var i in parameters.fields) {
			var nameToTranslate = viewName+"."+gridName+".field."+parameters.fields[i];
			gridParameters.fields.push({
				name: parameters.fields[i],
				label: mainController.getTranslation(nameToTranslate)
			});
		}
		gridParameters.paging = parameters.options.paging == "true" ? true : false;
		gridParameters.parentDefinition = parameters.parentDefinition ? parameters.parentDefinition : null;
		gridParameters.isDataDefinitionProritizable = parameters.isDataDefinitionProritizable;
		if (parameters.options) {
			gridParameters.paging = parameters.options.paging == "true" ? true : false;
			gridParameters.sortable = parameters.options.sortable == "true" ? true : false;
			gridParameters.filter = parameters.options.filter == "true" ? true : false;
			gridParameters.multiselect = parameters.options.multiselect == "true" ? true : false;
			gridParameters.canNew = parameters.options.canNew == "false" ? false : true;
			gridParameters.canDelete = parameters.options.canDelete == "false" ? false : true;
			if (parameters.options.height) { gridParameters.height = parseInt(parameters.options.height); }
		}
		gridParameters.events = parameters.events;
		gridParameters.parent = parameters.parent;
		gridParameters.correspondingViewName = parameters.correspondingViewName;
		gridParameters.isCorrespondingViewModal = parameters.isCorrespondingViewModal;
		
		var grid = new QCD.elements.GridElement(gridParameters, mainController);
		
		return grid;
	}
	
	function constructForm(formName, mainController) {
		var parameters = getElementParameters(formName);
		parameters.viewName = viewName;
		var formElement = new QCD.elements.FormElement(parameters, mainController);
		return formElement;
	}
	
	function getElementParameters(elementName) {
		var optionsElement = $("#"+elementName+" .element_options");
		if (!optionsElement.html() || optionsElement.html().trim() == "") {
			var options = new Object();
		} else {
			QCDLogger.info(optionsElement.html());
			var options = jsonParse(optionsElement.html());
		}
		optionsElement.remove();
		return options;
	}
	
	function getComponents(container) {
		var components = new Object();
		container.children().each(function(i,e) {
			var element = $(e);
			var elementFullName = element.attr('id');
			if (element.hasClass("component")) {
				var args = getElementParameters(elementFullName);
				args.windowName = windowName;
				args.elementFullName = elementFullName
				args.elementName = elementFullName.split("-")[elementFullName.split("-").length - 1];
				var component = null;
				if (element.hasClass("component_container")) {
					var containerComponentsElement = $("#"+elementFullName+" .containerComponents");
					var containerComponents = getComponents(containerComponentsElement);
					if (element.hasClass("component_container_form")) {
						component = new QCD.components.containers.Form(element, args, mainController, containerComponents);
					}
				} else if (element.hasClass("component_element")) {
					if (element.hasClass("component_element_grid")) {
						component = new QCD.components.elements.Grid(element, args, mainController);
					} else if (element.hasClass("component_element_textInput")) {
						component = new QCD.components.elements.TextInput(element, args, mainController);
					}
				}
				if (component) {
					components[args.elementName] = component;
				}
			}
		});
		return components;
	}
	
	this.constructPageElements = function() {
		var pageComponents = getComponents($("#windowComponents"));
		QCDLogger.info(pageComponents);
		return pageComponents;
	}
}