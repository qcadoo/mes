<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.3.0

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

<![CDATA[ERROR PAGE: ${errorHeader}##${errorExplanation}]]>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>QCADOO MES :: error</title>
		
		<link rel="shortcut icon" href="/qcadooView/public/img/core/icons/favicon.png">

		<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js"></script>
		
		<style type="text/css">
			body {
				background: #9B9B9B;
				color: white;
				font-family:Arial, Helvetica, sans-serif;
			}
			
			#content {
				width: 950px;
				margin: auto;
				margin-top: 20px;
			}
			#content #codeDiv {
				width: 130px;
				height: 130px;
				background-image: url('/qcadooView/public/img/core/error/errorCodeBg.png');
				background-repeat: no-repeat;
				font-size: 45px;
				text-align: center;
				padding-top: 37px;
				display: inline-block;
				vertical-align: top;
				margin-right: 10px;
			}
			#content #contentDiv {
				width: 800px;
				display: inline-block;
				color: #d7d7d7;
				font-size: 15px;
			}
			#content #contentDiv h1 {
				margin-top: 37px;
				margin-bottom: 10px;
				font-size: 45px;
				font-weight: normal;
				color: white;
			}
		
			#content #contentDiv #showExceptionLink {
				border: solid #d7d7d7 1px;
				color: #d7d7d7;
				text-decoration: none;
				margin-top: 15px;
				display: inline-block;
			}
			#content #contentDiv #showExceptionLink.expanded {
				color: #868686;
				background: #d7d7d7;
				border: solid #868686 1px;
				//border-bottom: solid transparent 1px;
				border-bottom: none;
			}
			#content #contentDiv #showExceptionLink.expanded:hover {
				background: #b7b7b7;
			}
			#content #contentDiv #showExceptionLink.expanded span#showExceptionLinkSignSpan {
				border-left: solid #868686 1px;
			}
			#content #contentDiv #showExceptionLink span {
				height: 100%;
				display: inline-block;
				padding: 5px;
			}
			#content #contentDiv #showExceptionLink span#showExceptionLinkTextSpan {
				width: 200px;
			}
			#content #contentDiv #showExceptionLink span#showExceptionLinkSignSpan {
				border-left: solid #d7d7d7 1px;
			}
			#content #contentDiv #showExceptionLink:hover {
				background: #868686;
			}
			#content #contentDiv #errorDetailsContent {
				background: #ffffff;
				border: solid #8F8F8F 1px;
				color: black;
				padding: 10px;
				font-size: 12px;
			}
			#content #contentDiv #errorDetailsContent .errorDetailsContentItem {
				margin-bottom: 15px;
			}
			#content #contentDiv #errorDetailsContent .errorDetailsContentItem .errorDetailsContentItemHeader {
				font-weight: bold;
				margin-bottom: 3px;
			}
			#content #contentDiv #errorDetailsContent .errorDetailsContentItem .errorDetailsContentItemContent {
				
			}
		</style>
		
		<script type="text/javascript">

			var showDetailsText = '<c:out value="${showDetailsText}"/>';
			var hideDetailsText = '<c:out value="${hideDetailsText}"/>';

			var errorDetailsVisible = false;
			
			jQuery(document).ready(function(){

				var errorDetailsContent = $("#errorDetailsContent");
				var showExceptionLink = $("#showExceptionLink");
				var showExceptionLinkTextSpan = $("#showExceptionLinkTextSpan");
				var showExceptionLinkSignSpan = $("#showExceptionLinkSignSpan");
				
				$("#showExceptionLink").click(function() {
					if (errorDetailsVisible) {
						errorDetailsContent.hide();
						showExceptionLink.removeClass("expanded");
						showExceptionLinkTextSpan.html(showDetailsText);
						showExceptionLinkSignSpan.html("+");
					} else {
						errorDetailsContent.show();
						showExceptionLink.addClass("expanded");
						showExceptionLinkTextSpan.html(hideDetailsText);
						showExceptionLinkSignSpan.html("-");
					}
					errorDetailsVisible = !errorDetailsVisible;
				});
			});
		</script>
		
	</head>
    <body>
    	<div id="content">
	        <div id="codeDiv">
	        	${code}
	        </div>
	        <div id="contentDiv">
	        		<h1>${errorHeader}</h1>
	        	<div>
	        		${errorExplanation}
	        	</div>
	        	<c:if test="${showDetails}">
	        	<div>
	        		<a href="#" id="showExceptionLink" class=""><span id="showExceptionLinkTextSpan">${showDetailsText}</span><span id="showExceptionLinkSignSpan">+</span></a>
	        		<div id="errorDetailsContent" style="display: none;">
	        		
	        			<div class="errorDetailsContentItem">
	        				<div class="errorDetailsContentItemHeader">${exceptionMessageText}</div>
	        				<div class="errorDetailsContentItemContent">${exceptionHeader}</div>
	        			</div>
	        			
	        			<div class="errorDetailsContentItem">
	        				<div class="errorDetailsContentItemHeader">${exceptionClassText}</div>
	        				<div class="errorDetailsContentItemContent">${exceptionClass}</div>
	        			</div>
	        		
	        		</div>
	        	</div>
	        	</c:if>
	        </div>
		</div>
    </body>
</html>