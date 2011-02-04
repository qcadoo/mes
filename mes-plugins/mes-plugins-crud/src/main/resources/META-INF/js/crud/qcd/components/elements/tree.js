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
	
	//var correspondingView = this.options.correspondingView;
	//var correspondingComponent = this.options.correspondingComponent;

	var elementPath = this.elementPath;
	var elementSearchName = this.elementSearchName;

	var root;
	
	var isEnabled = false;
	
	var listeners = this.options.listeners;
	
	var openedNodesArrayToInsert;
	var selectedNodeToInstert;
	
	var fireSelectEvent = true;
	
	var translations = this.options.translations;
	
	var moveMode = false;
	var treeStructureChanged = false;
	
	var nodeDataTypesMap = new Object();
	
	var dataTypesMap = new Object();
	
	var newButtons = new Array();
	
	if (this.options.referenceName) {
		mainController.registerReferenceName(this.options.referenceName, this);
	}
	
	function constructor(_this) {
		header = $("<div>").addClass('tree_header').addClass('elementHeader').addClass("elementHeaderDisabled");
			
			var title = $("<div>").addClass('tree_title').addClass('elementHeaderTitle').html(translations.header);
			header.append(title);
			
			dataTypesMap = _this.options.dataTypes;
			
			for (var i in dataTypesMap) {
				var dataType = dataTypesMap[i];
				var button = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(dataType) {
					if ($(this).hasClass("headerButtonEnabled")) {
						newClicked(dataType);
					}
				}, dataType.newIcon, dataType);
				button.attr("title",translations["newButton_"+dataType.name]);
				newButtons.push(button);
			}
			
			buttons.editButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				editClicked();
			}, "editIcon16_dis.png");
			buttons.editButton.attr("title",translations.editButton);
			buttons.editButton.css("marginLeft", "20px");
			buttons.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				deleteClicked();
			}, "deleteIcon16_dis.png");
			buttons.deleteButton.attr("title",translations.deleteButton);
			buttons.moveUpButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				moveUpClicked();
			}, "upIcon16_dis.png");
			buttons.moveDownButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				moveDownClicked();
			}, "downIcon16_dis.png");
			buttons.moveLeftButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				moveLeftClicked();
			}, "leftIcon16_dis.png");
			buttons.moveRightButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				moveRightClicked();
			}, "rightIcon16_dis.png");
			buttons.moveButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton("", function(e) {
				if (buttons.moveButton.hasClass("headerButtonActive")) {
					diactiveMoveMode();
				} else {
					activeMoveMode();
				}
			}, "moveIcon16_dis.png");
			buttons.moveButton.attr("title","MOVE OFF");
			
			buttons.moveUpButton.hide();
			buttons.moveDownButton.hide();
			buttons.moveLeftButton.hide();
			buttons.moveRightButton.hide();
			
			for (var i in newButtons) {
				header.append(newButtons[i]);
			}
			
			header.append(buttons.editButton);
			header.append(buttons.deleteButton);
			
			header.append(buttons.moveUpButton);
			header.append(buttons.moveDownButton);
			header.append(buttons.moveLeftButton);
			header.append(buttons.moveRightButton);
			
			
			header.append(buttons.moveButton);
			buttons.moveButton.addClass("headerButtonEnabled");
		
		contentElement = $("<div>").addClass('tree_content');
		
		var container = $("<div>").addClass('tree_wrapper');
		
		container.append(header);
		container.append(contentElement);
		
		element.append(container);
		element.css("padding", "10px");
		
		tree = contentElement.jstree({ plugins : ["json_data", "themes", "crrm", "ui", "dnd"  /*"hotkeys"*/ ],
			"themes" : {
				"theme": "classic",
				"dots" : true,
				"icons" : true
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
			"crrm" : {
				"move" : {
					"check_move" : function (m) {
						if (moveMode) {
							var targetId = m.r.attr("id");
							if (!targetId || m.p != "inside") {
								return true;
							}
							var dataType = dataTypesMap[nodeDataTypesMap[getEntityId(targetId)]];
							if (dataType.canHaveChildren) {
								return true;
							} else {
								return false;
							}
						}
						return false;
					}
				}
			},
			"dnd": {
				"drag_finish": function (data) {
					treeStructureChanged = true;
				},
			 	"drop_target" : false,
			 	"drag_target" : false,
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
		var result = new Object();
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
		result.openedNodes = openedNodesArray;
		var selectedNode;
		if (selectedNodeToInstert) {
			selectedNode = selectedNodeToInstert;
			selectedNodeToInstert = null;
		} else {
			selectedNode = getSelectedEntityId();
		}
		result.selectedEntityId = selectedNode;
		result.belongsToEntityId = belongsToEntityId;
		
		if (treeStructureChanged) {
			result.treeStructure = getTreeStructureData();
		}
		
		return result;
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
	
	function activeMoveMode() {
		buttons.moveButton.removeClass("headerButtonEnabled");
		buttons.moveButton.setInfo("Obecnie drzewo jest w trybie modyfikacji polozenia elementow. W trakcie tego trybu czesc funkcjonalnosci na stronie moz byc nieaktywna." +
				" Aby opuscic ten tryb zapisz encje lub cofnij zmiany");
		buttons.moveButton.label.html("MOVE ON");
		//buttons.moveButton.hide();
		
		for (var i in newButtons) {
			newButtons[i].hide();
		}
		buttons.editButton.hide();
		buttons.deleteButton.hide();
		
		buttons.moveUpButton.show();
		buttons.moveUpButton.css("display", "inline-block");
		buttons.moveDownButton.show();
		buttons.moveDownButton.css("display", "inline-block");
		buttons.moveLeftButton.show();
		buttons.moveLeftButton.css("display", "inline-block");
		buttons.moveRightButton.show();
		buttons.moveRightButton.css("display", "inline-block");
		
		buttons.moveUpButton.addClass("headerButtonEnabled");
		buttons.moveDownButton.addClass("headerButtonEnabled");
		buttons.moveLeftButton.addClass("headerButtonEnabled");
		buttons.moveRightButton.addClass("headerButtonEnabled");
		
		moveMode = true;
		updateButtons();
		
		if (listeners.length > 0) {
			for (var i in listeners) {
				var listener = mainController.getComponent(listeners[i]);
				if (listener.elementPath == elementPath) {
					continue;
				}
				listener.setEditable(false);
			}
		}
	}
	
	function diactiveMoveMode() {
		buttons.moveButton.addClass("headerButtonEnabled");
		buttons.moveButton.setInfo();
		buttons.moveButton.label.html("MOVE OFF");
		
		for (var i in newButtons) {
			newButtons[i].show();
		}
		buttons.editButton.show();
		buttons.deleteButton.show();
		buttons.moveUpButton.hide();
		buttons.moveDownButton.hide();
		buttons.moveLeftButton.hide();
		buttons.moveRightButton.hide();
		
		moveMode = false;
		updateButtons();
		if (listeners.length > 0) {
			for (var i in listeners) {
				var listener = mainController.getComponent(listeners[i]);
				if (listener.elementPath == elementPath) {
					continue;
				}
				listener.setEditable(true);
			}
		}
	}
	
	this.performUpdateState = function() {
		if (moveMode) {
			diactiveMoveMode();			
		}
	}
	
	function getTreeStructureData(childrensArray) {
		if (! childrensArray) {
			var childrensArray = tree.jstree("get_json", -1);
		}
		var resultArray = new Array();
		
		for (var i in childrensArray) {
			var nodeObject = {
				id: getEntityId(childrensArray[i].attr.id)
			}
			if (childrensArray[i].children) {
				nodeObject.children = getTreeStructureData(childrensArray[i].children);
			}
			resultArray.push(nodeObject);
		}
		return resultArray;
	}
	
	
	function addNode(data, node) {
		var nodeId = data.id ? data.id : "0";
		nodeDataTypesMap[nodeId] = data.dataType.name;
		var newNode = tree.jstree("create", node, "last", {data: {title: data.label, icon: data.dataType.nodeIcon}, attr : { id: elementPath+"_node_"+nodeId }}, false, true);
		for (var i in data.children) {
			addNode(data.children[i], newNode, false);
		}
		tree.jstree("close_node", newNode, true);
		return newNode;
	}
	
	function updateButtons() {
		var selected = getSelectedEntityId();
		if (!selected) {
			var treeData = tree.jstree("get_json", -1);
			if (treeData.length == 0) {
				for (var i in newButtons) {
					newButtons[i].addClass("headerButtonEnabled");
				}
			} else {
				for (var i in newButtons) {
					newButtons[i].removeClass("headerButtonEnabled");
				}
			}
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
			if (moveMode) {
				buttons.moveUpButton.removeClass("headerButtonEnabled");
				buttons.moveDownButton.removeClass("headerButtonEnabled");
				buttons.moveLeftButton.removeClass("headerButtonEnabled");
				buttons.moveRightButton.removeClass("headerButtonEnabled");
			}
		} else {
			var dataType = dataTypesMap[nodeDataTypesMap[selected]];
			if (dataType.canHaveChildren) {
				for (var i in newButtons) {
					newButtons[i].addClass("headerButtonEnabled");
					//newButtons[i].setInfo();
				}
			} else {
				for (var i in newButtons) {
					newButtons[i].removeClass("headerButtonEnabled");
					//newButtons[i].setInfo("zaznaczony element nie moze miec dzieci");
				}
			}
			if (selected != "0") {
				buttons.editButton.addClass("headerButtonEnabled");
				buttons.deleteButton.addClass("headerButtonEnabled");
			} else {
				buttons.editButton.removeClass("headerButtonEnabled");
				buttons.deleteButton.removeClass("headerButtonEnabled");
			}
			if (moveMode) {
				var selectedNode = tree.jstree("get_selected");
				var parentNode = $.jstree._focused()._get_parent(selectedNode);
				if (parentNode && parentNode != -1) {
					buttons.moveLeftButton.addClass("headerButtonEnabled");	
				} else {
					buttons.moveLeftButton.removeClass("headerButtonEnabled");
				}
				var nextNode = $.jstree._focused()._get_next(selectedNode, true);
				if (nextNode) {
					buttons.moveDownButton.addClass("headerButtonEnabled");	
				} else {
					buttons.moveDownButton.removeClass("headerButtonEnabled");
				}
				var previousNode = $.jstree._focused()._get_prev(selectedNode, true);
				if (previousNode) {
					buttons.moveUpButton.addClass("headerButtonEnabled");
					var previousNodeType = dataTypesMap[nodeDataTypesMap[getEntityId(previousNode.attr("id"))]];
					if (previousNodeType.canHaveChildren) {
						buttons.moveRightButton.addClass("headerButtonEnabled");
					} else {
						buttons.moveRightButton.removeClass("headerButtonEnabled");
					}
				} else {
					buttons.moveUpButton.removeClass("headerButtonEnabled");
					buttons.moveRightButton.removeClass("headerButtonEnabled");
				}
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
			for (var i in newButtons) {
				newButtons[i].removeClass("headerButtonEnabled");
			}
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
		}
	}
	
	function newClicked(dataType) {
		var params = new Object();
		if (belongsToFieldName) {
			params[dataType.correspondingComponent+"."+belongsToFieldName] = belongsToEntityId;
		}
		params[dataType.correspondingComponent+".entityType"] = dataType.name;
		var entityId = getSelectedEntityId();
		entityId = entityId=="0" ? null : entityId;
		params[dataType.correspondingComponent+".parent"] = entityId;
		redirectToCorrespondingPage(params, dataType);
	}
	
	function editClicked() {
		if (buttons.editButton.hasClass("headerButtonEnabled")) {
			var params = new Object();
			var entityId = getSelectedEntityId()
			dataType = dataTypesMap[nodeDataTypesMap[entityId]];
			params[dataType.correspondingComponent+".id"] = entityId;
			redirectToCorrespondingPage(params, dataType);
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
	
	function moveUpClicked() {
		if (buttons.moveUpButton.hasClass("headerButtonEnabled")) {
			var selectedNode = tree.jstree("get_selected");
			var previousNode = $.jstree._focused()._get_prev(selectedNode, true);
			moveNode(selectedNode, previousNode, "before");
		}
	}
	
	function moveDownClicked() {
		if (buttons.moveDownButton.hasClass("headerButtonEnabled")) {
			var selectedNode = tree.jstree("get_selected");
			var nextNode = $.jstree._focused()._get_next(selectedNode, true);
			moveNode(selectedNode, nextNode, "after");
		}
	}

	function moveLeftClicked() {
		if (buttons.moveLeftButton.hasClass("headerButtonEnabled")) {
			var selectedNode = tree.jstree("get_selected");
			var parentNode = $.jstree._focused()._get_parent(selectedNode);
			moveNode(selectedNode, parentNode, "after");
		}
	}

	function moveRightClicked() {
		if (buttons.moveRightButton.hasClass("headerButtonEnabled")) {
			var selectedNode = tree.jstree("get_selected");
			var previousNode = $.jstree._focused()._get_prev(selectedNode, true);
			moveNode(selectedNode, previousNode, "last");
		}
	}
	
	function moveNode(node, targetNode, position) {
		tree.jstree("move_node", node, targetNode, position);
		updateButtons();
		treeStructureChanged = true;
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
	
	function redirectToCorrespondingPage(params, dataType) {
		var correspondingView = dataType.correspondingView;
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