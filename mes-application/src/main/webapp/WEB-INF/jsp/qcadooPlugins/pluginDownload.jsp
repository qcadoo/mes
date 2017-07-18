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
<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.form.js?ver=2016_03_19_15_07"></script>

<script type="text/JavaScript">
	var buttonActive = false;

	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("${headerLabel}");
		$('#form').ajaxForm(function(response) {
			window.location = Encoder.htmlDecode(response);
	    }); 
	});

	function checkExtension(fileName, submitName, fileTypes) {
		if (!fileName){
			return;
		}

		var dots = fileName.split(".")
		var fileType = dots[dots.length-1];

		var contains = false;
		for (var i = 0; i < fileTypes.length; i++) {
			if (fileTypes[i] === fileType) {
				contains = true;
			}
		}
      
		if (contains) {
			buttonActive = true;
			$("#submit").addClass("activeButton");
			return true;
		} else {
			$("#submit").removeClass("activeButton");
			buttonActive = false;
	        return false;
		}
	}

</script>


<form method="post" action="<%=ctx%>/performDownload.html" enctype="multipart/form-data" id="form" style="text-align: left">
		        	
	<div style="margin-left: 10px; margin-top: 10px; font: 11px arial; font-weight: bold;">
		${chooseFileLabel}
	</div>
	
	<div style="margin-top: 5px; margin-bottom: 20px; margin-left: 10px;">
		<input type="file" name="file" size="40" onChange="checkExtension(this.value, this.form.upload, ['jar']);"/>
	</div>
		            
	<div class="linkButton" style="width: 200px; margin-left: 10px; margin-bottom: 5px;" id="submit">
		<a href="#" onclick="if (buttonActive) {QCD.components.elements.utils.LoadingIndicator.blockElement($('body')); $('#form').submit();}">
			<span>
				<div id="labelDiv">${buttonLabel}</div>
			</span>
		</a>
	</div>
</form>
