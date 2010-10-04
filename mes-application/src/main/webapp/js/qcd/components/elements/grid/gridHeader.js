var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.grid = QCD.components.elements.grid || {};

QCD.components.elements.grid.GridHeader = function(_gridController) {
	
	var gridController = _gridController;
	// gridController.onPagingParametersChange();
	
	
	function constructor() {
		
	}
	
	this.getHeaderElement = function() {
		
	}
	
	this.getFooterElement = function() {
		
	}
	
	this.getPagingParameters = function() {
		
		return {
			first: 0,
			last: 100
		}
	}
	
	this.updatePagingParameters = function() {
		
	}
	
	constructor();
}