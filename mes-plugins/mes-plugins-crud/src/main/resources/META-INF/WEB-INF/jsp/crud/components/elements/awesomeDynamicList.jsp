<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />

<c:if test="${! component.jspOptions.hasBorder}">
	<c:set var="borderStyle" value="noBorder" />
</c:if>
	
<div class="awesomeDynamicList ${borderStyle}">
	<c:if test="${component.jspOptions.header != null}">
		<div class="awesomeDynamicListHeader">
			<tiles:insertTemplate template="../component.jsp">
				<tiles:putAttribute name="component" value="${component.jspOptions.header}" />
			</tiles:insertTemplate>
		</div>
	</c:if>
	
	<div class="awesomeDynamicListInnerForm" style="display: none;">
		<tiles:insertTemplate template="../component.jsp">
			<tiles:putAttribute name="component" value="${component.jspOptions.innerForm}" />
		</tiles:insertTemplate>
	</div>
	
	<div class="awesomeDynamicListContent">
	</div>
</div>