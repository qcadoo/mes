var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageComponents;
	var viewName = _viewName;
	
	function constructor(_this) {
		QCDConnector.windowName = viewName;
		
		var contentElement = $("#content");
		var pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), this);
		
		QCDLogger.info(pageComponents);
	}
	
	this.init = function(entityId, contextEntityId, serializationObject) {
//		for (var i in pageElements) {
//			var elementParent = pageElements[i].getParent();
//			if (elementParent && elementParent.length > 12) {
//				var parts = elementParent.split(":");
//				if (parts.length == 2 && parts[0] == "viewElement") {
//					var parentViewName = parts[1];
//					pageElements[parentViewName].addChild(pageElements[i]);
//					QCDLogger.debug("attach "+i+" to "+parentViewName);	
//				}
//			}
//		}
//		if (contextEntityId && contextEntityId != "") {
//			for (var i in pageElements) {
//				pageElements[i].insertContext(contextEntityId);
//			}
//		}
//		if (serializationObject) {
//			for (var i in pageElements) {
//				pageElements[i].deserialize(serializationObject[i]);
//			}
//		} else {
//			for (var i in pageElements) {
//				var elementParent = pageElements[i].getParent();
//				if (!elementParent) {
//					pageElements[i].refresh();
//				} else if (elementParent == "entityId") {
//					if (entityId && entityId != "") {
//						pageElements[i].insertParentId(entityId);
//					}
//				}
//			}
//		}
		//QCDLogger.info("init");
		var parameters = new Object();
		if (entityId && entityId.trim() != "") {
			parameters.entityId = entityId;
		}
		
		QCDConnector.sendGet("data", parameters, function(response) {
			//QCDLogger.info(response);
			
			for (var i in response) {
				var component = pageComponents[i];
				component.insterData(response[i]);
			}
		});
	}
	
	this.getUpdate = function(componentName, value) {
		var parameters = new Object();
		parameters["comp-"+componentName] = value;
		QCDConnector.sendGet("dataUpdate", parameters, function(response) {
			QCDLogger.info(response);
		});
	}
	
	this.getTranslation = function(key) {
		return window.translationsMap[key] ? window.translationsMap[key] : "TT: "+key;
		//return window.parent.commonTranslations[key] ? window.parent.commonTranslations[key] : "ToTranslate";
	}
	
	this.goToPage = function(url) {
		var serializationObject = new Object();
//		for (var i in pageElements) {
//			serializationObject[i] = pageElements[i].serialize();
//		}
		window.parent.goToPage(url, serializationObject);
	}
	
	this.goBack = function() {
		window.parent.goBack();
	}
	
	this.onSessionExpired = function() {
		var serializationObject = new Object();
		for (var i in pageElements) {
			serializationObject[i] = pageElements[i].serialize();
		}
		window.parent.onSessionExpired(serializationObject);
	}
	
	constructor(this);
}