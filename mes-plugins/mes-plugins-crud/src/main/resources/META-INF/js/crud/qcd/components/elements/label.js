var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.elements.Label = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
}