var QCD = QCD || {};

QCD.PageController = function(_viewName, _pluginIdentifier, _contextFieldName, _contextEntityId) {
	
	var pageComponents;
	var viewName = _viewName;
	var pluginIdentifier = _pluginIdentifier;
	var contextFieldName = _contextFieldName; 
	var contextEntityId = _contextEntityId;
	
	function constructor(_this) {
		QCDConnector.windowName = viewName;
		QCDConnector.mainController = _this;
		
		var contentElement = $("body");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCD.debug(pageComponents);
	}
	
	this.init = function(entityId, serializationObject) {
		var parameters = new Object();
		if (entityId && entityId.trim() != "") {
			parameters.entityId = entityId;
		}
		if (serializationObject) {
			setComponentState(serializationObject);
		}
		parameters.data = getValueData();
		var valuesJson = JSON.stringify(parameters);
		QCDConnector.sendPost("data", valuesJson, function(response) {
			setValueData(response);
		});
	}
	
	this.getViewName = function() {
		return viewName;
	}
	
	this.getPluginIdentifier = function() {
		return pluginIdentifier;
	}
	
	this.performNew = function(actionsPerformer) {
		var parameters = new Object();
		var valuesJson = JSON.stringify(parameters);
		QCDConnector.sendPost("data", valuesJson, function(response) {
			setValueData(response);
			if (actionsPerformer) {
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
			data: getValueData()
		};
		QCD.info(parameters);
		var valuesJson = JSON.stringify(parameters);
		//QCD.info(valuesJson);
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
			contextFieldName: contextFieldName,
			contextEntityId: contextEntityId,
			data: getValueData()
		};
		QCD.info(parameters);
		var parametersJson = JSON.stringify(parameters);
		//QCD.info(parametersJson);
		QCDConnector.sendPost("save", parametersJson, function(response) {
			QCD.info(response);
			setValueData(response);
			if (actionsPerformer) {
				actionsPerformer.performNext();
			}
		});
	}
	
	this.performDelete = function(componentName, entityId, actionsPerformer) {
		QCD.info("delete " +componentName+" - "+entityId);
		var parameters = {
			componentName: componentName,
			data: getValueData()
		};
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost("delete", parametersJson, function(response) {
			QCD.info(response);
			setValueData(response);
			if (actionsPerformer) {
				actionsPerformer.performNext();
			}
		});
	}
	
	this.performChangePriority = function(componentName, entityId, direction) {
		var parameters = {
			componentName: componentName,
			data: getValueData(),
			offset: direction
		};
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost("move", parametersJson, function(response) {
			QCD.info(response);
			setValueData(response);
		});
	}
	
	this.performRibbonAction = function(ribbonAction) {
		window.parent.addMessage("info", ribbonAction);
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
				
				var actionObject = {
					component: component,
					action: elementAction
				}
				//var func = component[elementAction]
				
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
		//QCD.info(values);
		return values;
	}
	
	function setComponentState(state) {
		QCD.debug(state);
		for (var i in state.components) {
			var component = pageComponents[i];
			component.setState(state.components[i]);
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
			components: getValueData()
		}
		window.parent.goToPage(url, serializationObject);
	}
	
	this.goBack = function() {
		window.parent.goBack();
	}
	
	this.onSessionExpired = function() {
		var serializationObject = new Object();
		//for (var i in pageElements) {
			//serializationObject[i] = pageElements[i].serialize();
		//}
		window.parent.onSessionExpired(serializationObject);
	}
	
	constructor(this);
}