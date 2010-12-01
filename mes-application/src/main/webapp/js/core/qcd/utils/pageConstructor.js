/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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
			} else if (element.hasClass("component_element_textArea")) {
				component = new QCD.components.elements.TextArea(element, mainController);
			} else if (element.hasClass("component_element_passwordInput")) {
				component = new QCD.components.elements.PasswordInput(element, mainController);
			} else if (element.hasClass("component_element_dynamicComboBox")) {
				component = new QCD.components.elements.DynamicComboBox(element, mainController);
			} else if (element.hasClass("component_element_entityComboBox")) {
				component = new QCD.components.elements.EntityComboBox(element, mainController);
			} else if (element.hasClass("component_element_lookup")) {
				component = new QCD.components.elements.Lookup(element, mainController);
			} else if (element.hasClass("component_element_checkbox")) {
				component = new QCD.components.elements.CheckBox(element, mainController);
			} else if (element.hasClass("component_element_linkButton")) {
				component = new QCD.components.elements.LinkButton(element, mainController);
			} else if (element.hasClass("component_element_tree")) {
				component = new QCD.components.elements.Tree(element, mainController);
			} else if (element.hasClass("component_element_calendar")) {
				component = new QCD.components.elements.Calendar(element, mainController);
			}
			
			if (! component) {
				component = new QCD.components.elements.StaticComponent(element, mainController);
			}
			
			components[elementName] = component;
		}
	});
	return components;
}
