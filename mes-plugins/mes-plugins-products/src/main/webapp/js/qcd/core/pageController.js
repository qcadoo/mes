var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageElements;
	var viewName = _viewName;
	
	function constructor() {
		var pageConstructor = new QCD.PageConstructor(viewName);
		pageElements = pageConstructor.constructPageElements();
		QCDLogger.info(pageElements);
	}
	
	this.init = function(entityId) {
		for (var i in pageElements) {
			var elementParent = pageElements[i].getParent(); 
			QCDLogger.info(elementParent);
			// TODO attach parent when 'viewElement:{viewName}'
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