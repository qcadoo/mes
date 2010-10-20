var QCD = QCD || {};

QCD.PageController = function(_viewName, _pluginIdentifier, _context, _lookupComponentName) {
	
	var pageComponents;
	var viewName = _viewName;
	var pluginIdentifier = _pluginIdentifier;
	var context = (_context != null && _context.trim() != "") ? JSON.parse(_context) : null; 
	var lookupComponentName = _lookupComponentName;
	var rootEntityId = null;
	
	var headerComponent = null;
	
	function constructor(_this) {
		QCDConnector.windowName = viewName;
		QCDConnector.mainController = _this;
		
		var contentElement = $("body");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCD.debug(pageComponents);
		
		$(window).bind('resize', updateSize);
		updateSize();
	}
	
	this.init = function(entityId, serializationObject) {
		var parameters = new Object();
		if (entityId && entityId.trim() != "") {
			rootEntityId = entityId;
		}
		if (serializationObject) {
			setComponentState(serializationObject);
		}
		parameters.entityId = rootEntityId;
		parameters.data = getValueData();
		var valuesJson = JSON.stringify(parameters);
		QCDConnector.sendPost("data", valuesJson, function(response) {
			setValueData(response);
		}, function(message) {
			//alert(message);
		});
	}
	
	this.getViewName = function() {
		return viewName;
	}
	
	this.getPluginIdentifier = function() {
		return pluginIdentifier;
	}
	
	this.performCancel = function(entityId, actionsPerformer) {
		var parameters = new Object();
		parameters.entityId = entityId;
		var valuesJson = JSON.stringify(parameters);
		QCDConnector.sendPost("data", valuesJson, function(response) {
			setValueData(response);
			if (actionsPerformer && !(response.errorMessages &&response.errorMessages.length > 0)) {
				actionsPerformer.performNext();
			}
		});
	}
	
	this.performNew = function(actionsPerformer) {
		var parameters = new Object();
		var valuesJson = JSON.stringify(parameters);
		QCDConnector.sendPost("data", valuesJson, function(response) {
			setValueData(response);
			if (actionsPerformer && !(response.errorMessages &&response.errorMessages.length > 0)) {
				actionsPerformer.performNext();
			}
		});
	}

	this.getUpdate = function(componentName, value, listeners) {
		QCD.info("getUpdate "+componentName+"->"+value);
		if (listeners) {
			for (var i in listeners) {
				this.getComponent(listeners[i]).setLoading(true);
			}
		}
		var parameters = {
			componentName: componentName,
			data: getValueData(),
			entityId: rootEntityId
		};
		QCD.info(parameters);
		var valuesJson = JSON.stringify(parameters);
		var _this = this;
		QCDConnector.sendPost("dataUpdate", valuesJson, function(response) {
			QCD.info(response);
			setValueData(response);
			if (listeners) {
				for (var i in listeners) {
					_this.getComponent(listeners[i]).setLoading(false);
				}
			}
		});
	}
	
	this.performSave = function(componentName, actionsPerformer) {
		QCD.info("save " +componentName);
		var parameters = {
			componentName: componentName,
			context: context,
			data: getValueData()
		};
		QCD.info(parameters);
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost("save", parametersJson, function(response) {
			QCD.info(response);
			setValueData(response);
			if (actionsPerformer && !(response.errorMessages &&response.errorMessages.length > 0)) {
				actionsPerformer.performNext();
			}
		});
	}
	
	this.performDelete = function(componentName, entityId, actionsPerformer) {
		QCD.info("delete " +componentName+" - "+entityId);
		var parameters = {
			componentName: componentName,
			data: getValueData(),
			entityId: rootEntityId
		};
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost("delete", parametersJson, function(response) {
			QCD.info(response);
			setValueData(response);
			if (actionsPerformer && !(response.errorMessages &&response.errorMessages.length > 0)) {
				actionsPerformer.performNext();
			}
		});
	}
	
	this.performCallFunction = function(functionName, entityId, actionsPerformer) {
		if (functionName == "printOrder" || functionName == "printMaterialRequirementPdf") {
			window.open(viewName+"/function/"+functionName+".pdf?entityId="+entityId);
		} else if (functionName == "printMaterialRequirementCsv") {
			window.open(viewName+"/function/"+functionName+".xsl?entityId="+entityId);
		}
	}
	
	this.performChangePriority = function(componentName, entityId, direction) {
		var parameters = {
			componentName: componentName,
			data: getValueData(),
			offset: direction,
			entityId: rootEntityId
		};
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost("move", parametersJson, function(response) {
			QCD.info(response);
			setValueData(response);
		});
	}
	
	this.performLookupSelect = function(entityId, entityString, entityCode, actionsPerformer) {
		window.opener[lookupComponentName+"_onSelectFunction"].call(null, entityId, entityString, entityCode);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.performRibbonAction = function(ribbonAction) {
		var actionParts = ribbonAction.split(";");
		var actions = new Array();
		for (var actionIter in actionParts) {
			var action = actionParts[actionIter].trim();
			if (action) {
				var elementBegin = action.search("{");
				var elementEnd = action.search("}");
				if (elementBegin<0 || elementEnd<0 || elementEnd<elementBegin) {
					QCD.error("action parse error in: "+action);
					return;
				}
				var elementPath = action.substring(elementBegin+1, elementEnd);
				
				var elementPathElements = elementPath.split(".");
				var component = pageComponents[elementPathElements[0]];
				var componentPath = elementPath.substring(elementPathElements[0].length+1);
				if (componentPath) {
					component = component.getComponent(componentPath);
				}
				
				var elementAction = action.substring(elementEnd+1);
				if (elementAction[0] != ".") {
					QCD.error("action parse error in: "+action);
					return;
				}
				elementAction = elementAction.substring(1);

				var argumentsBegin = elementAction.indexOf("(");
				var argumentsEnd = elementAction.indexOf(")");
				var argumentsList;
				
				if(argumentsBegin > 0 && argumentsEnd > 0 && argumentsBegin < argumentsEnd) {
					var args = elementAction.substring(argumentsBegin+1, argumentsEnd);
					argumentsList = args.split(",");
					elementAction = elementAction.substring(0, argumentsBegin);
				} 
				

				var actionObject = {
					component: component,
					action: elementAction,
					arguments: argumentsList
				}
				actions.push(actionObject);
			}
		}
		var actionsPerformer = {
			actions: actions,
			actionIter: 0,
			performNext: function() {
				var actionObject = this.actions[this.actionIter];
				if (actionObject) {
					var func = actionObject.component[actionObject.action];
					if (!func) {
						QCD.error("no function in "+actionObject.component.elementName+": "+actionObject.action);
						return;
					}
					this.actionIter++;
					func.call(actionObject.component, this);
				}
			}
		}
		actionsPerformer.performNext();
	}
	
	function getValueData() {
		var values = new Object();
		for (var i in pageComponents) {
			var value = pageComponents[i].getValue();
			if (value) {
				values[i] = value;
			}
		}
		return values;
	}
	
	function setComponentState(state) {
		QCD.debug(state);
		if (state.value) {
			rootEntityId = state.value;
		}
		for (var i in state.components) {
			var component = pageComponents[i];
			component.setState(state.components[i]);
		}
	}
	
	this.showMessage = function(type, content) {
		if (window.parent && window.parent.addMessage) {
			window.parent.addMessage(type, content);
		} else {
			alert(type+": "+content);
		}
	}
	
	this.setWindowHeaderComponent = function(component) {
		headerComponent = component;
	}
	this.setWindowHeader = function(header) {
		if (headerComponent) {
			headerComponent.setHeader(header);
		}
	}
	
	function setValueData(data) {
		QCD.debug(data);
		if (data.messages) {
			for (var i in data.messages) {
				var message = data.messages[i];
				window.parent.addMessage(message.type, message.message);
			}
		}
		for (var i in data.components) {
			var component = pageComponents[i];
			component.setValue(data.components[i]);
		}
		if (data.value) {
			rootEntityId = data.value;
		}
	}
	
	this.getComponent = function(componentPath) {
		var componentName = componentPath.split(".")[0];
		var path = componentPath.substring(componentName.length+1);
		return pageComponents[componentName].getComponent(path);
	}
	
	this.getTranslation = function(key) {
		return window.translationsMap[key] ? window.translationsMap[key] : "TT: "+key;
	}
	var getTranslation = this.getTranslation;
	
	this.goToPage = function(url) {
		var serializationObject = {
			value: rootEntityId,
			components: getValueData()
		}
		window.parent.goToPage(url, serializationObject);
	}
	
	this.goBack = function() {
		window.parent.goBack();
	}
	
	this.closeWindow = function() {
		window.close();
	}
	
	this.onSessionExpired = function() {
		var serializationObject = {
			value: rootEntityId,
			components: getValueData()
		}
		window.parent.onSessionExpired(serializationObject);
	}
	
	function updateSize() {
		var width = $(document).width();
		var height = $(document).height();
		for (var i in pageComponents) {
			pageComponents[i].updateSize(width, height);
		}
	}
	
	constructor(this);
}