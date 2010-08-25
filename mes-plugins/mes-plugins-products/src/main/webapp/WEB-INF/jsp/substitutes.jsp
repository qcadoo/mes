
	
	<script type="text/javascript">

		var substitutesColNames = new Array();
		var substitutesColModel = new Array();
		<c:forEach items="${substituteGridDefinition.columns}" var="column">
			substitutesColNames.push("<spring:message code="substitutes.column.${column.name}"/>");
			substitutesColModel.push({name:"${column.name}", index:"${column.name}", width:100, sortable: false});
		</c:forEach>
		var substitutesGrid;

		var substituteComponentsColNames = new Array();
		var substituteComponentsColModel = new Array();
		<c:forEach items="${substituteComponentGridDefinition.columns}" var="column">
			substituteComponentsColNames.push("<spring:message code="substitutes.column.${column.name}"/>");
			substituteComponentsColModel.push({name:"${column.name}", index:"${column.name}", width:100, sortable: false});
			console.info("${column.fields[0].name}");
		</c:forEach>
		var substituteComponentsGrid;
		
		jQuery(document).ready(function(){
			substitutesGrid = new QCDGrid({
				element: 'substitutesGrid',
				dataSource: "substitute/data.html?productId=${entityId }",
				deleteUrl: "substitute/deleteSubstitute.html",
				height: 150, 
				paging: false,
				colNames: substitutesColNames,
				colModel: substitutesColModel,
				loadingText: 'Wczytuje...',
				onSelectRow: function(id){
			        console.debug('row '+id);
			        substituteComponentsGrid.setOption('dataSource','substitute/components.html?productId=${entityId }&substituteId='+id);
			        substituteComponentsGrid.refresh();
			        $("#newSubstituteComponentButton").attr("disabled", false);
			        $("#deleteSubstituteButton").attr("disabled", false);
			        $("#upSubstituteButton").attr("disabled", false);
			        $("#downSubstituteButton").attr("disabled", false);
		        }
			});

			substituteComponentsGrid = new QCDGrid({
				element: 'substituteComponentsGrid',
				deleteUrl: "substitute/deleteSubstituteComponent.html",
				height: 150, 
				paging: false,
				colNames: substituteComponentsColNames,
				colModel: substituteComponentsColModel,
				loadingText: 'Wczytuje...',
				onSelectRow: function(id){
					$("#deleteSubstituteComponentButton").attr("disabled", false);
				}
			});

			 $("#newSubstituteComponentButton").attr("disabled", true);
			 $("#deleteSubstituteComponentButton").attr("disabled", true);
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
			<button id="deleteSubstituteButton" onClick="substitutesGrid.deleteSelectedRecords()"><spring:message code="addModifyEntity.delete"/></button>
			<button id="upSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.up"/></button>
			<button id="downSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.down"/></button>
		</div>
		<table id="substitutesGrid"></table>
		<div>
			Produkty substytutu:
			<button id="newSubstituteComponentButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.new"/></button>
			<button id="deleteSubstituteComponentButton" onClick="substituteComponentsGrid.deleteSelectedRecords()"><spring:message code="addModifyEntity.delete"/></button>
		</div>
		<table id="substituteComponentsGrid"></table> 




