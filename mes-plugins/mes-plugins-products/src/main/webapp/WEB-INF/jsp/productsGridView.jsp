<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>


<html>
<head>
	
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../css/productGrid.css" type="text/css" />

	<script type="text/javascript" src="../js/json_sans_eval.js"></script>
	<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../js/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>

	<script type="text/javascript">
		jQuery(document).ready(function(){

			var colNames = new Array();
			var colModel = new Array();
			<c:forEach items="${gridDefinition.columns}" var="column">
				colNames.push("${column.name}");
				colModel.push({name:"${column.fields[0].name}", index:"${column.fields[0].name}", width:100, sortable: false});
			</c:forEach>

			/*
			var mydata = new Array();
			<c:forEach items="${entities}" var="entity">
	    		var o = new Object();
	    		<c:forEach items="${gridDefinition.columns}" var="column">
	    			o["${column.name}"] = "${entity.fields[column.name]}";
	    		</c:forEach>
	    		mydata.push(o);
	        </c:forEach>*/
			 
			$("#list").jqGrid({
				datatype: "local", 
				height: 450, 
				colNames: colNames,
				colModel: colModel,
				multiselect: true,
				emptyDataText:'There are no records. If you would like to add one, click the "Add New ...',
				ondblClickRow: function(id){
			        window.location='editEntity.html?entityId='+id
		        }

			}); 

			$("#recordsNumberSelect").val(10)
			refresh();
			
		}); 

		var thisFirst = 0;
		var thisMax = 10;
		var totalNumberOfEntities;
		
		function prev() {
			thisFirst -= thisMax;
			if (thisFirst < 0) {
				thisFirst = 0;
			}
			refresh();
		}

		function next() {
			thisFirst += thisMax;
			refresh();
		}

		function selectChange() {
			thisMax = parseInt($("#recordsNumberSelect").val());
			refresh();
		}

		function refresh() {
			jQuery("#list").jqGrid('clearGridData');
			blockList();
			$.getJSON("listData.html", {'maxResults' : thisMax, 'firstResult': thisFirst}, function(response) {
				totalNumberOfEntities = response.totalNumberOfEntities;
				for (var entityNo in response.entities) {
					var entity = response.entities[entityNo];
					$("#list").jqGrid('addRowData',entity.id,entity.fields);
				}	       
				unblockList();
			});
		}

		function deleteSelectedRecords() {
			if (window.confirm("delete?")) {
				blockList();
				var selectedRows = jQuery("#list").getGridParam("selarrrow");
				var dataString = JSON.stringify(selectedRows);
				$.ajax({
					url: 'deleteData.html',
					type: 'POST',
					dataType: 'json',
					data: dataString,
					contentType: 'application/json; charset=utf-8',
					success: function(response) {
						if (response != "ok") {
							alert(response);
						}
						refresh();
					}
				});
			}
		}

		function blockList() {
			$('#list').block({ message: 'Wczytuje...', showOverlay: false,  fadeOut: 1000, fadeIn: 0,css: { 
	            border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#000', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: .5, 
	            color: '#fff' } });
			$("#previousPageButton").attr("disabled", true);
			$("#nextPageButton").attr("disabled", true);
			$("#recordsNumberSelect").attr("disabled", true);
		}

		function unblockList() {
			$('#list').unblock();
			refreshBottomButtons();
		}

		function refreshBottomButtons() {
			if (thisFirst > 0) {
				$("#previousPageButton").attr("disabled", false);
			}
			if (thisFirst + thisMax < totalNumberOfEntities) {
				$("#nextPageButton").attr("disabled", false);
			}
			$("#recordsNumberSelect").attr("disabled", false);
			var pagesNo = Math.ceil(totalNumberOfEntities / thisMax);
			if (pagesNo == 0) {
				pagesNo = 1;
			}
			var currPage = Math.ceil(thisFirst / thisMax) + 1;
			$("#pageNoSpan").html(currPage);
			$("#allPagesNoSpan").html(pagesNo);
		}		
		
	</script>
		

</head>
<body>

	<div id="pageHeader">${headerContent}</div>
	
	<div id="messageBox">${message}</div>
	
	<div id="topButtons">
		<button onClick="window.location='newEntity.html'">New</button>
		<button onClick="refresh()">refresh</button>
		<button onClick="deleteSelectedRecords()">delete</button>
	</div>
	
	<table id="list"></table> 
	
	<div id="bottomButtons">
		<button id="previousPageButton" onClick="prev()">prev</button>
		
		<select id="recordsNumberSelect" onChange="selectChange()">
			<option value=10>10</option>
			<option value=20>20</option>
			<option value=50>50</option>
			<option value=100>100</option>
		</select>
		
		<span id="pageInfoSpan">
			<span>page</span>
			<span id="pageNoSpan"></span>
			<span>/</span>
			<span id="allPagesNoSpan"></span>
		</span>
		
		<button id="nextPageButton" onClick="next()">next</button>
	</div>
</body>
</html>