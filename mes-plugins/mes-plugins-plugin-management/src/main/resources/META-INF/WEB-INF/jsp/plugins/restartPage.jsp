<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.1

    This file is part of Qcadoo.

    Qcadoo is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    ***************************************************************************

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<script type="text/javascript" src="/js/core/lib/jquery-1.4.2.min.js"></script>

	<script type="text/javascript">

		jQuery(document).ready(function(){
			$.ajax({
				url: 'handleRestart.html',
				type: 'POST',
				complete: function(XMLHttpRequest, textStatus) {
					setTimeout('checkStatus();',5000);
				}
			});
		});

		function checkStatus() {
			$.ajax({
				url: 'restartPage.html?message=noMessage',
				type: 'GET',
				timeout: 2000, 
				complete: function(XMLHttpRequest, textStatus) {
					try {
						if (XMLHttpRequest.status == 200) {
							window.location = "restartInfoView.html?message=${message}";
							return;
						} else {
							setTimeout("checkStatus();",1000);
						}
					} catch (e) {
						setTimeout("checkStatus();",1000);
					}
				}
			});
		}

	</script>

</head>
<body>

	<div style="margin: 20px; font-size: 18px; height: 35px; line-height: 35px; background: transparent url('/img/core/loading_indicator.gif') no-repeat 0 0; padding-left: 35px;">
		${restartMessage}
	</div>

</body>
</html>