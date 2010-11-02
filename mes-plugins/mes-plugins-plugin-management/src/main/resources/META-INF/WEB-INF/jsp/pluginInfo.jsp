<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

		<c:choose>
			<c:when test="${pluginStatusError}">
				<c:set var="infoStyle" value="color: red;"/>
			</c:when>
			<c:otherwise>
    			<c:set var="infoStyle" value="color: green;"/>
			</c:otherwise>
		</c:choose>
	
	<div style='margin: 20px; ${infoStyle}'>
		<div style="margin-bottom: 10px; font-weight: bold;">
			${pluginStatusMessageHeader}
		</div>
		<div>
			${pluginStatusMessage}
		</div>
	</div>

