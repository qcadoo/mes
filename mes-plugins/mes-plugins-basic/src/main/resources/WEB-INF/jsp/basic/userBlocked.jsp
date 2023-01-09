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
        <title>${applicationDisplayName} :: error</title>

        <link rel="shortcut icon" href="/qcadooView/public/img/core/icons/favicon.png">

        <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js?ver=${buildNumber}"></script>

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

            #content #codeDivSad {
                width: 130px;
                height: 130px;
                background-image: url('/qcadooView/public/img/core/error/errorCodeBgSad.png');
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
            #content #contentDiv a {
                text-decoration: none;
            }
            #content #contentDiv h1 {
                margin-top: 37px;
                margin-bottom: 10px;
                font-size: 45px;
                font-weight: normal;
                color: white;
            }

            #content #contentDiv #showExceptionLink span {
                height: 100%;
                display: inline-block;
                padding: 5px;
            }

        </style>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=${buildNumber}" type="text/css" />
    </head>

    <body>
        <div id="content">

            <div id="codeDivSad"> </div>

            <div id="contentDiv">
                    <h1>${translationsMap["security.message.userBlocked.header"]}</h1>
                <div>
                     ${translationsMap["security.message.userBlocked.info"]}&nbsp;${adminEmail}
                </div>
            </div>
        </div>
    </body>

</html>
