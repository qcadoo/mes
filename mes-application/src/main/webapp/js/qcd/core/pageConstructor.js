var QCD = QCD || {};

QCD.PageConstructor = function(_viewName, _mainController) {
	
	var windowName = _viewName;
	var mainController = _mainController;
	
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