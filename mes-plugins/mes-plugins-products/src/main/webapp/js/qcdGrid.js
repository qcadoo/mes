
(function($) {
	
		// CONSTRUCTOR
	$.fn.qcdGrid = function(options) {
		
		var main_opts = $.extend({}, $.fn.qcdGrid.defaults, options);
		
		debug('qcdGrid constructor');
		
		this.each(function() {
			$this = $(this);
			$this.options = $.meta ? $.extend({}, main_opts, $this.data()) : main_opts;
			console.debug($this.options);
			
				//create paging div
			$paging_prevButton = null;
			$paging_nextButton = null;
			$paging_recordsNoSelect = null;
			$paging_pageNoSpan = null;
			$paging_allPagesNoSpan = null;
			
			
			var pagingDiv = $("<div>").addClass('qcdGrid_paging');
				$paging_prevButton =  $("<button>").html('prev');
				pagingDiv.append($paging_prevButton);
				
				$paging_recordsNoSelect = $("<select>");
					$paging_recordsNoSelect.append("<option value=10>10</option>");
					$paging_recordsNoSelect.append("<option value=20>20</option>");
					$paging_recordsNoSelect.append("<option value=50>50</option>");
					$paging_recordsNoSelect.append("<option value=100>100</option>");
				pagingDiv.append($paging_recordsNoSelect);
				
				var pageInfoSpan = $("<span>").addClass('qcdGrid_paging_pageInfo');
					pageInfoSpan.append('<span>page</span>');
					$paging_pageNoSpan = $("<span>");
					pageInfoSpan.append($paging_pageNoSpan);
					pageInfoSpan.append('<span>/</span>');
					$paging_allPagesNoSpan = $("<span>");
					pageInfoSpan.append($paging_allPagesNoSpan);
				pagingDiv.append(pageInfoSpan);
				
				$paging_nextButton =  $("<button>").html('next');
				pagingDiv.append($paging_nextButton);
			
				$paging_prevButton.click(function() {paging_prev();});
				$paging_recordsNoSelect.change(function() {paging_onRecordsNoSelectChange();});
				$paging_nextButton.click(function() {paging_next();});
				
			$this.after(pagingDiv);
			
				// create grid
			$this.jqGrid($this.options);
			
			refresh();
		});
		
			// define public methods
		this.deleteSelectedRecords = deleteSelectedRecords;
		this.refresh = refresh;
		this.setSortOptions = setSortOptions;
		
		return this;
	};
	
	$.fn.qcdGrid.defaults = {
		datatype: "local"
	}
	
	
		// PRIVATE
	var thisFirst = 0;
	var thisMax = 10;
	var totalNumberOfEntities;
	
	var sortColumn;
	var sortOrder;
	
	function setSortOptions(_sortColumn, _sortOrder) {
		sortColumn = _sortColumn;
		sortOrder = _sortOrder;
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
		thisMax = parseInt($paging_recordsNoSelect.val());
		refresh();
	}

	function refresh() {
		$this.jqGrid('clearGridData');
		blockList();
		var parameters = {'maxResults' : thisMax, 'firstResult': thisFirst};
		if (sortColumn && sortOrder) {
			parameters.sortColumn = sortColumn;
			parameters.sortOrder = sortOrder;
		}
		$.getJSON($this.options.dataSource, parameters, function(response) {
			totalNumberOfEntities = response.totalNumberOfEntities;
			for (var entityNo in response.entities) {
				var entity = response.entities[entityNo];
				$this.jqGrid('addRowData',entity.id,entity.fields);
			}	       
			unblockList();
		});
	}
	
	function deleteSelectedRecords() {
		if (window.confirm("delete?")) {
			debug("delete");
			blockList();
			var selectedRows = $this.getGridParam("selarrrow");
			
			var dataString = JSON.stringify(selectedRows);
			debug(dataString);
			$.ajax({
				url: $this.options.deleteUrl,
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
		$this.block({ message: $this.options.loadingText, showOverlay: false,  fadeOut: 0, fadeIn: 0,css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: '#000', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: .5, 
            color: '#fff' } });
		$paging_prevButton.attr("disabled", true);
		$paging_nextButton.attr("disabled", true);
		$paging_recordsNoSelect.attr("disabled", true);
	}

	function unblockList() {
		$this.unblock();
		paging_refreshBottomButtons();
	}

	function paging_refreshBottomButtons() {
		if (thisFirst > 0) {
			$paging_prevButton.attr("disabled", false);
		}
		if (thisFirst + thisMax < totalNumberOfEntities) {
			$paging_nextButton.attr("disabled", false);
		}
		$paging_recordsNoSelect.attr("disabled", false);
		var pagesNo = Math.ceil(totalNumberOfEntities / thisMax);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(thisFirst / thisMax) + 1;
		$paging_pageNoSpan.html(currPage);
		$paging_allPagesNoSpan.html(pagesNo);
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
	
})(jQuery);