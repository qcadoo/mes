<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />

<div id="${component['path']}_layoutComponents" class="borderLayout">

	<span class="borderLayoutLabel">ZZZ</span>
	
	<c:forEach items="${component['children']}" var="component">
		<tiles:insertTemplate template="../../component.jsp">
			<tiles:putAttribute name="component" value="${component.value}" />
		</tiles:insertTemplate>
	</c:forEach>
	
</div>