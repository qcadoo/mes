var QCD = QCD || {};

QCD.PageController = function(_viewName, _pluginIdentifier, _hasDataDefinition, _isPopup) {
	
	var viewName = _viewName;
	var pluginIdentifier = _pluginIdentifier;
	var hasDataDefinition = _hasDataDefinition;
	var isPopup = _isPopup;
	
	var pageComponents;
	
	var headerComponent = null;
	
	var pageOptions;
	
	var messagesController;
	
	var popup;
	
	function constructor(_this) {
		
		QCDConnector.windowName = "/page/"+pluginIdentifier+"/"+viewName;
		QCDConnector.mainController = _this;
		
		var pageOptionsElement = $("#pageOptions");
		pageOptions = JSON.parse($.trim(pageOptionsElement.html()));
		pageOptionsElement.remove();
		
		var contentElement = $("body");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCD.debug(pageComponents);
		
		$(window).bind('resize', updateSize);
		updateSize();
		
		if (window.parent) {
			$(window.parent).focus(onWindowClick);
		} else {
			$(window).focus(onWindowClick);
		}
	}
	
	this.init = function(entityId, serializationObject) {
		if (serializationObject) {
			setComponentState(serializationObject);
			if (hasDataDefinition) {
				this.callEvent("initializeAfterBack");
			}
		} else {
			if (hasDataDefinition) {
				this.callEvent("initialize");
			}
		}
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
			
			if (response.redirect) {
				if (response.redirect.openInNewWindow) {
					window.open(response.redirect.url);
				} else {
					goToPage(response.redirect.url);
					return;
				}
			} else {
				setValueData(response);
			}
			
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
					fullArgumentList = fullArgumentList.concat(actionObject.arguments[0]);
					fullArgumentList.push(actionObject.arguments.slice(1));
					
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
	
	function onWindowClick() {
		if (popup) {
			QCD.info(popup);
			popup.parentComponent.onPopupClose();
			popup.window.close();
			popup = null;
		}
	}
	this.openPopup = function(url, parentComponent, title) {
		if (popup) {
			
		}
		
		if (url.indexOf("?") != -1) {
			url+="&";
		} else {
			url+="?";
		}
		url+="popup=true";
		
		popup = new Object();
		popup.parentComponent = parentComponent;
		popup.window = window.open(url, title, 'width=800,height=700');
		return popup.window;
	}
	
	this.onPopupInit = function() {
		popup.parentComponent.onPopupInit();
	}
	
	this.isPopup = function() {
		return isPopup;
	}
	
	this.goToPage = function(url) {
		//if(canClose()) {
			var serializationObject = {
				components: getValueData()
			}
			window.parent.goToPage(url, serializationObject);
		//}
	}
	var goToPage = this.goToPage;
	
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
			return window.confirm(pageOptions.translations.backWithChangesConfirmation);
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