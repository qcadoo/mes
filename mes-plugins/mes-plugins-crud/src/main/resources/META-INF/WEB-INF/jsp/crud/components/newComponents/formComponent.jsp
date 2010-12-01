<%--

    ********************************************************************
    Code developed by amazing QCADOO developers team.
    Copyright (c) Qcadoo Limited sp. z o.o. (2010)
    ********************************************************************

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />
<tiles:useAttribute name="componentType" />
<tiles:useAttribute name="componentBody"/>

	
	<div class="labelbox"><div class="label_h"></div><div class="label" >
		<span style="display: inline" id="${component.pathName}_labelDiv">${component.name}</span>
				<div class="error_box">
				<div id="${componentFullName}_error_icon" class="error_icon"></div>
				<div id="${componentFullName}_error_messages" class="error_messages" style="display: none"></div></div></div></div>
	
			<div class="component_container_form_w">
				<div class="component_container_form_inner">
					<div class="component_container_form_x"></div>
					<div class="component_container_form_y"></div>
					${componentBody}
				</div>
			</div>
