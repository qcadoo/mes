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

QCD.components.elements.Tree = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	
	var element = _element;
	
	var tree;
	
	var header;
	var buttons = new Object();
	
	var contentElement;
	
	var belongsToEntityId;
	var belongsToFieldName = this.options.belongsToFieldName;
	
	var correspondingView = this.options.correspondingView;
	var correspondingComponent = this.options.correspondingComponent;

	var elementPath = this.elementPath;
	var elementSearchName = this.elementSearchName;

	var root;
	
	var isEnabled = false;
	
	var listeners = this.options.listeners;
	
	var openedNodesArrayToInsert;
	var selectedNodeToInstert;
	
	var fireSelectEvent = true;
	
	var translations = this.options.translations;
	
	function constructor(_this) {
		header = $("<div>").addClass('tree_header').addClass('elementHeader').addClass("elementHeaderDisabled");
			
			var title = $("<div>").addClass('tree_title').addClass('elementHeaderTitle').html(translations.header);
			header.append(title);
			
			buttons.newButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.newButton, function(e) {
				newClicked();
			}, "newIcon16_dis.png");
			buttons.editButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.editButton, function(e) {
				editClicked();
			}, "editIcon16_dis.png");
			buttons.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.deleteButton, function(e) {
				deleteClicked();
			}, "deleteIcon16_dis.png");
			
			header.append(buttons.newButton);
			header.append(buttons.editButton);
			header.append(buttons.deleteButton);
		
		contentElement = $("<div>").addClass('tree_content');
		
		var container = $("<div>").addClass('tree_wrapper');
		
		container.append(header);
		container.append(contentElement);
		
		element.append(container);
		element.css("padding", "10px");
		
		tree = contentElement.jstree({ plugins : ["json_data", "themes", "crrm", "ui", /*"hotkeys"*/ ],
			"themes" : {
				"theme": "classic",
				"dots" : false,
				"icons" : false
			},
			"json_data" : {
				"data" : [ ]
			},
//			"hotkeys" : {
//				"f2" : function () { },
//				"del" : function () { }
//				"up": function(){
//					var o = this.data.ui.last_selected || -1;
//					this.deselect_node(o);
//					this.select_node(this._get_prev(o));
//					return false; 
//				},
//				"down" : function () { 
//					var o = this.data.ui.last_selected || -1;
//					this.deselect_node(o);
//					this.select_node(this._get_next(o));
//					//tree.jstree("select_node", this._get_next(o), false);
//					return false;
//				}
//			},
			"ui": {
				"select_limit": 1
			},
			core : {
				html_titles: true,
				animation: 100
			},
		    cookies: false
		}).bind("before.jstree", function (e, data) {
			if (!isEnabled && (data.func == 'select_node' || data.func == 'hover_node')) { 
				e.stopImmediatePropagation();
		    	return false;
			}
		}).bind("select_node.jstree", function (e, data) {
			if (fireSelectEvent) {
				updateButtons();
				if (listeners.length > 0) {
					onSelectChange();
				}
			}
		});
	}
	
	this.setComponentState = function(state) {
		openedNodesArrayToInsert = state.openedNodes;
		selectedNodeToInstert = state.selectedEntityId;
		belongsToEntityId = state.belongsToEntityId;
	}
	
	this.getComponentValue = function() {
		var openedNodesArray;
		if (openedNodesArrayToInsert) {
			openedNodesArray = openedNodesArrayToInsert;
			openedNodesArrayToInsert = null;
		} else {
			openedNodesArray = new Array();
			tree.find(".jstree-open").each(function () { 
				openedNodesArray.push(getEntityId(this.id));
			});
		}
		var selectedNode;
		if (selectedNodeToInstert) {
			selectedNode = selectedNodeToInstert;
			selectedNodeToInstert = null;
		} else {
			selectedNode = getSelectedEntityId();
		}
		return {
			openedNodes: openedNodesArray,
			selectedEntityId: selectedNode,
			belongsToEntityId: belongsToEntityId
		}
	}
	
	this.setComponentValue = function(value) {
		
		if (value.belongsToEntityId) {
			belongsToEntityId = value.belongsToEntityId;
		}
		
		if (value.root) {
			if (root) {
				tree.jstree("remove", root); 
			}
			root = addNode(value.root, -1);
		}
		
		tree.jstree("close_all", root, true);
		for (var i in value.openedNodes) {
			tree.jstree("open_node", $("#"+elementSearchName+"_node_"+value.openedNodes[i]), false, true);
		}
		
		if (value.selectedEntityId != null) {
			fireSelectEvent = false;
			tree.jstree("select_node", $("#"+elementSearchName+"_node_"+value.selectedEntityId), false);
			fireSelectEvent = true;
		}
		
		updateButtons();
		unblock();
	}
	
	function addNode(data, node) {
		var nodeId = data.id ? data.id : "0";
		var newNode = tree.jstree("create", node, "last", {data: {title: data.label}, attr : { id: elementPath+"_node_"+nodeId }}, false, true);
		for (var i in data.children) {
			addNode(data.children[i], newNode, false);
		}
		tree.jstree("close_node", newNode, true);
		return newNode;
	}
	
	function updateButtons() {
		var selected = getSelectedEntityId();
		if (!selected) {
			buttons.newButton.removeClass("headerButtonEnabled");
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
		} else {
			buttons.newButton.addClass("headerButtonEnabled");
			if (selected != "0") {
				buttons.editButton.addClass("headerButtonEnabled");
				buttons.deleteButton.addClass("headerButtonEnabled");
			} else {
				buttons.editButton.removeClass("headerButtonEnabled");
				buttons.deleteButton.removeClass("headerButtonEnabled");
			}
		}
	}
	
	this.setComponentEnabled = function(_isEnabled) {
		isEnabled = _isEnabled;
		if (isEnabled) {
			tree.removeClass("treeDisabled");
			header.removeClass("elementHeaderDisabled");
		} else {
			tree.addClass("treeDisabled");
			header.addClass("elementHeaderDisabled");
			buttons.newButton.removeClass("headerButtonEnabled");
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
		}
	}
	
	function newClicked() {
		if (buttons.newButton.hasClass("headerButtonEnabled")) {
			var params = new Object();
			if (belongsToFieldName) {
				params[correspondingComponent+"."+belongsToFieldName] = belongsToEntityId;
			}
			var entityId = getSelectedEntityId();
			entityId = entityId=="0" ? null : entityId;
			params[correspondingComponent+".parent"] = entityId;
			redirectToCorrespondingPage(params);
		}
	}
	
	function editClicked() {
		if (buttons.editButton.hasClass("headerButtonEnabled")) {
			var params = new Object();
			params[correspondingComponent+".id"] = getSelectedEntityId();
			redirectToCorrespondingPage(params);
		}
	}
	
	function deleteClicked() {
		var confirmDeleteMessage = translations.confirmDeleteMessage;
		if (buttons.deleteButton.hasClass("headerButtonEnabled")) {
			if (window.confirm(confirmDeleteMessage)) {
				block();
				mainController.callEvent("remove", elementPath, function() {
					unblock();
				}, null, null);
			}
		}
	}	
	
	function onSelectChange() {
		if (isEnabled) {
			mainController.callEvent("select", elementPath, null);
		}
	}
	
	function getSelectedEntityId() {
		var selected = tree.jstree("get_selected");
		if (selected && selected.length > 0) {
			return getEntityId(selected.attr("id"));
		}
		return null;
	}
	function getEntityId(nodeId) {
		return nodeId.substring(elementPath.length + 6);
	}
	
	function redirectToCorrespondingPage(params) {
		if (correspondingView && correspondingView != '') {
			var url = correspondingView + ".html";
			if (params) {
				url += "?context="+JSON.stringify(params);
			}
			mainController.goToPage(url);
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			block();
		} else {
			unblock();
		}
	}
	
	this.updateSize = function(_width, _height) {
		if (! _width) {
			_width = 300;
		}
		if (! _height) {
			_height = 300;
		}
		//element.css("height",_height+"px")
		contentElement.height(_height - 52);
	}
	
	function block() {
		isEnabled = false;
		QCD.components.elements.utils.LoadingIndicator.blockElement(element);
	}
	
	function unblock() {
		QCD.components.elements.utils.LoadingIndicator.unblockElement(element);
		isEnabled = true;
	}
	
	constructor(this);
}