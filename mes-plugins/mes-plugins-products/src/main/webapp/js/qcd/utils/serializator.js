var QCDSerializator = {};

QCDSerializator.serializeForm = function(form) {
	var a = form.serializeArray();
	var o = {};
	$.each(a, function() {
		if (/.*\[.*\]/.test(this.name)) {
			var objectName = this.name.substring(0, this.name.search(/\[/));
			var fieldName = this.name.substring(this.name.search(/\[/)+1, this.name.search(/\]/));
			if (! o[objectName]) {
				o[objectName] = new Object();
			}
			o[objectName][fieldName] = this.value || '';
		} else {
			o[this.name] = this.value || '';
		}
    });
	return o;

};
