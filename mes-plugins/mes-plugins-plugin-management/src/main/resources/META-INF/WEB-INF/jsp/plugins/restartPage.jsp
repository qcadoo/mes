<%--

    ********************************************************************
    Code developed by amazing QCADOO developers team.
    Copyright © Qcadoo Limited sp. z o.o. (2010)
    ********************************************************************

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

	${restartMessage}

</body>
</html>