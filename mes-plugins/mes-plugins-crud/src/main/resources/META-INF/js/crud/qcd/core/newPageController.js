var QCD = QCD || {};

QCD.PageController = function(_viewName, _pluginIdentifier) {
	
	var viewName = _viewName;
	var pluginIdentifier = _pluginIdentifier;
	
	var pageComponents;
	
	//var headerComponent = null;
	
	var messagesController;
	
	function constructor(_this) {
		
		QCDConnector.windowName = "/page/"+pluginIdentifier+"/"+viewName;
		QCDConnector.mainController = _this;
		
		var contentElement = $("body");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCD.debug(pageComponents);
		
		$(window).bind('resize', updateSize);
		updateSize();
	}
	
	this.init = function(entityId, serializationObject) {
		if (serializationObject) {
			setComponentState(serializationObject);
			this.callEvent("initializeAfterBack");
		} else {
			this.callEvent("initialize");	
		}
		
		// TODO mina used in static pages (ex. error pages from plugin management) 
		
//		if (hasDataDefinition) {
//			parameters.entityId = rootEntityId;
//			parameters.data = getValueData();
//			var valuesJson = JSON.stringify(parameters);
//			QCDConnector.sendPost("data", valuesJson, function(response) {
//				setValueData(response);
//			}, function(message) {
//				//alert(message);
//			});
//		}
		
		
	}
	
	this.setContext = function(contextStr) {
		var context = JSON.parse(contextStr);
		for (var i in context) {
			var dotPos = i.lastIndexOf(".");
			var contextComponentPath = i.substring(0, dotPos);
			var contextField = i.substring(dotPos+1);
			var contextComponent = this.getComponent(contextComponentPath);
			contextComponent.addContext(contextField, context[i]);
		}
	}
	
	
	this.callEvent = function(eventName, component, completeFunction, args, actionsPerformer) {
		var initParameters = new Object();
		initParameters.event = {
			name: eventName
		}
		if (component) {
			initParameters.event.component = component;
		}
		if (args) {
			initParameters.event.args = args;
		}
		initParameters.components = getValueData();
		performEvent(initParameters, completeFunction, actionsPerformer);
	}
	
	function performEvent(parameters, completeFunction, actionsPerformer) {
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost(parametersJson, function(response) {
			setValueData(response);
			if (completeFunction) {
				completeFunction();
			}
			if (actionsPerformer && response.content.status == "ok") {
				actionsPerformer.performNext();
			}
		}, function() {
			if (completeFunction) {
				completeFunction();
			}
		});
	}
	
	// TODO mina
	
//	this.performLookupSelect = function(entityId, entityString, entityCode, actionsPerformer) {
//		window.opener[lookupComponentName+"_onSelectFunction"].call(null, entityId, entityString, entityCode);
//		if (actionsPerformer) {
//			actionsPerformer.performNext();
//		}
//	}
	
	this.performRibbonAction = function(ribbonAction) {
		var actionParts = ribbonAction.split(";");
		var actions = new Array();
		for (var actionIter in actionParts) {
			var action = $.trim(actionParts[actionIter]);
			if (action) {
				var elementBegin = action.search("{");
				var elementEnd = action.search("}");
				if (elementBegin<0 || elementEnd<0 || elementEnd<elementBegin) {
					QCD.error("action parse error in: "+action);
					return;
				}
				var elementPath = action.substring(elementBegin+1, elementEnd);
				var component = this.getComponent(elementPath);
				
				var elementAction = action.substring(elementEnd+1);
				if (elementAction[0] != ".") {
					QCD.error("action parse error in: "+action);
					return;
				}
				elementAction = elementAction.substring(1);

				var argumentsBegin = elementAction.indexOf("(");
				var argumentsEnd = elementAction.indexOf(")");
				var argumentsList = new Array();
				
				//(argumentsBegin < argumentsEnd-1) because it then means that there are no arguments
				//and only empty parenthesis ()
				if(argumentsBegin > 0 && argumentsEnd > 0 && argumentsBegin < argumentsEnd-1) {
					var args = elementAction.substring(argumentsBegin+1, argumentsEnd);
					argumentsList = args.split(",");
					elementAction = elementAction.substring(0, argumentsBegin);
				} else if(argumentsBegin == argumentsEnd-1) {
					//we need to get rid of the empty parenthesis
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
						QCD.error("no function in "+actionObject.component.elementPath+": "+actionObject.action);
						return;
					}
					this.actionIter++;
					
					var fullArgumentList = new Array(this);
					fullArgumentList = fullArgumentList.concat(actionObject.arguments);
					
					func.apply(actionObject.component, fullArgumentList);
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
		for (var i in state.components) {
			var component = pageComponents[i];
			component.setState(state.components[i]);
		}
	}
	
	this.showMessage = function(message) {
		if (window.parent && window.parent.addMessage) {
			window.parent.addMessage(message);
		} else {
			if (!messagesController) {
				messagesController = new QCD.MessagesController();
			}
			messagesController.addMessage(message);
		}
	}
	
	// TODO mina add header
	
//	this.setWindowHeaderComponent = function(component) {
//		headerComponent = component;
//	}
//	this.setWindowHeader = function(header) {
//		if (headerComponent) {
//			headerComponent.setHeader(header);
//		}
//	}
	
	function setValueData(data) {
		QCD.debug(data);
		if (data.messages) {
			for (var i in data.messages) {
				var message = data.messages[i];
				window.parent.addMessage(message.type, message.content);
			}
		}
		for (var i in data.components) {
			var component = pageComponents[i];
			component.setValue(data.components[i]);
		}
	}
	
	this.getComponent = function(componentPath) {
		var pathParts = componentPath.split(".");
		var component = pageComponents[pathParts[0]];
		if (! component) {
			return null;
		}
		for (var i = 1; i<pathParts.length; i++) {
			component = component.components[pathParts[i]];
			if (! component) {
				return null;
			}
		}
		return component;
	}
	
	
	this.getTranslation = function(key) {
		
		// TODO mina translation
		
	//	return window.translationsMap[key] ? window.translationsMap[key] : key;
		return key;
	}
	var getTranslation = this.getTranslation;
	
	this.goToPage = function(url) {
		//if(canClose()) {
			var serializationObject = {
				components: getValueData()
			}
			window.parent.goToPage(url, serializationObject);
		//}
	}
	
	this.goBack = function() {
		if(canClose()) {
			window.parent.goBack();
		}
	}
	this.canClose = canClose;
	
	function canClose() {
		changed = false;
		for (var i in pageComponents) {
			if(pageComponents[i].isChanged()) {
				changed = true;
			}
		}
		if(changed) {
			return window.confirm(getTranslation('commons.backWithChangesConfirmation'));
		} else {
			return true;
		}
	}
	
//	this.closeWindow = function() {
//		window.close();
//	}
//	
	this.onSessionExpired = function() {
		var serializationObject = {
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