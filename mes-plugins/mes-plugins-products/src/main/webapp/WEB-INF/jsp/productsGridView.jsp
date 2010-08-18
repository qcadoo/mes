<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>


<html>
<head>
	
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />

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
				colModel.push({name:"${column.name}",index:"${column.name}", width:100});
			</c:forEach>

			var mydata = new Array();
			<c:forEach items="${entities}" var="entity">
	    		var o = new Object();
	    		<c:forEach items="${gridDefinition.columns}" var="column">
	    			o["${column.name}"] = "${entity.fields[column.name]}";
	    		</c:forEach>
	    		mydata.push(o);
	        </c:forEach>
			 
			jQuery("#list").jqGrid({
				datatype: "local", 
				height: 500, 
				colNames: colNames,
				colModel: colModel,
				ondblClickRow: function(id){
			        window.location='editEntity.html?entityId='+id
		        }

			}); 

			jQuery("#recordsNumberSelect").val(10)
			refresh();
			
		}); 

		var thisFirst = 0;
		var thisMax = 10;
		
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
			thisMax = parseInt(jQuery("#recordsNumberSelect").val());
			refresh();
		}

		function refresh() {
			jQuery("#previousPageButton").attr("disabled", true);
			jQuery("#nextPageButton").attr("disabled", true);
			jQuery("#recordsNumberSelect").attr("disabled", true);
			jQuery("#list").jqGrid('clearGridData');
			jQuery('#list').block({ message: 'Wczytuje...', showOverlay: false,  fadeOut: 1000, fadeIn: 0,css: { 
	            border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#000', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: .5, 
	            color: '#fff' } });
			 $.getJSON("listData.html", {'maxResults' : thisMax, 'firstResult': thisFirst}, function(response) {
			       for (var entityNo in response) {
				       var entity = response[entityNo];
				       jQuery("#list").jqGrid('addRowData',entity.id,entity.fields);
			       }
			       jQuery('#list').unblock();
			       if (thisFirst > 0) {
			       	jQuery("#previousPageButton").attr("disabled", false);
			       }
					jQuery("#nextPageButton").attr("disabled", false);
					jQuery("#recordsNumberSelect").attr("disabled", false);
			    });
		}

		
	</script>
		

</head>
<body>

	<h2>${headerContent}</h2>
	
	<button onClick="window.location='newEntity.html'">New</button>
	
	
	<table id="list"></table> 
	
	<button id="previousPageButton" onClick="prev()">prev</button>
	<button id="nextPageButton" onClick="next()">next</button>
	<select id="recordsNumberSelect" onChange="selectChange()">
		<option value=10>10</option>
		<option value=20>20</option>
		<option value=50>50</option>
		<option value=100>100</option>
	</select>
	
</body>
</html>