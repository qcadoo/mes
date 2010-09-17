var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.TextInput = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;
	
	var element = _element;
	
//	this.elementFullName = args.elementFullName;
//	this.elementName = args.elementName;
//	
//	var input = $("#"+this.elementFullName+"_input");
//	
//	this.insterData = function(data) {
//		QCDLogger.info(input);
//		input.val(data);
//	}
}