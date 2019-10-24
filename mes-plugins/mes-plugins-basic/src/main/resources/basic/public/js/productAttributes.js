let grid;
let options = {
    enableCellNavigation: true,
    showHeaderRow: true,
    headerRowHeight: 30,
    explicitInitialization: true,
    autosizeColsMode: Slick.GridAutosizeColsMode.FitColsToViewport
};
let pagerOptions = {
    showAllText: QCD.translate('qcadooView.slickGrid.pager.showAllText'),
    showPageText: QCD.translate('qcadooView.slickGrid.pager.showPageText'),
    show: QCD.translate('qcadooView.slickGrid.pager.show'),
    all: QCD.translate('qcadooView.slickGrid.pager.all')
};
let columnFilters = {};

function filter(item) {
    for (let columnId in columnFilters) {
        if (columnId !== undefined && columnFilters[columnId] !== "") {
            let c = grid.getColumns()[grid.getColumnIndex(columnId)];
            if (item[c.field] === undefined || item[c.field].indexOf(columnFilters[columnId]) < 0) {
                return false;
            }
        }
    }
    return true;
}

$.get("/rest/prodAttributes/columns", function (columns) {
    $('#productAttributesGrid').height($('#window_windowContent').height() - 45);
    $('#productAttributesGrid').width($('#window_windowContent').width() - 20);
    let dataView = new Slick.Data.DataView();
    grid = new Slick.Grid("#productAttributesGrid", dataView, columns, options);

    grid.setSortColumn("productNumber", true);

    let pager = new Slick.Controls.Pager(dataView, grid, $("#pager"), pagerOptions);

    dataView.onRowCountChanged.subscribe(function (e, args) {
        grid.updateRowCount();
        grid.render();
    });

    dataView.onRowsChanged.subscribe(function (e, args) {
        grid.invalidateRows(args.rows);
        grid.render();
    });

    dataView.onPagingInfoChanged.subscribe(function (e, pagingInfo) {
        grid.updatePagingStatusFromView( pagingInfo );
    });

    $(grid.getHeaderRow()).on("change keyup", ":input", function (e) {
        let columnId = $(this).data("columnId");
        if (columnId != null) {
            columnFilters[columnId] = $.trim($(this).val());
            dataView.refresh();
        }
    });

    grid.onHeaderRowCellRendered.subscribe(function(e, args) {
        $(args.node).empty();
        $("<input type='text'>")
            .data("columnId", args.column.id)
            .val(columnFilters[args.column.id])
            .appendTo(args.node);
    });

    grid.onSort.subscribe(function (e, args) {
        let comparer = function(a, b) {
            return (a[args.sortCol.field] > b[args.sortCol.field]) ? 1 : -1;
        };

        dataView.sort(comparer, args.sortAsc);
    });

    $.get("/rest/prodAttributes/records", function (records) {
        grid.init();
        grid.autosizeColumns();
        dataView.beginUpdate();
        dataView.setItems(records);
        dataView.setFilter(filter);
        dataView.endUpdate();
    }, 'json');
}, 'json');
