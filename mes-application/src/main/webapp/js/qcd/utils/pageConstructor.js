var QCDPageConstructor = {};

QCDPageConstructor.getChildrenComponents = function(elements, mainController) {
	var components = new Object();
	elements.each(function(i,e) {
		var element = $(e);
		if (element.hasClass("component")) {
			var component = null;
			var elementFullName = element.attr('id');
			var elementName = elementFullName.split("-")[elementFullName.split("-").length - 1];
			
			if (element.hasClass("component_container_window")) {
				component = new QCD.components.containers.Window(element, mainController);
			} if (element.hasClass("component_container_form")) {
				component = new QCD.components.containers.Form(element, mainController);
			} else if (element.hasClass("component_element_grid")) {
				component = new QCD.components.elements.Grid(element, mainController);
			} else if (element.hasClass("component_element_textInput")) {
				component = new QCD.components.elements.TextInput(element, mainController);
			}
			
			if (component) {
				//component.elementFullName = elementFullName;
				component.elementName = elementName;
				components[elementName] = component;
			}
		}
	});
	return components;
}
