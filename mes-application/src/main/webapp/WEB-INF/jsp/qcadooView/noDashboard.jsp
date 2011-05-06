<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<c:choose>
		<c:when test="${useCompressedStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css" type="text/css" />
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css" type="text/css" />
		</c:otherwise>
	</c:choose>
	
	
</head>
<body>

	<div id="windowContainer">
		<div id="windowContainerRibbon">
			<div id="q_row3_out">
				<div id="q_menu_row3"></div>
			</div>
			<div id="q_row4_out"></div>
		</div>
		<div id="windowContainerContentBody">
			<div id="contentWrapperOuter">
			<div id="contentWrapperMiddle">
			<div id="dashboardContentWrapper">
			
				<div id="userElement">
					${translationsMap['qcadooView.noDashboardPage.hello']} <span id="userLogin">${userLogin}</span>
				</div>
				<div id="descriptionElement">
					<div id="descriptionHeader">
						${translationsMap['qcadooView.noDashboardPage.header']}
					</div>
					<div id="descriptionContent">
						${translationsMap['qcadooView.noDashboardPage.content']}
					</div>
				</div>
		
			</div>
			</div>
			</div>
		</div>
	</div>
</body>
</html>