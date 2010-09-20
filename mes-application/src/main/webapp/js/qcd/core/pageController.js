var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageComponents;
	var viewName = _viewName;
	
	function constructor(_this) {
		QCDConnector.windowName = viewName;
		
		var contentElement = $("#content");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCDLogger.debug(pageComponents);
	}
	
	this.init = function(entityId) {
		var parameters = new Object();
		if (entityId && entityId.trim() != "") {
			parameters.entityId = entityId;
		}
		QCDConnector.sendGet("data", parameters, function(response) {
			updateData(response);
		});
	}
	
	this.getUpdate = function(componentName, value) {
		QCDLogger.info(componentName+"->"+value);
		var values = new Object();
		for (var i in pageComponents) {
			var value = pageComponents[i].getValue();
			if (value) {
				values[i] = value;
			}
		}
		QCDLogger.info(values);
		values = JSON.stringify(values);
		QCDLogger.info(values);
		QCDConnector.sendPost("dataUpdate", values, function(response) {
			QCDLogger.info(response);
		});
	}
	
	function updateData(data) {
		QCDLogger.debug(data);
		for (var i in data.components) {
			var component = pageComponents[i];
			component.setValue(data.components[i]);
		}
	}
	
	this.getTranslation = function(key) {
		return window.translationsMap[key] ? window.translationsMap[key] : "TT: "+key;
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