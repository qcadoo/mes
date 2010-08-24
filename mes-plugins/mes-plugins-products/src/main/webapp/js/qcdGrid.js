
QCDGrid = function(opts) {
	
	// PRIVATE
	
	var options;
	
	var grid;
	
	var thisFirst = null;
	var thisMax = null;
	var totalNumberOfEntities = null;
	
	var sortColumn = null;
	var sortOrder = null;
	
	var paging_prevButton = null;
	var paging_nextButton = null;
	var paging_recordsNoSelect = null;
	var paging_pageNoSpan = null;
	var paging_allPagesNoSpan = null;
	
	var defaultOptions = {
		paging: true
	};
	
	function parseOptions(opts) {
		if (!opts.element) {
			throw("no element definied");
		}
		for (var opName in defaultOptions) {
			if (opts[opName] == undefined) {
				opts[opName] = defaultOptions[opName];
			}
		}
		return opts;
	}
	
	function paging_prev() {
		thisFirst -= thisMax;
		if (thisFirst < 0) {
			thisFirst = 0;
		}
		refresh();
	}

	function paging_next() {
		thisFirst += thisMax;
		refresh();
	}

	function paging_onRecordsNoSelectChange() {
		thisMax = parseInt(paging_recordsNoSelect.val());
		refresh();
	}
	
	function blockList() {
		grid.block({ message: options.loadingText, showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: '#000', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: .5, 
            color: '#fff' } });
		if (options.paging) {
			paging_prevButton.attr("disabled", true);
			paging_nextButton.attr("disabled", true);
			paging_recordsNoSelect.attr("disabled", true);
		}
	}

	function unblockList() {
		grid.unblock();
		paging_refreshBottomButtons();
	}

	function paging_refreshBottomButtons() {
		if (options.paging) {
			if (thisFirst > 0) {
				paging_prevButton.attr("disabled", false);
			}
			if (thisFirst + thisMax < totalNumberOfEntities) {
				paging_nextButton.attr("disabled", false);
			}
			paging_recordsNoSelect.attr("disabled", false);
			var pagesNo = Math.ceil(totalNumberOfEntities / thisMax);
			if (pagesNo == 0) {
				pagesNo = 1;
			}
			var currPage = Math.ceil(thisFirst / thisMax) + 1;
			paging_pageNoSpan.html(currPage);
			paging_allPagesNoSpan.html(pagesNo);
		}
	}		
	
	function debug($msg) {
		if (window.console && window.console.debug) {
			window.console.debug($msg);
		}
	};
	function info($msg) {
		if (window.console && window.console.info) {
			window.console.info($msg);
		}
	};
	
	// CONSTRUCTOR
	
	function constructor(opts) {
		console.info("constructor");
		
		options = parseOptions(opts);
		
		
		var element = $("#"+opts.element);
		
		if (options.paging) {
			//create paging div
			
			var pagingDiv = $("<div>").addClass('qcdGrid_paging');
				paging_prevButton =  $("<button>").html('prev');
				pagingDiv.append(paging_prevButton);
				
				paging_recordsNoSelect = $("<select>");
					paging_recordsNoSelect.append("<option value=10>10</option>");
					paging_recordsNoSelect.append("<option value=20>20</option>");
					paging_recordsNoSelect.append("<option value=50>50</option>");
					paging_recordsNoSelect.append("<option value=100>100</option>");
				pagingDiv.append(paging_recordsNoSelect);
				
				var pageInfoSpan = $("<span>").addClass('qcdGrid_paging_pageInfo');
					pageInfoSpan.append('<span>page</span>');
					paging_pageNoSpan = $("<span>");
					pageInfoSpan.append(paging_pageNoSpan);
					pageInfoSpan.append('<span>/</span>');
					paging_allPagesNoSpan = $("<span>");
					pageInfoSpan.append(paging_allPagesNoSpan);
				pagingDiv.append(pageInfoSpan);
				
				paging_nextButton =  $("<button>").html('next');
				pagingDiv.append(paging_nextButton);
			
				paging_prevButton.click(function() {paging_prev();});
				paging_recordsNoSelect.change(function() {paging_onRecordsNoSelectChange();});
				paging_nextButton.click(function() {paging_next(); });
				
			element.after(pagingDiv);
			
			thisFirst = 0;
			thisMax = 10;
		}
		
		options.datatype = 'local';
		grid = element.jqGrid(options);
	}
	
	// PUBLIC
	
	this.setSortOptions = function(_sortColumn, _sortOrder) {
		sortColumn = _sortColumn;
		sortOrder = _sortOrder;
	}
	
	this.setOption = function(key, value) {
		options[key] = value;
	}
	
	function refresh() {
		//$this.jqGrid('clearGridData');
		grid.jqGrid('clearGridData');
		blockList();
		var parameters = new Object();
		if (thisMax) {
			parameters.maxResults = thisMax;
		}
		if (thisFirst != null) {
			parameters.firstResult = thisFirst;
		}
		if (sortColumn && sortOrder) {
			parameters.sortColumn = sortColumn;
			parameters.sortOrder = sortOrder;
		}
		$.getJSON(options.dataSource, parameters, function(response) {
			totalNumberOfEntities = response.totalNumberOfEntities;
			for (var entityNo in response.entities) {
				var entity = response.entities[entityNo];
				grid.jqGrid('addRowData',entity.id,entity.fields);
			}	       
			unblockList();
		});
	}
	
	this.refresh = refresh;
	
	this.deleteSelectedRecords = function() {
		if (window.confirm("delete?")) {
			debug("delete");
			blockList();
			var selectedRows = grid.getGridParam("selarrrow");
			
			var dataString = JSON.stringify(selectedRows);
			debug(dataString);
			$.ajax({
				url: options.deleteUrl,
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
	
	
	constructor(opts);
	
};
