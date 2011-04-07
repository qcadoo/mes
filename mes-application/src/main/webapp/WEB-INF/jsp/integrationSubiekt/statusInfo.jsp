<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<style type="text/css">

	.statusInfoPage {
		text-align: left;
		margin: 10px 10px 0px 10px;
	}
	
	.statusInfoPage .contentHeader {
		margin-bottom: 10px;
	}
	
	.successHeader {
		color: green;
	}
	
	.errorHeader {
		color: red;
	}
	
</style>

<script type="text/JavaScript">

	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("<div class='${headerClass}'>${headerLabel}</div>");	
	});

</script>

<div class="statusInfoPage">

	<div class="contentHeader">
		${contentHeader}
	</div>
	<div>
		${content}
	</div>
	
</div>