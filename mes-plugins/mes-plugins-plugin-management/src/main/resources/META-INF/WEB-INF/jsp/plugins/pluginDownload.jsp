<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<script type="text/JavaScript">
	var buttonActive = true;

	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("${headerLabel}");	
	});

	function checkExtension(fileName, submitName, fileTypes) {
		if (!fileName){
			return;
		}

		var dots = fileName.split(".")
		var fileType = dots[dots.length-1];
      
		if (fileTypes.indexOf(fileType) != -1) {
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
		<input type="file" name="file" size="50" onChange="checkExtension(this.value, this.form.upload, ['jar']);"/>
	</div>
		            
	<div class="linkButton activeButton" style="width: 200px; margin-left: 10px;" id="submit">
		<a href="#" onclick="if (buttonActive) {$('#form').submit();}">
			<span>
				<div id="labelDiv">${buttonLabel}</div>
			</span>
		</a>
	</div>
</form>
