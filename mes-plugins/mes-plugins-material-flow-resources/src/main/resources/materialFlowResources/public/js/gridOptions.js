$(function() {


	var _this = this;
	var lookupWindow;
	var productIdElement;

function validatePositive(value, column) {
    if ( isNaN(value) && value < 0)
        return [false, "Please enter a positive value"];
    else
        return [true, ""];
}

	this.onGridLinkClicked = function(entityId) {
		var grid = lookupWindow.mainController.getComponent("window.mainTab.grid");
//		var lookupData = grid.getLookupData(entityId);

        productIdElement.val(entityId);

		mainController.closeThisModalWindow();
	}

function onModalClose() {
    lookupWindow = null;
}

function onModalRender(modalWindow) {
    modalWindow.getComponent("window.mainTab.grid").setLinkListener(_this);
}

function editProductId_openLookup(){
    lookupWindow = mainController.openModal('body', 'materialFlowResources/productsLookup.html', null, onModalClose, onModalRender, {width : 1000, height : 560 })
}

function editProductId_createElement (value, options) {
  productIdElement = $('<input type="text" readonly="true" />');
  productIdElement.val(value);

  var button = productIdElement.after('<button>Produkt</button>');

  button.bind( 'click', function() {
    editProductId_openLookup();
  });

  return productIdElement;
}

function editProductId_value(elem, operation, value) {
    if(operation === 'get') {
       return $(elem).val();

    } else if(operation === 'set') {
       $('input',elem).val(value);
    }
}

var template = "<div style='margin-left:15px;'>";
template += "<div> Nazwa: </div><div>{name} </div>";

$("#grid").jqGrid({
	url:'../../integration/rest/documentPositions.html',
	datatype: "json",
	height: 500,
	width: 1500,
	rowNum: 150,
	sortname: 'id',
    colNames: ['ID', 'quantity', 'givenquantity', 'expirationdate', 'type', 'product_id'],
    colModel: [
        { 
            name: 'id', 
            key: true, 
            width: 75 
        },
        {
            name: 'quantity',
            index: 'quantity',
            editable: true,
            editrules:{
                custom_func: validatePositive,
                custom: true,
                required: false
            },
        },
         {
             name: 'givenquantity',
             index: 'givenquantity',
             editable: true,
             editrules:{
                 custom_func: validatePositive,
                 custom: true,
                 required: false
             },
         },
        {
            name: 'expirationdate',
            index: 'expirationdate',
            width: 150,
            editable: true,
            edittype:"text",
            editoptions: {
                dataInit: function (element) {
                    var options = $.datepicker.regional[window.locale];
                    options.showOn = 'focus';

                    $(element).datepicker(options);
                }
            }
        },
        {
              name: 'type',
              index: 'type',
              editable: true,
              edittype: 'select',
            editoptions: {aysnc:false, dataUrl: '../../integration/rest/documentPositions/types.html',
                buildSelect: function (response) {
                    var data = $.parseJSON(response);
                    var s = "<select>";

                    s += '<option value="0">--</option>';
                    $.each(data, function () {
                        s += '<option value="' + this.key + '">' + this.value +'</option>';
                    });

                    return s + "</select>";
                }
            },
          },
         {
             name: 'product_id',
             index: 'product_id',
             editable: true,
             edittype: 'custom',
             editoptions: {
                custom_element: editProductId_createElement,
                custom_value: editProductId_value
             }
         }

    /*,
    {
        name: 'number',
        index: 'number',
        editable: true,
        sorttype:"text",
    },{
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
         }*/
         ],
    pager: "#jqGridPager",
     onSelectRow: function(id){
         jQuery('#grid').editRow(id,{
                                        keys : true,
                                        "url" : '../../integration/rest/documentPositions/' + id + ".html",
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
                            params.url = '../../integration/rest/documentPositions/' + postdata.grid_id+".html";
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
                                               params.url = '../../integration/rest/documentPositions.html';
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
                                params.url = '../../integration/rest/documentPositions/' + encodeURIComponent(postdata)+".html";
                            },
                    errorTextFormat: function (data) {
                        return 'Error: ' + data.responseText
                    }
                });
});