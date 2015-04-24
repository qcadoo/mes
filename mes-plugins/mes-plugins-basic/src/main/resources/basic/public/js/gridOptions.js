$(function() {

var template = "<div style='margin-left:15px;'>";
template += "<div> Numer: </div><div>{number} </div>";
template += "<div> Nazwa: </div><div>{name} </div>";
template += "<div> Kod EAN: </div><div>{ean} </div>";
template += "<div> Aktywny: </div><div>{active} </div>";
template += "<hr style='width:100%;'/>";
template += "<div> {sData} {cData}  </div></div>";
$("#grid").jqGrid({
	url:'integration/rest/products.html',
	editurl:'integration/rest/products.html',
	datatype: "json",
	height: 500,
	width: 1500,
    colNames: ["ID", "Numer", "Nazwa", "Kod EAN", "Aktywny"],
    colModel: [
    { name: 'id', key: true, width: 75 },
    {
        name: 'number',
        index: 'number',
        editable: true,
    }, {
        name: 'name',
        index: 'name',
        editable: true,
    }, {
        name: 'ean',
        index: 'ean',
        editable: true,
    }, {
             name: 'active',
             index: 'active',
             edittype: "checkbox",

             editable: true,
         }],
    pager: "#jqGridPager",
    onSelectRow: editRow
});
      var lastSelection;

            function editRow(id) {
                if (id && id !== lastSelection) {
                    var grid = $("#grid");
                    grid.jqGrid('restoreRow',lastSelection);
                    grid.jqGrid('editRow',id, {keys:true, focusField: 4});
                    lastSelection = id;
                }
            }

    $('#grid').navGrid('#jqGridPager',
                // the buttons to appear on the toolbar of the grid
                { edit: true, add: true, del: true, search: false, refresh: false, view: false, position: "left", cloneToTop: false },
                // options for the Edit Dialog
                {
                     ajaxEditOptions: { contentType: "application/json" },
                     mtype: 'PUT',
                     serializeEditData: function(data) {
                        delete data.oper;
                     	return JSON.stringify(data);
                     },
                      onclickSubmit: function (params, postdata) {
                            params.url = 'integration/rest/products/' + postdata.grid_id+".html";
                       },
                       errorTextFormat: function (data) {
                            return 'Error: ' + data.responseText
                       }
                },
                // options for the Add Dialog
                {
                                     ajaxEditOptions: { contentType: "application/json" },

                    mtype: "POST",
                    serializeEditData: function(data) {
                                           delete data.oper;
                                        	return JSON.stringify(data);
                                        },
                                         onclickSubmit: function (params, postdata) {
                                               params.url = 'integration/rest/products.html';
                                          },
                    errorTextFormat: function (data) {
                        return 'Error: ' + data.responseText
                    }
                },
                // options for the Delete Dailog
                {
                    mtype: "DELETE",
                      serializeDelData: function () {
                                return ""; // don't send and body for the HTTP DELETE
                            },
                            onclickSubmit: function (params, postdata) {
                                params.url = 'integration/rest/products/' + encodeURIComponent(postdata)+".html";
                            },
                    errorTextFormat: function (data) {
                        return 'Error: ' + data.responseText
                    }
                });
});