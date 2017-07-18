<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo Framework
    Version: 1.4

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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js?ver=2016_03_19_15_07"></script>

	<link rel="shortcut icon" href="/qcadooView/public/img/core/icons/favicon.png">
	
	<title>${applicationDisplayName}</title>
	
	<style type="text/css">
	
	</style>
		
	<script type="text/javascript">

		jQuery(document).ready(function(){
			$("#languageSelect").val("${currentLanguage}");
		});
	
		changeLanguage = function(language) {
			window.location = "browserNotSupported.html?lang="+language;
		}
		
	</script>
	
	<style type="text/css">
		body {
			background-color: white;
			font-family:"Lucida Grande",Arial, Helvetica, sans-serif;
			margin: 0;
			padding: 0;
		}
		#header {
			padding: 5px 10px;
			background-color: #ebebeb;
			border-bottom: solid #a7a7a7 1px;
		}
		#header #headerText {
			display: inline-block;
		}
		#header #languageSelect {
			float: right;
		}
		#content {
			padding: 20px;
		}
		#content #browswersList {
			border-top: solid #a7a7a7 1px;
			margin-top: 20px;
			padding-top: 20px;
		}
		#content #browswersList ul {
			margin-top: 10px;
		}
		#content #browswersList ul li {
			margin-top: 7px;
		}
		#content #browswersList ul li a,
		#content #browswersList ul li a:link,
		#content #browswersList ul li a:visited {
			color: #005EC5;
			text-decoration: none;
		}
		#content #browswersList ul li a:hover {
			text-decoration: underline;
		}
		#content #browswersList ul li a:active {
			color: red;
		}
		#content #browswersList ul li .fromVersionText {
			color: #777777;
		}
		#content #browswersList ul li .fromVersionNumber {
			color: black;
		}
	</style>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
	
</head>
<body >
	<div id="header">
		<span id="headerText">
			${translation["qcadooView.browserNotSupported.header"]}
		</span>
 		<select id="languageSelect" onchange="changeLanguage(this.value)">
 			<c:forEach items="${locales}" var="localesEntry">
 				<option value="${localesEntry.key}">${localesEntry.value}</option>
 			</c:forEach>
 		</select>
 	</div>
				 	
	<div id="content">
	
		<div>
			${translation["qcadooView.browserNotSupported.content"]}
		</div>
		
		<div id="browswersList">
			<div>
				${translation["qcadooView.browserNotSupported.listHeader"]}
			</div>
			<ul>
				<li>
					<a href="http://www.google.com/chrome" target="_blank">Chrome</a>
				</li>
				<li>
					<a href="http://www.firefox.com" target="_blank">Firefox</a>
					<span class="fromVersionText">${translation["qcadooView.browserNotSupported.listFromVersion"]}</span>
					<span class="fromVersionNumber">3.0</span>
				</li>
				<li>
					<a href="http://windows.microsoft.com/en-US/internet-explorer/products/ie/home" target="_blank">Internet Explorer</a>
					<span class="fromVersionText">${translation["qcadooView.browserNotSupported.listFromVersion"]}</span>
					<span class="fromVersionNumber">8</span>
				</li>
				<li>
					<a href="http://www.apple.com/safari/" target="_blank">Safari</a>
				</li>
			</ul>
		</div>
	</div>
	
</body>
</html>