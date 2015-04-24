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
	datatype: "json",
	height: 500,
	width: 1500,
	rowNum: 150,
	sortname: 'number',
    colNames: ["ID", "Numer", "Nazwa", "Kod EAN", "Aktywny"],
    colModel: [
    { name: 'id', key: true, width: 75 },
    {
        name: 'number',
        index: 'number',
        editable: true,
        sorttype:"text",
    }, {
        name: 'name',
        index: 'name',
        editable: true,
        sorttype:"text",
    }, {
        name: 'ean',
        index: 'ean',
        editable: true,
        sorttype:"text",
    }, {
             name: 'active',
             index: 'active',
             edittype: "checkbox",
             formatter: "checkbox",
                          editoptions: {
                                          value:"true:false"
                                          },
                                          sorttype:"boolean",

             editable: true,
         }],
    pager: "#jqGridPager",
     onSelectRow: function(id){
         jQuery('#grid').editRow(id,{
                                        keys : true,
                                        "url" : 'integration/rest/products/' + id + ".html",
                                         mtype: 'PUT',

                                    });
       },
       ajaxRowOptions: { contentType: "application/json" },
        serializeRowData: function(postdata){
           delete postdata.oper;
           return JSON.stringify(postdata);
        }
});



 $('#grid').jqGrid('filterToolbar');

    $('#grid').navGrid('#jqGridPager',
                // the buttons to appear on the toolbar of the grid
                { edit: true, add: true, del: true, search: false, refresh: false, view: false, position: "left", cloneToTop: false  },
                // options for the Edit Dialog
                {
                     ajaxEditOptions: { contentType: "application/json" },
                     mtype: 'PUT',
                     closeAfterEdit: true,
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
                    closeAfterEdit: true,
                    serializeEditData: function(data) {
                                           delete data.oper;
                                           delete data.id;
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