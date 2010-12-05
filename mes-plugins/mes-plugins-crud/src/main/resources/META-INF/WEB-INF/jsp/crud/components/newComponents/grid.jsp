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


	<table style="height: 100%; width: 100%;" cellpadding=0 cellspacing=0 class="gridTable">
	
		<tr><td id="${component['path']}_gridHeader" class="gridHeaderCell">
		</td></tr>
		
		<tr style="height: 100%; width: 100%;" id="${component['path']}_gridCell"><td>
				<table class="element_table" id="${component['path']}_grid" style="height: 100%">
				</table>
		</td></tr>
		
		<tr><td id="${component['path']}_gridFooter" class="gridFooterCell">
		</td></tr>
		
	</table>
