<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo Framework
    Version: 1.4

    This file is part of Qcadoo.

    Qcadoo is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    ***************************************************************************

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<script type="text/JavaScript">

	var errorTimePassed = false;

	jQuery(document).ready(function(){

		window.mainController.setWindowHeader("${headerLabel}");

		setTimeout('errorTimePassed = true;', 120000);
		checkStatusTimer = setTimeout('checkStatus();', 5000);
		
		$.ajax({
			url: 'performRestart.html',
			type: 'POST',
			complete: function(XMLHttpRequest, textStatus) {
				setTimeout('checkStatus();',5000);
			}
		});	
	});

	function checkStatus() {
		
		if (errorTimePassed) {
			$("#restartMessage").hide();
			$("#errorMessage").show();
			return;
		}
		
		$.ajax({
			url: 'restartPage.html',
			type: 'GET',
			timeout: 2000, 
			complete: function(XMLHttpRequest, textStatus) {
				try {
					if (XMLHttpRequest.status == 200) {
						window.location = "${redirectPage}";
						return;
					} else {
						setTimeout("checkStatus();",1000);
					}
				} catch (e) {
					setTimeout("checkStatus();",1000);
				}
			}
		});
		
	}

</script>

<div style="margin: 10px;">
	<span id="restartMessage" style="padding-top: 10px; padding-bottom: 10px; font-size: 18px; height: 35px; line-height: 35px; background: transparent url('/qcadooView/public/img/core/loading_indicator.gif') no-repeat 0 0; padding-left: 35px;">
		${restartMessage}
	</span>
	<span id="errorMessage" style="display: none; padding-top: 10px; padding-bottom: 10px; font-size: 18px; height: 35px; line-height: 35px; color: red;">
		${restartErrorMessage}
	</span>
</div>