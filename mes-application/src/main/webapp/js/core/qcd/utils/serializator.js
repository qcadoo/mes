/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

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

QCDSerializator.equals = function(u, v) {
	if (u == null && v == null) {
		return true;
	}
	
	if (u == null || v == null) {
		return false;
	}
	
    if (typeof(u) != typeof(v)) {
        return false;
    }

    var allkeys = {};
    for (var i in u) {
        allkeys[i] = 1;
    }
    for (var i in v) {
        allkeys[i] = 1;
    }
    for (var i in allkeys) {
        if (u.hasOwnProperty(i) != v.hasOwnProperty(i)) {
            if ((u.hasOwnProperty(i) && typeof(u[i]) == 'function') ||
                (v.hasOwnProperty(i) && typeof(v[i]) == 'function')) {
                continue;
            } else {
                return false;
            }
        }
        if (typeof(u[i]) != typeof(v[i])) {
            return false;
        }
        if (typeof(u[i]) == 'object') {
            if (!QCDSerializator.equals(u[i], v[i])) {
                return false;
            }
        } else {
            if (u[i] !== v[i]) {
                return false;
            }
        }
    }

    return true;
};

