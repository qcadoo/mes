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
	
	var tree;
	
	var header;
	var buttons = new Object();
	
	var contextFieldName;
	var contextId;
	
	var elementPath = this.elementPath;
	
	var correspondingViewName = this.options.correspondingView;
	
	var root;
	
	var isEnabled = false;
	
	var openedArrayToInstert;
	var selectedEntityIdToInstert;
	
	function constructor(_this) {
		var messagesPath = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".");
		
		header = $("<div>").addClass('tree_header').addClass('elementHeader').addClass("elementHeaderDisabled");
			var treeName = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+_this.elementPath.replace(/-/g,".")+".header";
			var title = $("<div>").addClass('tree_title').addClass('elementHeaderTitle').html(mainController.getTranslation(treeName));
			header.append(title);
			
			buttons.newButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + '.new') ,function(e) {
				newClicked();
			}, "newIcon16_dis.png");
			buttons.editButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + '.edit') ,function(e) {
				editClicked();
			}, "editIcon16_dis.png");
			buttons.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(mainController.getTranslation(messagesPath + '.delete'),function(e) {
				deleteClicked();
			}, "deleteIcon16_dis.png");
			
			header.append(buttons.newButton);
			header.append(buttons.editButton);
			header.append(buttons.deleteButton);
		
		var content = $("<div>").addClass('tree_content');
		
		var container = $("#"+_this.elementPath+"_treeContent");
		container.addClass('tree_wrapper');
		
		if (_this.options.width) {
			container.width(_this.options.width);
		}
		if (_this.options.height) {
			content.height(_this.options.height - 30);
		}
		container.append(header);
		container.append(content);
		
		tree = content.jstree({ plugins : ["json_data", "themes", "crrm", "ui" ],
			"themes" : {
				"theme": "classic",
				"dots" : true,
				"icons" : false
			},
			"json_data" : {
				"data" : [ ]
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
			buttons.newButton.addClass("headerButtonEnabled");
			if (tree.jstree("get_selected").attr("id").substring(elementPath.length + 6) != 0) {
				buttons.editButton.addClass("headerButtonEnabled");
				buttons.deleteButton.addClass("headerButtonEnabled");
			} else {
				buttons.editButton.removeClass("headerButtonEnabled");
				buttons.deleteButton.removeClass("headerButtonEnabled");
			}
		});
		openedArrayToInstert = new Array();
		openedArrayToInstert.push("0");
		
		block();
	}
	
	this.setComponentState = function(state) {
		QCD.info("setComponentState");
		openedArrayToInstert = state.opened;
		selectedEntityIdToInstert = state.selectedEntityId;
	}
	
	this.getUpdateMode = function() {
		return QCD.components.Component.UPDATE_MODE_UPDATE;
	}
	
	this.getComponentValue = function() {
		var entityId = null;
		if (tree.jstree("get_selected")) {
			entityId = tree.jstree("get_selected").attr("id");
			if (entityId) {
				entityId = entityId.substring(elementPath.length + 6);
			}
		}
		var openedArray = new Array();
		tree.find(".jstree-open").each(function () { 
			openedArray.push(this.id.substring(elementPath.length + 6));
		});
		return {
			opened: openedArray,
			selectedEntityId: entityId
		}
	}
	
	this.setComponentValue = function(value) {
		if (value == null) {
			return;
		}
		if(value.contextFieldName || value.contextId) {
			contextFieldName = value.contextFieldName;
			contextId = value.contextId; 
		}
		if (value.rootNode) {
			if (root) {
				tree.jstree("remove", root); 
			}
			root = addNode(value.rootNode, -1);
		}
		tree.jstree("close_all", root, true);
		if (openedArrayToInstert) {
			for (var i in openedArrayToInstert) {
				tree.jstree("open_node", $("#"+elementPath+"_node_"+openedArrayToInstert[i]), false, true);
			}
			openedArrayToInstert = null;
		} else {
			for (var i in value.openedNodes) {
				tree.jstree("open_node", $("#"+elementPath+"_node_"+value.openedNodes[i]), false, true);
			}
		}
		if (selectedEntityIdToInstert) {
			tree.jstree("select_node", $("#"+elementPath+"_node_"+selectedEntityIdToInstert), false);
			selectedEntityIdToInstert = null;
		}
		if (tree.jstree("get_selected").length > 0) {
			buttons.newButton.addClass("headerButtonEnabled");
			if (tree.jstree("get_selected").attr("id").substring(elementPath.length + 6) != 0) {
				buttons.editButton.addClass("headerButtonEnabled");
				buttons.deleteButton.addClass("headerButtonEnabled");
			} else {
				buttons.editButton.removeClass("headerButtonEnabled");
				buttons.deleteButton.removeClass("headerButtonEnabled");
			}
		} else {
			buttons.newButton.removeClass("headerButtonEnabled");
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
		}
		unblock();
	}
	
	function addNode(data, node) {
		var newNode = tree.jstree("create", node, "last", {data: {title: data.label}, attr : { id: elementPath+"_node_"+data.id }}, false, true);
		newNode.bind("onselect", function() {alert("aa")})
		for (var i in data.children) {
			addNode(data.children[i], newNode, false);
		}
		tree.jstree("close_node", newNode, true);
		return newNode;
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
			QCD.info("new");
			var contextArray = new Array();
			var parentId = tree.jstree("get_selected").attr("id").substring(elementPath.length + 6);
			contextArray.push({
				fieldName: "parent",
				entityId: (parentId == 0) ? null : parentId
			});
			if (contextFieldName && contextId) {
				contextArray.push({
					fieldName: contextFieldName,
					entityId: contextId
				});
			}
			context = "context="+JSON.stringify(contextArray);
			QCD.info("newClicked");
			QCD.info(context);
			redirectToCorrespondingPage(context);
		}
	}
	
	function editClicked() {
		if (buttons.editButton.hasClass("headerButtonEnabled")) {
			QCD.info("edit");
			var entityId = tree.jstree("get_selected").attr("id").substring(elementPath.length + 6);
			redirectToCorrespondingPage("entityId="+entityId);
		}
	}
	
	function deleteClicked() {
		var confirmDeleteMessage = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".confirmDeleteMessage";
		if (buttons.deleteButton.hasClass("headerButtonEnabled")) {
			if (window.confirm(mainController.getTranslation(confirmDeleteMessage))) {
				block();
				var entityId = tree.jstree("get_selected").attr("id").substring(elementPath.length + 6);
				mainController.performDelete(elementPath, entityId, null);	
			}
		}
	}	
	
	function redirectToCorrespondingPage(params) {
		if (correspondingViewName && correspondingViewName != '') {
			var url = correspondingViewName + ".html";
			if (params) {
				url += "?"+params;
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
	
	function block() {
		if (tree) {
			tree.block({ message: '<div class="loading_div">'+mainController.getTranslation("commons.loading")+'</div>', showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
	            border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#000', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: .5, 
	            color: '#fff' } });
		}
	}
	
	function unblock() {
		if (tree) {
			tree.unblock();
		}
	}
	
	constructor(this);
}