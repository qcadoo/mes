
	
	<script type="text/javascript">

		var colNames = new Array();
		var colModel = new Array();
			colNames.push("f1");
			colModel.push({name:"f1", index:"f1", width:100, sortable: false});
			colNames.push("f2");
			colModel.push({name:"f2", index:"f2", width:100, sortable: false});
		var substitutesGrid;

		var spColNames = new Array();
		var spColModel = new Array();
			spColNames.push("f11");
			spColModel.push({name:"f11", index:"f11", width:100, sortable: false});
			spColNames.push("f12");
			spColModel.push({name:"f12", index:"f12", width:100, sortable: false});
		var substituteProductsGrid;
		
		jQuery(document).ready(function(){
			var substitutesGrid = new QCDGrid({
				element: 'substitutesGrid',
				dataSource: "substitute/data.html?productId=${entityId }",
				height: 150, 
				paging: false,
				colNames: colNames,
				colModel: colModel,
				loadingText: 'Wczytuje...',
				onSelectRow: function(id){
			        console.debug('row '+id);
			        substituteProductsGrid.setOption('dataSource','substitute/products.html?productId=${entityId }&substituteId='+id);
			        substituteProductsGrid.refresh();
			        $("#newSubstituteProductButton").attr("disabled", false);
			        $("#deleteSubstituteButton").attr("disabled", false);
			        $("#upSubstituteButton").attr("disabled", false);
			        $("#downSubstituteButton").attr("disabled", false);
		        }
			});

			substituteProductsGrid = new QCDGrid({
				element: 'substituteProductsGrid',
				height: 150, 
				paging: false,
				colNames: spColNames,
				colModel: spColModel,
				loadingText: 'Wczytuje...',
				onSelectRow: function(id){
					$("#deleteSubstituteProductButton").attr("disabled", false);
				}
			});

			 $("#newSubstituteProductButton").attr("disabled", true);
			 $("#deleteSubstituteProductButton").attr("disabled", true);
			 $("#deleteSubstituteButton").attr("disabled", true);
			 $("#upSubstituteButton").attr("disabled", true);
			 $("#downSubstituteButton").attr("disabled", true);

			 if ("${entityId }") {
			 	substitutesGrid.refresh();
			 } else {
				 $("#newSubstituteButton").attr("disabled", true);
			 }
		});
		
	
	</script>
		
		
		<div>
			Substytuty:
			<button id="newSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.new"/></button>
			<button id="deleteSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.delete"/></button>
			<button id="upSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.up"/></button>
			<button id="downSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.down"/></button>
		</div>
		<table id="substitutesGrid"></table>
		<div>
			Produkty substytutu:
			<button id="newSubstituteProductButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.new"/></button>
			<button id="deleteSubstituteProductButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.delete"/></button>
		</div>
		<table id="substituteProductsGrid"></table> 




