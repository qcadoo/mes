<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<link rel="stylesheet" href="${ctx}/plugins/public/css/pluginInfo.css" type="text/css" />

<script type="text/JavaScript">

	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("<div class='${headerClass}'>${headerLabel}</div>");	
	});

</script>

<div class="pluginInfoPage">

	<div>
		${content}
	</div>
	
	<c:if test="${dependencies != null}">
		<div class="dependencies">
			<c:forEach var="dependency" items="${dependencies}">
				<div class="dependency">
					- <span class="dependencyIdentifier">${dependency.key}</span>
					<c:if test="${dependency.value != null}">
						<span class="dependencyVersion">${dependency.value}</span>
					</c:if>
				</div>
			</c:forEach>
		</div>
	</c:if> 
	
	<c:if test="${isConfirm != null}">
		<div class="confirmButtons">
			<div class="linkButton activeButton confirmButton">
				<a href="#" onclick="window.mainController.goBack()">
					<span>
						<div>${cancelButtonLabel}</div>
					</span>
				</a>
			</div>
			
			<div class="linkButton activeButton confirmButton">
				<a href="#" onclick="QCD.components.elements.utils.LoadingIndicator.blockElement($('body')); window.location='${acceptRedirect}'">
					<span>
						<div>${acceptButtonLabel}</div>
					</span>
				</a>
			</div>
		</div>
	</c:if>
</div>