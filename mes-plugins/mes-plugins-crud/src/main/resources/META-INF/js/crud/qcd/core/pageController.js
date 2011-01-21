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
	
	var actionEvaluator = new QCD.ActionEvaluator(this);
	
	var referencesObject = {};
	
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
		
		QCD.components.elements.utils.LoadingIndicator.blockElement($("body"));
	}
	
	this.init = function(serializationObject) {
		
		for (var i in pageComponents) {
			pageComponents[i].performScript();
		}
		
		if (serializationObject) {
			setComponentState(serializationObject);
			if (hasDataDefinition) {
				this.callEvent("initializeAfterBack", null, function() {QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"))});
			} else {
				QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"));
			}
		} else {
			if (hasDataDefinition) {
				this.callEvent("initialize", null, function() {QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"))});
			} else {
				QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"));
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
		var eventCompleteFunction = completeFunction;
		initParameters.event = {
			name: eventName
		}
		if (component) {
			initParameters.event.component = component;
			var componentObject = getComponent(component);
			var componentListeners = componentObject.options.listeners;
			if (componentListeners) {
				for (var i = 0; i<componentListeners.length; i++) {
					var listenerElement = getComponent(componentListeners[i]);
					listenerElement.setComponentLoading(true);
				}
				eventCompleteFunction = function() {
					if (completeFunction) {
						completeFunction();
					}
					for (var i = 0; i<componentListeners.length; i++) {
						var listenerElement = getComponent(componentListeners[i]);
						listenerElement.setComponentLoading(false);
					}
				}
			}
		}
		if (args) {
			initParameters.event.args = args;
		}
		initParameters.components = getValueData();
		performEvent(initParameters, eventCompleteFunction, actionsPerformer);
	}
	
	function performEvent(parameters, completeFunction, actionsPerformer) {
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost(parametersJson, function(response) {
			if (completeFunction) {
				completeFunction();
			}
			if (response.redirect) {
				if (response.redirect.openInNewWindow) {
					window.open(response.redirect.url);
				} else {
					goToPage(response.redirect.url, false);
					return;
				}
			} else {
				setValueData(response);
			}
			if (actionsPerformer && ! (response.content && response.content.status != "ok")) {
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
	
	this.getActionEvaluator = function() {
		return actionEvaluator;
	};
	
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
			if (!component.components) {
				return null;
			}
			component = component.components[pathParts[i]];
			if (! component) {
				return null;
			}
		}
		return component;
	}
	var getComponent = this.getComponent;
	
	this.registerReferenceName = function(referenceName, object) {
		referencesObject[referenceName] = object;
	}
	
	this.getComponentByReferenceName = function(referenceName) {
		return referencesObject[referenceName];
	}
	
	function onWindowClick() {
		if (popup) {
			popup.parentComponent.onPopupClose();
			popup.window.close();
			popup = null;
		}
	}
	
	this.closePopup = function() {
		if (popup) {
			popup.parentComponent.onPopupClose();
			try {
				popup.window.close();
			} catch (e) {
			}
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
		var left = (screen.width/2)-(400);
		var top = (screen.height/2)-(350);
		popup.window = window.open(url, title, 'status=0,toolbar=0,width=800,height=700,left='+left+',top='+top);
		return popup.window;
	}
	
	this.onPopupInit = function() {
		popup.parentComponent.onPopupInit();
	}
	
	this.isPopup = function() {
		return isPopup;
	}
	
	this.goToPage = function(url, isPage) {
		if (isPage == undefined || isPage == null) {
			isPage = true;
		}
		//if(canClose()) {
			var serializationObject = {
				components: getValueData()
			}
			window.parent.goToPage(url, serializationObject, isPage);
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
	
	this.closeWindow = function() {
		window.close();
	}
	
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
	this.updateSize = updateSize;
	
	constructor(this);
}