var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.grid = QCD.components.elements.grid || {};

QCD.components.elements.grid.GridModel = function(_elementPath) {

	var elementPath = _elementPath;
	
	var globalColumnTranslations = new Object();

	var columnModel;
	
	var entities;
	
	return {
		parseColumns: function(options) {
			columnModel = new Object();
			
			var colNames = new Array();
			var colModel = new Array();
			var isfiltersEnabled = false;
			
			for (var i in options.columns) {
				var column = options.columns[i];
				columnModel[column.name] = column;
				
				var isSortable = false;
				var isSerchable = false;
				for (var sortColIter in options.orderableColumns) {
					if (options.orderableColumns[sortColIter] == column.name) {
						isSortable = true;
						break;
					}
				}
				for (var sortColIter in options.searchableColumns) {
					if (options.searchableColumns[sortColIter] == column.name) {
						isSerchable = true;
						isfiltersEnabled = true;
						break;
					}
				}
				column.isSerchable = isSerchable;
				
				colNames.push(column.label+"<div class='sortArrow' id='"+elementPath+"_sortArrow_"+column.name+"'></div>");
				
				var stype = 'text';
				var searchoptions = {};
				if (column.filterValues) {
					var possibleValues = new Object();
					possibleValues[""] = "";
					for (var i in column.filterValues) {
						possibleValues[i] = column.filterValues[i];
					}
					stype = 'select';
					searchoptions.value = possibleValues;
					searchoptions.defaultValue = "";
				}
				
				var col = {name:column.name, index:column.name, width:column.width, sortable: isSortable, resizable: true, 
						align: column.align, stype: stype, searchoptions: searchoptions
				};
				
				if (searchoptions.value) {
					globalColumnTranslations[column.name] = searchoptions.value;
					col.formatter = function(cellvalue, options, rowObject) {
						return globalColumnTranslations[options.colModel.name][cellvalue];
					}
				}
				
				colModel.push(col);
				
			}
			
			return {
				colNames: colNames,
				colModel: colModel,
				isfiltersEnabled: isfiltersEnabled
			};
		}
	}
	
}