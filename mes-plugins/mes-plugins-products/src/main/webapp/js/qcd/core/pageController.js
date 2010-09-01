var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageElements;
	var viewName = _viewName;
	
	this.init = function() {
		QCDLogger.info("init");
		
		var pageConstructor = new QCD.PageConstructor(viewName);
		
		pageElements = pageConstructor.constructPageElements();
	}
	
	this.insertParents = function(definedParentEntities) {
		//QCDLogger.info(pageElements);
		//QCDLogger.info(definedParentEntities);
		for (var i in definedParentEntities) {
			if (pageElements[i]) {
				pageElements[i].insertParentId(definedParentEntities[i]);
			}
		}
	}
	
}