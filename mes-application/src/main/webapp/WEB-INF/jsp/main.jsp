<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<title>QCADOO MES</title>
	
	<link rel="stylesheet" href="css/mainPage.css" type="text/css" />
	<link rel="stylesheet" href="css/menuTopLevel.css" type="text/css" />
	<link rel="stylesheet" href="css/menu.css" type="text/css" />
	<link rel="stylesheet" href="css/menuRibbon.css" type="text/css" />
	
	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="js/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="js/qcd/menu/model.js"></script>
	<script type="text/javascript" src="js/qcd/menu/menuController.js"></script>
	<script type="text/javascript" src="js/qcd/menu/ribbon.js"></script>
	<script type="text/javascript" src="js/qcd/core/windowController.js"></script>
	
	<script type="text/javascript"><!--

		/*var menuStructure = {
			menuItems: [
				{
					name: "home",
					label: "home",
					ribbonItems: [
						{
							name: "testgroup1",
							items: [
								{name: "big1", type: "bigButton", label: "b1", clickAction: "goToView:products.testViewB1"},
								{name: "big2", type: "bigButton"},
								{name: "small1", type: "smallButton", label:"s1", clickAction: "goToView:products.testViewS1"},
								{name: "small2", type: "smallButton"},
								{name: "small3", type: "smallButton"},
								{name: "small4", type: "smallButtonDropdown", label:"s4", clickAction: "goToView:products.testViewS4", items: [
									{name: "small4-1", label:"s41", clickAction: "goToView:products.testViewS41"},
									{name: "small4-2"}
								]},
								{name: "small5 das dsa das da", type: "smallButtonDropdown", items: [
   									{name: "small5-1"},
   									{name: "small5-2"}
   								]},
   								{name: "small6", type: "smallButton"},
								{name: "big3", type: "bigButton"},
								{name: "big4", type: "bigButtonDropdown", label:"B4", clickAction: "goToView:products.testViewB4", items: [
									{name: "item1", label:"B41", clickAction: "goToView:products.testViewB41"},
									{name: "item2"},
									{name: "item3"},
									{name: "item4"}
								]},
								{name: "big5", type: "bigButtonDropdown", items: [
   									{name: "item5-1"},
   									{name: "item5-2"},
   									{name: "item5-3"}
   								]}
							]
						}
					]
					
				},
				{
					name: "test1",
					label: "test1",
					items: [
						{name: "test1-1", label: "test1-1"},
						{name: "test1-2", label: "test1-2",
							ribbonItems: [
								{
									name: "testgroup1",
									items: [
										{name: "big1", type: "bigButton"},
										{name: "big2", type: "bigButton"},
										{name: "small1", type: "smallButton"},
										{name: "small2", type: "smallButton"},
										{name: "small3", type: "smallButton"},
										{name: "big3", type: "bigButton"}
									]
								},
								{
									name: "testgroup2",
									items: [
										{name: "small4", type: "smallButton"},
										{name: "small5", type: "smallButton"},
										{name: "big4", type: "bigButton"}
									]
								}
							]
						},
						{name: "test1-3", label: "test1-3"}
					]
				},
				{
					name: "test2",
					label: "test2",
					items: [
						{name: "test2-1", label: "test2-1"},
						{name: "test2-2", label: "test2-2"}
					]
				},
				{
					name: "test3",
					label: "test3"
				},
				{
					name: "products",
					label: "produkty",
					ribbonItems: [
						{
							name: "navigation",
							items: [
								{name: "list", label: "lista produktow", type: "bigButton"},
								{name: "new", label: "nowy produkt", type: "smallButton"},
								{name: "edit", label: "edytuj produkt", type: "smallButton"}
							]
						},
						{
							name: "actions",
							items: [
								{name: "save", label: "zapisz", type: "bigButton"},
								{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
								{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
								{name: "cancel", label: "anuluj", type: "smallButton"},
								{name: "delete", label: "usun", type: "smallButton"}
							]
						}
					]
				}
			]
		};*/

		var menuStructure = {
				menuItems: [
					{
						name: "home",
						label: "home",
						ribbonItems: [
							{
								name: "home",
								items: [
									{name: "dashboard", type: "bigButton", label: "dashboard"}
								]
							}
						]
					},
					{
						name: "products",
						label: "Zarządzanie Produktami",
						items: [
							{name: "products", label: "Produkty", ribbonItems: [
           						{
           							name: "navigation",
           							items: [
           								{name: "list", label: "lista", type: "bigButton"},
           								{name: "new", label: "nowy", type: "bigButton"}
           								//{name: "edit", label: "edytuj", type: "smallButton"}
           							]
           						},
           						{
           							name: "actions1",
           							items: [
           								{name: "save", label: "zapisz", type: "bigButton"},
           								{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
           								{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
           								{name: "cancel", label: "anuluj", type: "smallButton"},
           								{name: "delete", label: "usun", type: "smallButton"}
           							]
           						},
           						{
           							name: "actions2",
           							items: [
           								{name: "edit", label: "edytuj", type: "bigButton"},
           								{name: "delete", label: "usun", type: "smallButton"}
           							]
           						}
           					]},
							{name: "instructions", label: "Instrukcje materiałowe", ribbonItems: [
	        					{
	      							name: "navigation",
	      							items: [
	      								{name: "list", label: "lista", type: "bigButton"},
	      								{name: "new", label: "nowy", type: "smallButton"},
	      								{name: "edit", label: "edytuj", type: "smallButton"}
	      							]
	      						},
	      						{
	      							name: "actions",
	      							items: [
	      								{name: "save", label: "zapisz", type: "bigButton"},
	      								{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
	      								{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
	      								{name: "cancel", label: "anuluj", type: "smallButton"},
	      								{name: "delete", label: "usun", type: "smallButton"}
	      							]
	      						}
	      					]}
						]
					},
					{
						name: "orders",
						label: "Zlecenia produkcyjne",
						ribbonItems: [
							{
								name: "navigation",
								items: [
									{name: "list", label: "lista", type: "bigButton"},
									{name: "new", label: "nowy", type: "smallButton"},
									{name: "edit", label: "edytuj", type: "smallButton"}
								]
							},
							{
								name: "actions",
								items: [
									{name: "save", label: "zapisz", type: "bigButton"},
									{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
									{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
									{name: "cancel", label: "anuluj", type: "smallButton"},
									{name: "delete", label: "usun", type: "smallButton"}
								]
							}
						]
					},
					{
						name: "dictionaries",
						label: "Słowniki",
						ribbonItems: [
							{
								name: "navigation",
								items: [
									{name: "list", label: "lista", type: "bigButton"},
									{name: "values", label: "pokaz wartosci", type: "smallButton"}
								]
							},
							{
								name: "actions",
								items: [
									{name: "save", label: "zapisz", type: "bigButton"},
									{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
									{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
									{name: "cancel", label: "anuluj", type: "smallButton"},
									{name: "delete", label: "usun", type: "smallButton"}
								]
							}
						]
					},
					{
						name: "administration",
						label: "Administracja",
						items: [
							{name: "users", label: "Użytkownicy i Grupy"},
							{name: "plugins", label: "Pluginy"}
						]
					}
				]
			};

		var windowController;
		
		jQuery(document).ready(function(){
			windowController = new QCD.WindowController(menuController);

			var menuController = new QCD.menu.MenuController(menuStructure, windowController);
			
			$("#mainPageIframe").load(function() {
				el = $('body', $('iframe').contents());
				el.click(function() {menuController.restoreState()});
			});
		});
	
	--></script>

</head>
<body>

	<table id="mainStructuralTable" cellspacing="0" cellpadding="0">
		<tr id="mainHeaderRow">
			<td id="mainHeaderCell">
				<div id="topLevelMenu">
					<img id="logoImage" src="css/images/logo_small.png"></img>
					<div id="topRightPanel">
						<button onclick="windowController.performLogout()">${commonTranslations["commons.button.logout"] }</button>
					</div>
				</div>
				<div id="firstLevelMenu">
				</div>
				<div id="secondLevelMenu">
				</div>
				<div id="ribbonLevelMenu">
				</div>
			</td>
		</tr>
		<tr id="mainContentRow">
			<td class="noMargin">
				<iframe id="mainPageIframe" src="testPage.html">
				</iframe>
			</td>
		</tr>
	</table>

</body>
</html>