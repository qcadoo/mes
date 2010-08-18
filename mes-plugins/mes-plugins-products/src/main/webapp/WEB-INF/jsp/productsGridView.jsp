<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>


<html>
<head>

	<!--  <link rel="stylesheet" href="../css/slick.grid.css" type="text/css" />
	
	<link rel="stylesheet" href="../css/examples.css" type="text/css" />-->
	
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />

	<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
	<!-- <script type="text/javascript" src="../js/jquery-ui-1.8.2.custom.min.js"></script>
	<script type="text/javascript" src="../js/jquery.event.drag-2.0.min.js"></script>
	<script type="text/javascript" src="../js/slick.grid.js"></script> -->
	<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>

	<!--  <script>

		var grid;

		var columns = [
			<c:forEach items="${gridDefinition.columns}" var="column">
				{id:"${column.name}", name:"${column.name}", field:"${column.name}"},
			</c:forEach>
			/*{id:"title", name:"Title", field:"title"},
			{id:"duration", name:"Duration", field:"duration"},
			{id:"%", name:"% Complete", field:"percentComplete"},
			{id:"start", name:"Start", field:"start"},
			{id:"finish", name:"Finish", field:"finish"},
			{id:"effort-driven", name:"Effort Driven", field:"effortDriven"}*/
			
		];

		var options = {
				editable: true,
				enableAddRow: false,
				enableCellNavigation: true
		};

		$(function() {
            var data = [];
            var i = 0;
            <c:forEach items="${entities}" var="entity">
	    		var o = new Object();
	    		<c:forEach items="${gridDefinition.columns}" var="column">
	    			o["${column.name}"] = "${entity.fields[column.name]}";
	    		</c:forEach>
	    		data[i] = o;
	    		i++;
	        </c:forEach>
	        
			grid = new Slick.Grid($("#myGrid"), data, columns, options);
			
			//grid.ondblClickRow = function(id){
			grid.click = function(id){
		        alert(id);
		    };

		    grid.beforeSelectRow = function(rowid, status) {
		        alert("11");
		    }
		    		    
		})

		</script>-->
		
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
				height: 550, 
				/*colNames:['Inv No','Date', 'Client', 'Amount','Tax','Total','Notes'],
				colModel:[ 
					{name:'id',index:'id', width:60, sorttype:"int"}, 
					{name:'invdate',index:'invdate', width:90, sorttype:"date"}, 
					{name:'name',index:'name', width:100}, 
					{name:'amount',index:'amount', width:80, align:"right",sorttype:"float"}, 
					{name:'tax',index:'tax', width:80, align:"right",sorttype:"float"}, 
					{name:'total',index:'total', width:80,align:"right",sorttype:"float"}, 
					{name:'note',index:'note', width:150, sortable:false} 
				] */
				colNames: colNames,
				colModel: colModel
				//multiselect: true 
				//caption: "Manipulating Array Data" 
			}); 
			/*
			var mydata = [ 
				{id:"1",invdate:"2007-10-01",name:"test",note:"note",amount:"200.00",tax:"10.00",total:"210.00"}, 
				{id:"2",invdate:"2007-10-02",name:"test2",note:"note2",amount:"300.00",tax:"20.00",total:"320.00"}, 
				{id:"3",invdate:"2007-09-01",name:"test3",note:"note3",amount:"400.00",tax:"30.00",total:"430.00"}, 
				{id:"4",invdate:"2007-10-04",name:"test",note:"note",amount:"200.00",tax:"10.00",total:"210.00"}, 
				{id:"5",invdate:"2007-10-05",name:"test2",note:"note2",amount:"300.00",tax:"20.00",total:"320.00"}, 
				{id:"6",invdate:"2007-09-06",name:"test3",note:"note3",amount:"400.00",tax:"30.00",total:"430.00"}, 
				{id:"7",invdate:"2007-10-04",name:"test",note:"note",amount:"200.00",tax:"10.00",total:"210.00"}, 
				{id:"8",invdate:"2007-10-03",name:"test2",note:"note2",amount:"300.00",tax:"20.00",total:"320.00"}, 
				{id:"9",invdate:"2007-09-01",name:"test3",note:"note3",amount:"400.00",tax:"30.00",total:"430.00"} 
			]; 
			*/
			for(var i=0;i<=mydata.length;i++)
				jQuery("#list").jqGrid('addRowData',i+1,mydata[i]);
		}); 
	</script>
		

</head>
<body>

	<h2>${headerContent}</h2>
	
	<a href="#" class="fg-button ui-state-default ui-corner-all">New</a>
	
	
	<table id="list"></table> 
	<div id="pager"></div> 
	
	
	<!-- <div id="myGrid" style="width:600px;height:500px;"></div> -->
	
</body>
</html>