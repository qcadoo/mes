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

<html lang="<c:out value="${languageCode}" />">
<head>

	<title>${applicationDisplayName}</title>
	
	<c:choose>
		<c:when test="${useCompressedStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-1.8.3.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.8.5.custom.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jqGrid.min.js?ver=2016_03_19_15_07"></script>

			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/packaged/jquery.noty.packaged.min.js?ver=2016_03_19_15_07"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/layouts/top.min.js?ver=2016_03_19_15_07"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/layouts/center.min.js?ver=2016_03_19_15_07"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/notyController.min.js?ver=2016_03_19_15_07"></script>

			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.js?ver=2016_03_19_15_07"></script>


		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/qcd.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/qcadoo-min.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/mainPage.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menuTopLevel.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/notification.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/jqModal.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
		
            <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-1.8.3.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.pnotify.js?ver=2016_03_19_15_07"></script>


			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.blockUI.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jqModal.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/logger.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/modal.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/connector.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/menu/model.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/menu/menuController.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/core/windowController.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/core/messagesController.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/utils/loadingIndicator.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/highlight.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/liveUpdate.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.menu-aim.js?ver=2016_03_19_15_07"></script>

			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/packaged/jquery.noty.packaged.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/layouts/top.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/layouts/center.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/alert/notyController.min.js?ver=2016_03_19_15_07"></script>

		</c:otherwise>
	</c:choose>
	
	<link rel="shortcut icon" href="/qcadooView/public/img/core/icons/favicon.png">
	
	<script type="text/javascript">

		var menuStructure = ${menuStructure}

		var windowController;

// ************ open request page

		jQuery(document).ready(function(){

			windowController = new QCD.WindowController(menuStructure);
			var notifications = new QCD.Notifications();
 			notifications.getNotifications();
			$("#mainPageIframe").load(function() {
				try {
					el = $('body', $('iframe').contents());
					el.click(function() {windowController.restoreMenuState()});
					$(document.getElementById('mainPageIframe').contentWindow.document).keydown(function(event){
					    var keycode = (event.keyCode ? event.keyCode : event.which);

                            if(event.ctrlKey){
                                if(keycode == 77){
                                    $logoDropdownBox.toggleClass('open');
                                    $userMenuBackdoor.toggleClass('open');
            						$('.subMenuBox').hide();
                                    $('.subMenuBox .maintainHover').removeClass('maintainHover');
                                    if($logoDropdownBox.hasClass('open')){
                                        $headerSearchInput.val('').keyup().focus();
            					        $('.mainMenu .maintainHover').removeClass('maintainHover');
            					        $('.mainMenu .currentMainActive').addClass('maintainHover');
            					        activateSubmenu($('.maintainHover', $mainMenu).parent());
                                    }
            					}
                            }
                            if($logoDropdownBox.hasClass('open')) {
                                if (keycode == 27) {
                                    $('.userMenuBackdoor').click();
                                }
                            }
					});

				} catch(e) {
				}
			});

			// ************ base variable
            			var $logoDropdownBox = $('.logoDropdownBox');
            			var $userMenuBackdoor = $('.userMenuBackdoor');
            			var $headerSearchInput = $('.headerSearchForm [type="text"]');
            			var $mainMenu = $('.mainMenu');

            			// ************ toogle menu visible by arrow
            			$('.logoDropdownBoxToggle a.arrow, .userMenuBackdoor').click(function(e){
            				$logoDropdownBox.toggleClass('open');
            				$userMenuBackdoor.toggleClass('open');
            				$('.subMenuBox').hide();
            				$('.subMenuBox .maintainHover').removeClass('maintainHover');
            				if($logoDropdownBox.hasClass('open')){
            					$headerSearchInput.val('').keyup().focus();
            					$('.mainMenu .maintainHover').removeClass('maintainHover');
            					$('.mainMenu .currentMainActive').addClass('maintainHover');
            					activateSubmenu($('.maintainHover', $mainMenu).parent());
            				}
            				e.preventDefault();
            			});
            // ************ main menu disabled click
            			$('.mainMenu a').click(function(e){
            				e.preventDefault();
            			});

            			// ************ main menu live search
            			$headerSearchInput.liveUpdate('.subMenuBoxLiveSearch .subMenu');


            			// ************ main menu search, clear
            			$('.headerSearchForm .iconDel').click(function(e){
            				$headerSearchInput.val('').keyup().blur();
            				e.preventDefault();
            			});

            var $menu = $(".mainMenu");
        	$menu.menuAim({
				activate: activateSubmenu,
	            deactivate: deactivateSubmenu
	        });

// ************ lazy menu show item
	        function activateSubmenu(row) {
	        	deactivateSubmenu($('.maintainHover', $mainMenu).parent());

	            var $row = $(row);
	            $row.find("a").addClass("maintainHover");
	        	var target = $row.find("a").attr('href');
				$("#"+target).show();
	        }

			// ************ lazy menu hide item
	        function deactivateSubmenu(row) {
				$('.subMenu .maintainHover').removeClass('maintainHover');

	            var $row = $(row);
	            $row.find("a").removeClass("maintainHover");
	        	var target = $row.find("a").attr('href');
				$("#"+target).hide();
	        }


	        			$('body').keydown(function(event) {
            				var keycode = (event.keyCode ? event.keyCode : event.which);

            				if(event.ctrlKey){
            					if(keycode == 77){
            					    $logoDropdownBox.toggleClass('open');
                                    $userMenuBackdoor.toggleClass('open');
            						$('.subMenuBox').hide();
                                    $('.subMenuBox .maintainHover').removeClass('maintainHover');
                                    if($logoDropdownBox.hasClass('open')){
                                        $headerSearchInput.val('').keyup().focus();
            					        $('.mainMenu .maintainHover').removeClass('maintainHover');
            					        $('.mainMenu .currentMainActive').addClass('maintainHover');
            					        activateSubmenu($('.maintainHover', $mainMenu).parent());
                                    }
            					}
            				}

            				if($logoDropdownBox.hasClass('open') && $.trim($headerSearchInput.val()).length < 1){

            					// enter
            					if(keycode == '13'){
            						if($('.subMenu .maintainHover').length > 0){
            							var href = $('.subMenu .maintainHover').parent().attr('id');
            							var itemParts = href.split("_");
            							$('.userMenuBackdoor').click();
                                        windowController.goToMenuPosition(itemParts[1] + "." + itemParts[2]);
            						//	openPage(href);
            						}
            					}

            					// down arrow
            					if(keycode == '40'){
            						// chek main or sub menu
            						if($('.subMenu .maintainHover').length < 1){
            							// main menu
            							var actualIndex = $('.maintainHover', $mainMenu).parent().index();
            					        if(actualIndex == $('li', $mainMenu).length - 1){
            					          actualIndex = -1;
            					        }
            							activateSubmenu($('li:eq(' + (actualIndex + 1) + ')', $mainMenu));
            						} else {
            							// sub menu
            							var actualIndex = $('.maintainHover', '.subMenu:visible').parent().index();
            							$('.maintainHover', '.subMenu:visible').removeClass('maintainHover');
            					        if(actualIndex == $('li', '.subMenu:visible').length - 1){
            					          actualIndex = -1;
            					        }
            							$('li:eq(' + (actualIndex + 1) + ') a', '.subMenu:visible').addClass('maintainHover');
            						}
            					}

            					// up arrow
            					if(keycode == '38'){
            						// chek main or sub menu
            						if($('.subMenu .maintainHover').length < 1){
            							// main menu
            							var actualIndex = $('.maintainHover', $mainMenu).parent().index();
            							if(actualIndex <= 0){
            								actualIndex = $('li', $mainMenu).length;
            							}
            							activateSubmenu($('li:eq(' + (actualIndex - 1) + ')', $mainMenu));
            						} else {
            							// sub menu
            							var actualIndex = $('.maintainHover', '.subMenu:visible').parent().index();
            							$('.maintainHover', '.subMenu:visible').removeClass('maintainHover');
            							if(actualIndex <= 0){
            								actualIndex = $('li', '.subMenu:visible').length;
            							}
            							$('li:eq(' + (actualIndex - 1) + ') a', '.subMenu:visible').addClass('maintainHover');
            						}
            					}

            					// right arrow
            					if(keycode == '39'){
            						if($('.subMenu .maintainHover').length < 1){
            							$('.subMenu:visible li:eq(0) a').addClass('maintainHover');
            						}
            					}

            					// left arrow
            					if(keycode == '37'){
            						if($('.subMenu .maintainHover').length > 0){
            							$('.subMenu a.maintainHover').removeClass('maintainHover');
            						}
            					}
            					
            					// escape
            					if($logoDropdownBox.hasClass('open')) {
                                    if (keycode == 27) {
                                        $('.userMenuBackdoor').click();
                                     }
                                 }

            				}
            			});

		});
        function openPage(href){
			alert(href);
			$('.userMenuBackdoor').click();
		}
		window.goToPage = function(url, serializationObject, isPage) {
			windowController.goToPage(url, serializationObject, isPage);
		}

		window.openModal = function(id, url, serializationObject, onCloseListener) {
			windowController.openModal(id, url, serializationObject, onCloseListener);
		}

		window.changeModalSize = function(width, height) {
			windowController.changeModalSize(width, height);
		}

		window.goBack = function(pageController) {
			windowController.goBack(pageController);
		}

		window.closeThisModalWindow = function(status) {
			windowController.closeThisModalWindow(status);
		}

		window.getLastPageController = function() {
			return windowController.getLastPageController();
		}

		window.goToLastPage = function() {
			windowController.goToLastPage();
		}

		window.onSessionExpired = function(serializationObject, isModal) {
			windowController.onSessionExpired(serializationObject, isModal);
		}

		window.addMessage = function(message) {
			windowController.addMessage(message);
		}

		window.onLoginSuccess = function() {
			windowController.onLoginSuccess();
		}

		window.goToMenuPosition = function(position) {
			windowController.goToMenuPosition(position);
		}

		window.activateMenuPosition = function(position) {
 			windowController.activateMenuPosition(position);
 		}

		window.hasMenuPosition = function(position) {
			return windowController.hasMenuPosition(position);
		}

		window.updateMenu = function() {
			windowController.updateMenu();
		}

		window.getCurrentUserLogin = function() {
			return "${userLogin}";
		}
	
		window.translationsMap = new Object();
		<c:forEach items="${commonTranslations}" var="translation">
			window.translationsMap["${translation.key}"] = "${translation.value}";
		</c:forEach>
	
		
	</script>

<!-- start Mixpanel -->
	<c:if test="${not empty mixpanelToken}">
		
		<script type="text/javascript">(function(e,b){if(!b.__SV){var a,f,i,g;window.mixpanel=b;a=e.createElement("script");a.type="text/javascript";a.async=!0;a.src=("https:"===e.location.protocol?"https:":"http:")+'//cdn.mxpnl.com/libs/mixpanel-2.2.min.js';f=e.getElementsByTagName("script")[0];f.parentNode.insertBefore(a,f);b._i=[];b.init=function(a,e,d){function f(b,h){var a=h.split(".");2==a.length&&(b=b[a[0]],h=a[1]);b[h]=function(){b.push([h].concat(Array.prototype.slice.call(arguments,0)))}}var c=b;"undefined"!==
		typeof d?c=b[d]=[]:d="mixpanel";c.people=c.people||[];c.toString=function(b){var a="mixpanel";"mixpanel"!==d&&(a+="."+d);b||(a+=" (stub)");return a};c.people.toString=function(){return c.toString(1)+".people (stub)"};i="disable track track_pageview track_links track_forms register register_once alias unregister identify name_tag set_config people.set people.set_once people.increment people.append people.track_charge people.clear_charges people.delete_user".split(" ");for(g=0;g<i.length;g++)f(c,i[g]);
		b._i.push([a,e,d])};b.__SV=1.2}})(document,window.mixpanel||[]);
		mixpanel.init("${mixpanelToken}");
		</script>
	</c:if>
<!-- end Mixpanel -->
</head>
<body>
    <div id="mainTopMenu" class="pageTopHeader clearfix">
        <div class="userMenuBackdoor"></div>
	    <div class="logoDropdownBox">
			<div class="logoDropdownBoxToggle">
				<div class="logo">
					<img src="/qcadooView/public/css/core/menu/images-new/qcadoo-logo.png" class="logoDark" alt="qcadoo MES logo" onclick="windowController.goToDashboard()">
					<img src="/qcadooView/public/css/core/menu/images-new/qcadoo-white-logo.png" class="logoWhite" alt="qcadoo MES logo" onclick="windowController.goToDashboard()">
				</div>
				<a href="#" class="arrow"">
					<i></i>
				</a>
			</div>
			<div class="logoDropdownBoxContent">
				<div class="headerSearchForm">

					<div class="headerSearchFormContent">
						<input type="text" value="" />
						<i class="icon iconSearch"></i>
						<a href="#" class="iconDel hidden">&times;</a>
					</div>
				</div>
				<div class="headerMenuBox">
					<div class="headerMenuContent">
						<div class="headerMenuRowMain">
                            <ul class='mainMenu'></ul>
						</div>
						<div class="headerMenuRowSub">
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="pageTitle">
        </div>
		<div class="userMenu">
		        <ul>
        				<li><a href="http://dokumentacja.qcadoo.com/" target="_blank" class="help"><i class="icon iconHelp"></i> ${commonTranslations["qcadooView.button.help"] }</a></li>
        				<li><i class="icon iconUser"></i> <a href='#' id="profileButton" onclick="windowController.goToMenuPosition('administration.profile')">${userLogin}</a>
        					<div class="userMenuDropdown">
        						<a href="#" class="toggle"><i class="icon iconDropdown"></i></a>
        						<ul>
        							<li>
        							    <a href='#' onclick="windowController.performLogout()"><i class="icon iconLogout"></i>${commonTranslations["qcadooView.button.logout"] }</a>
        							</li>
        						</ul>
        					</div>
        				</li>
        		</ul>
        </div>

	</div>
	<div id="mainPageIframeWrapper"><iframe id="mainPageIframe" frameborder="0"></iframe></div>
</body>
</html>