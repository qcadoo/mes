var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Tree = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	
	var tree;
	
	var buttons = new Object();
	
	var contextFieldName;
	var contextId;
	
	var elementPath = this.elementPath;
	
	var correspondingViewName = this.options.correspondingView;
	
	var root;
	
	var openedArrayToInstert;
	var selectedEntityIdToInstert;
	
	function constructor(_this) {
		
		var header = $("<div>").addClass('tree_header');
			var treeName = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+_this.elementPath.replace(/-/g,".")+".header";
			var title = $("<div>").addClass('tree_title').html(mainController.getTranslation(treeName));
			header.append(title);
		
			buttons.newButton = $("<div>").addClass('tree_button').html("new").click(newClicked);
			header.append(buttons.newButton);
			buttons.editButton = $("<div>").addClass('tree_button').html("edit").click(editClicked);
			header.append(buttons.editButton);
			buttons.deleteButton = $("<div>").addClass('tree_button').html("delete").click(deleteClicked);
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
			},
		    cookies: false
		}).bind("select_node.jstree", function (e, data) {
			buttons.newButton.addClass("enabled");
			if (tree.jstree("get_selected").attr("id").substring(elementPath.length + 6) != 0) {
				buttons.editButton.addClass("enabled");
				buttons.deleteButton.addClass("enabled");
			} else {
				buttons.editButton.removeClass("enabled");
				buttons.deleteButton.removeClass("enabled");
			}
		});
		openedArrayToInstert = new Array();
		openedArrayToInstert.push("0");
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
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			
		} else {
			buttons.newButton.removeClass("enabled");
			buttons.editButton.removeClass("enabled");
			buttons.deleteButton.removeClass("enabled");
		}
	}
	
	function newClicked() {
		if (buttons.newButton.hasClass("enabled")) {
			QCD.info("new");
			redirectToCorrespondingPage((contextFieldName && contextId) ? "contextFieldName="+contextFieldName+"&contextEntityId="+contextId : null);
		}
	}
	
	function editClicked() {
		if (buttons.editButton.hasClass("enabled")) {
			QCD.info("edit");
			var entityId = tree.jstree("get_selected").attr("id").substring(elementPath.length + 6);
			redirectToCorrespondingPage("entityId="+entityId);
		}
	}
	
	function deleteClicked() {
		if (buttons.deleteButton.hasClass("enabled")) {
			if (window.confirm("delete?")) {
				var entityId = tree.jstree("get_selected").attr("id");
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
	
	constructor(this);
}