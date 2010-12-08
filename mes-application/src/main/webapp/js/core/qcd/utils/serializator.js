/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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

