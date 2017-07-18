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
	var buttonActive = false;
	var selectRibbonItem = null;

	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("${headerLabel}");
		$('#form').ajaxForm(function(responseText, statusText, xhr) {

			if (xhr.errorText) {
				if (xhr.errorText == "LoginPage") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
					
				QCDConnector.showErrorMessage(xhr.errorText);
				QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"));
				return;
			}

			if (responseText.search(/upload size exceeded/) >= 0) {
				QCDConnector.showErrorMessage("${maxUploadSizeExceeded}");
				QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"));
				return;
			}

			var response  = $.trim(responseText).replace(/<PRE>/,'').replace(/<\/PRE>/,'');
			window.mainController.getComponentByReferenceName("window").closeThisModalWindow(null, response);
	    }); 
	});
	
	function onInputChange(fileName) {
		if (!selectRibbonItem) {
			selectRibbonItem = window.mainController.getComponentByReferenceName("window").getRibbonItem("navigation.select");
			selectRibbonItem.addOnChangeListener({
				onClick: performSubmit
			});
		}
		if (fileName && fileName != "") {
			buttonActive = true;
			$("#submit").addClass("activeButton");
			selectRibbonItem.enable();
			return true;
		} else {
			buttonActive = false;
			$("#submit").removeClass("activeButton");
			selectRibbonItem.disable();
	        return false;
		}
	}

	function performSubmit() {
		if (buttonActive) {
			QCD.components.elements.utils.LoadingIndicator.blockElement($("body"));
			$('#form').submit();
		}
	}

</script>


<form method="post" action="<%=ctx%>/fileUpload.html" enctype="multipart/form-data" id="form" style="text-align: left">
		        	
	<div style="margin-left: 10px; margin-top: 10px; font: 11px arial; font-weight: bold;">
		${chooseFileLabel}
	</div>
	
	<div style="margin-top: 5px; margin-bottom: 20px; margin-left: 10px;">
		<input type="file" name="file" size="40" onChange="onInputChange(this.value);"/>
	</div>
		            
	<div class="linkButton" style="width: 200px; margin-left: 10px; margin-bottom: 5px;" id="submit">
		<a href="#" onclick="performSubmit()">
			<span>
				<div id="labelDiv">${buttonLabel}</div>
			</span>
		</a>
	</div>
</form>
