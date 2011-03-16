<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<script type="text/JavaScript">

	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("${headerLabel}");	
	});

</script>

<div>

	<div>
	${content}
	</div>
	
	<c:if test="${dependencies != null}">
		<div>
			<c:forEach var="dependency" items="${dependencies}">
				<div>
					<span>${dependency.key}</span>
					<c:if test="${dependency.value != null}">
						<span>${inVersion}</span>
						<span>${dependency.value}</span>
					</c:if>
				</div>
			</c:forEach>
		</div>
	</c:if> 

	<div class="linkButton activeButton" style="width: 200px; margin-left: 10px;">
		<a href="#" onclick="window.mainController.goBack()">
			<span>
				<div>${cancelButtonLabel}</div>
			</span>
		</a>
	</div>
	
	<div class="linkButton activeButton" style="width: 200px; margin-left: 10px;">
		<a href="#" onclick="window.location = '${acceptRedirect}'">
			<span>
				<div>${acceptButtonLabel}</div>
			</span>
		</a>
	</div>
	
</div>