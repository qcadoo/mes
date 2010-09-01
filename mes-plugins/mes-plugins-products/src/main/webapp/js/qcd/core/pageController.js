var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageElements;
	var viewName = _viewName;
	
	function constructor() {
		var pageConstructor = new QCD.PageConstructor(viewName);
		pageElements = pageConstructor.constructPageElements();
	}
	
	this.init = function(entityId) {
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
		if (entityId && entityId != "") {
			for (var i in pageElements) {
				var elementParent = pageElements[i].getParent(); 
				if (elementParent == "entityId") {
					pageElements[i].insertParentId(entityId);
				}
			}
		}
	}
	
	constructor();
}