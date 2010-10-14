var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.EntityComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var mainController = _mainController;
	var options = this.options;
	var elementPath = this.elementPath;
	var input = this.input;
	
	function constructor(_this) {
		_this.input.change(onChange);
	}

	function onChange() {
		if (options.listeners.length > 0) {
			mainController.getUpdate(elementPath, input.val(), options.listeners);
		}
	}
	
	this.getComponentData = function() {
		var selected = this.input.val();
		if (!selected || selected.trim() == "") {
			selected = null;
		}
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		var previousSelected = this.input.val();
		
		if(data.values != null) {
			this.input.children().remove();
			this.input.append("<option value=''></option>");
			for (var i in data.values) {
				var value = data.values[i];
				this.input.append("<option value='"+i+"'>"+value+"</option>");
			}
		}
		
		selected = data.value;
		
		if (selected != null) {
			this.input.val(selected);
		//} else if (value.emptySelected) {
		//	this.input.val('');
		} else {
			this.input.val(previousSelected);
		}
	}
	
	constructor(this);
}