var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.containers.layout.SeperatorLine = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

	if (this.options.referenceName) {
		_mainController.registerReferenceName(this.options.referenceName, this);
	}
}