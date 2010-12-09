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

var QCD = QCD || {};

var pnotify_stack = {"dir1": "up", "dir2": "left", "firstpos1": 15, "firstpos2": 30};

QCD.MessagesController = function() {

	this.clearMessager = function() {
		$.pnotify_remove_all()
	}
	
	this.addMessage = function(message) { // type = [info|error|success]
		
		type = message.type.toLowerCase();
		if (type == "failure") {
			type = "error";
		}
		
		$.pnotify({
			pnotify_title: message.title,
			pnotify_text: message.content,
			pnotify_stack: pnotify_stack,
			pnotify_history: false,
			pnotify_width: "300px",
			pnotify_type: type,
			pnotify_addclass: type == 'success' ? 'ui-state-success' : '',
			pnotify_notice_icon: type == 'success' ? 'ui-icon ui-icon-success' : 'ui-icon ui-icon-notify',
			pnotify_error_icon: 'ui-icon ui-icon-error',
			pnotify_opacity: .9,
			pnotify_delay: 4000,
			pnotify_hide: true // type == 'error' ? false : true
		});
		
	}

}

