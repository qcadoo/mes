var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageElements;
	var viewName = _viewName;
	
	var isInsertByDeserialize = false;
	
	function constructor(_this) {
		var pageConstructor = new QCD.PageConstructor(viewName);
		pageElements = pageConstructor.constructPageElements(_this);
	}
	
	this.init = function(entityId, contextEntityId) {
		for (var i in pageElements) {
			var elementParent = pageElements[i].getParent();
			if (elementParent && elementParent.length > 12) {
				var parts = elementParent.split(":");
				if (parts.length == 2 && parts[0] == "viewElement") {
					var parentViewName = parts[1];
					pageElements[parentViewName].addChild(pageElements[i]);
					QCDLogger.debug("attach "+i+" to "+parentViewName);	
				}
			}
		}
		if (contextEntityId && contextEntityId != "") {
			for (var i in pageElements) {
				pageElements[i].insertContext(contextEntityId);
			}
		}
		QCDLogger.info(isInsertByDeserialize);
		if (entityId && entityId != "" && !isInsertByDeserialize) {
			for (var i in pageElements) {
				var elementParent = pageElements[i].getParent(); 
				if (elementParent == "entityId") {
					pageElements[i].insertParentId(entityId);
				}
			}
		}
	}
	
	this.goToPage = function(url) {
		var serializationObject = new Object();
		for (var i in pageElements) {
			serializationObject[i] = pageElements[i].serialize();
		}
		window.parent.goToPage(url, serializationObject);
	}
	
	this.goBack = function() {
		window.parent.goBack();
	}
	
	this.insertState = function(serializationObject) {
		isInsertByDeserialize = true;
		for (var i in pageElements) {
			pageElements[i].deserialize(serializationObject[i]);
		}
	}
	
	constructor(this);
}