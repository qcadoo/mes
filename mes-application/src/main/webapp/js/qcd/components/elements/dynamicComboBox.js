var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.DynamicComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var selectedValue = null;

	this.getComponentData = function() {
		var selected = this.input.val();
		if (!selected || $.trim(selected) == "") {
			selected = selectedValue;
		}
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		if (data == null) {
			return;
		}
		var previousSelected = this.input.val();
		this.input.children().remove();
		this.input.append("<option value=''></option>");
		for (var i in data.values) {
			var value = data.values[i];
			this.input.append("<option value='"+value+"'>"+value+"</option>");
		}
		selected = data.value;
		
		if (!selected || $.trim(selected) == "") {
			this.input.val(previousSelected);
		} else {
			this.input.val(selected);
		}
	}
	
	this.setComponentState = function(state) {
		selectedValue = state.value;
	}
	
}