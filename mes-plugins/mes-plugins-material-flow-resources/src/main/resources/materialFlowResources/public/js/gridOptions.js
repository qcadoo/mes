/*
 * jQuery resize event - v1.1 - 3/14/2010
 * http://benalman.com/projects/jquery-resize-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */
(function ($, h, c) {
    var a = $([]), e = $.resize = $.extend($.resize, {}), i, k = "setTimeout", j = "resize", d = j + "-special-event", b = "delay", f = "throttleWindow";
    e[b] = 250;
    e[f] = true;
    $.event.special[j] = {setup: function () {
            if (!e[f] && this[k]) {
                return false
            }
            var l = $(this);
            a = a.add(l);
            $.data(this, d, {w: l.width(), h: l.height()});
            if (a.length === 1) {
                g()
            }
        }, teardown: function () {
            if (!e[f] && this[k]) {
                return false
            }
            var l = $(this);
            a = a.not(l);
            l.removeData(d);
            if (!a.length) {
                clearTimeout(i)
            }
        }, add: function (l) {
            if (!e[f] && this[k]) {
                return false
            }
            var n;
            function m(s, o, p) {
                var q = $(this), r = $.data(this, d);
                r.w = o !== c ? o : q.width();
                r.h = p !== c ? p : q.height();
                n.apply(this, arguments)
            }
            if ($.isFunction(l)) {
                n = l;
                return m
            } else {
                n = l.handler;
                l.handler = m
            }
        }};
    function g() {
        i = h[k](function () {
            a.each(function () {
                var n = $(this), m = n.width(), l = n.height(), o = $.data(this, d);
                if (m !== o.w || l !== o.h) {
                    n.trigger(j, [o.w = m, o.h = l])
                }
            });
            g()
        }, e[b])
    }}
)(jQuery, this);

var myApp = angular.module('gridApp', []);

myApp.directive('ngJqGrid', function ($window) {
    return {
        restrict: 'E',
        scope: {
            config: '=',
            data: '=',
        },
        link: function (scope, element, attrs) {
            var table;

            scope.$watch('config', function (newValue) {
                if (newValue) {
                    element.children().empty();
                    table = angular.element('<table id="grid"></table>');
                    element.append(table);
                    element.append(angular.element('<div id="jqGridPager"></div>'));
                    $(table).jqGrid(newValue);

                    var positionsHeader = QCD.translate('qcadooView.gridHeader.positions');
                    var newHeader = QCD.translate('qcadooView.gridHeader.new');
                    var addNewRowButton = '<div id="add_new_row" class="headerActionButton headerButtonEnabled ' + (newValue.readOnly ? 'disabled-button"' : '" onclick="return addNewRow();"') + '> <a href="#"><span>' +
                            '<div class="icon" id="add_new_icon""></div>' +
                            '<div class="hasIcon">' + newHeader + '</div></div>';

                    var gridTitle = '<div class="gridTitle">' + positionsHeader + '</div>';

                    $('#t_grid').append(gridTitle);
                    $('#t_grid').append(addNewRowButton);

                    $(table).jqGrid('filterToolbar');

                    function translateAndShowMessages(response) {
                        var messages = translateMessages(JSON.parse(response.responseText).message);

                        angular.forEach(messages.split('\n'), function (el, key) {
                            messagesController.addMessage({
                                type: "failure",
                                content: el
                            });
                        });

                        return '';
                    }

                    if (!newValue.readOnly) {
                        $(table).navGrid('#jqGridPager',
                                // the buttons to appear on the toolbar of the grid
                                        {edit: true, add: true, del: true, search: false, refresh: false, view: false, position: "left", cloneToTop: false},
                                // options for the Edit Dialog
                                {
                                    ajaxEditOptions: {contentType: "application/json"},
                                    mtype: 'PUT',
                                    closeAfterEdit: true,
                                    resize: false,
                                    viewPagerButtons: false,
                                    serializeEditData: function (data) {
                                        delete data.oper;

                                        return validateSerializeData(data);
                                    },
                                    onclickSubmit: function (params, postdata) {
                                        params.url = '../../integration/rest/documentPositions/' + postdata.grid_id + ".html";
                                    },
                                    errorTextFormat: function (response) {
                                        return translateAndShowMessages(response);
                                    },
                                    beforeShowForm: function (form) {
                                        var dlgDiv = $("#editmodgrid");
                                        var dlgWidth = 586;
                                        var dlgHeight = dlgDiv.height();
                                        var parentWidth = $(window).width();
                                        var parentHeight = $(window).height();
                                        dlgDiv[0].style.left = Math.round((parentWidth - dlgWidth) / 2) + "px";
                                        dlgDiv[0].style.top = Math.round((parentHeight - dlgHeight) / 2) + "px";
                                        dlgDiv[0].style.width = dlgWidth + "px";
                                    },
                                },
                                        // options for the Add Dialog
                                                {
                                                    ajaxEditOptions: {
                                                        contentType: "application/json"
                                                    },
                                                    mtype: "PUT",
                                                    closeAfterEdit: true,
                                                    resize: false,
                                                    reloadAfterSubmit: true,
                                                    viewPagerButtons: false,
                                                    serializeEditData: function (data) {
                                                        delete data.oper;
                                                        delete data.id;

                                                        return validateSerializeData(data);
                                                    },
                                                    onclickSubmit: function (params, postdata) {
                                                        params.url = '../../integration/rest/documentPositions.html';
                                                    },
                                                    errorTextFormat: function (response) {
                                                        return translateAndShowMessages(response);
                                                    },
                                                    beforeShowForm: function (form) {
                                                        var dlgDiv = $("#editmodgrid");
                                                        var dlgWidth = 586;
                                                        var dlgHeight = dlgDiv.height();
                                                        var parentWidth = $(window).width();
                                                        var parentHeight = $(window).height();
                                                        dlgDiv[0].style.left = Math.round((parentWidth - dlgWidth) / 2) + "px";
                                                        dlgDiv[0].style.top = Math.round((parentHeight - dlgHeight) / 2) + "px";
                                                        dlgDiv[0].style.width = dlgWidth + "px";
                                                    },
                                                },
                                                // options for the Delete Dailog
                                                        {
                                                            mtype: "DELETE",
                                                            serializeDelData: function () {
                                                                return ""; // don't send and body for the HTTP DELETE
                                                            },
                                                            onclickSubmit: function (params, postdata) {
                                                                params.url = '../../integration/rest/documentPositions/' + encodeURIComponent(postdata) + ".html";
                                                            },
                                                            errorTextFormat: function (response) {
                                                                return translateMessages(JSON.parse(response.responseText).message);
                                                            }
                                                        });
                                            }
                                }
                    });


            scope.$watch('data', function (newValue, oldValue) {
                var i;
                for (i = oldValue.length - 1; i >= 0; i--) {
                    $(table).jqGrid('delRowData', i);
                }
                for (i = 0; i < newValue.length; i++) {
                    $(table).jqGrid('addRowData', i, newValue[i]);
                }
            });
        }
    };
});

function validateSerializeData(data) {
    var elements = null;
    if ($('#FrmGrid_grid').length) {
        elements = $('.error-grid', '#FrmGrid_grid');

    } else {
        elements = $('.error-grid', '#gridContainer');
    }
    /*
     angular.forEach(elements, function (el, key) {
     var name = $(el).attr('name');
     delete data[name];
     });
     */
    angular.forEach(data, function (el, key) {
        if ('typeOfPallet' === key) {
            /*var emptyItem = translateMessages('qcadooView.typeOfPallet.emptyItem');
             if(emptyItem === data[key]){
             delete data[key];
             }*/
        }
    });



    return JSON.stringify(data);
}

function roundTo(n) {
    var places = 5;
    return +(Math.round(parseFloat(n) + "e+" + places) + "e-" + places);
}

function validatorNumber(val) {
    if (val === '') {
        return true;
    }

    return parseFloat(val) === roundTo(val);
}

function validateElement(el, validator) {
    el = $(el);
    if (validator(el.val())) {
        el.removeClass('error-grid');

    } else {
        el.addClass('error-grid');
    }
}

function translateMessages(messages) {
    var message = [];
    if (messages) {
        var messageArray = messages.split('\n');
        for (var i in messageArray) {
            message.push(QCD.translate(messageArray[i]));
        }
    }
    message = message.join('\n');

    return message;
}

function documentIdChanged(id) {
    angular.element($("#GridController")).scope().documentIdChanged(id);
}

function addNewRow() {
    angular.element($("#GridController")).scope().addNewRow();
}

function openLookup(name) {
    mainController.openModal('body', '../' + name + '/lookup.html', null, function onModalClose() {
    }, function onModalRender(modalWindow) {
    }, {width: 1000, height: 560});
}

function updateFieldValue(field, value, rowId) {
    var productInput = $('#product');
    var selector = null;

    if (productInput.length) {
        // edit form
        selector = $('#' + field);

    } else {
        // edit inline
        selector = $('#' + rowId + '_' + field);
    }

    var element = $(selector);
    if (element.length && element[0].tagName.toLowerCase() === 'span') {
        element = $('input', element);
    }

    return element.val(value);
}

function onSelectLookupRow(row, recordName) {
    if (row) {
        var code = row.code || row.number;

        var rowId = $('#product').length ? null : jQuery('#grid').jqGrid('getGridParam', 'selrow');
        var field = updateFieldValue(recordName, code, rowId);
        field.trigger('change');
    }

    mainController.closeThisModalWindow();
}

var messagesController = new QCD.MessagesController();

myApp.controller('GridController', ['$scope', '$window', '$http', function ($scope, $window, $http) {
        var _this = this;
        var quantities = {};
        var conversionModified = true;
        var lastSel;
        var firstLoad = true;

        function getRowIdFromElement(el) {
            var rowId = el.attr('rowId');
            if ('_empty' === rowId) {
                rowId = 0;
            }

            return rowId;
        }

        function showMessage(message) {
            messagesController.addMessage(message);
        }

        function getDocumentId() {
            if (context) {
                var contextObject = JSON.parse(context);
                if (contextObject && contextObject['window.generalTab.form.id']) {
                    return contextObject['window.generalTab.form.id'];
                }
            }

            return 0;
        }

        function createLookupElement(name, value, url, options) {
            var $ac = $('<input class="eac-square" rowId="' + options.rowId + '" />');
            $ac.val(value);
            $ac.autoComplete({
                source: function (query, response) {
                    try {
                        xhr.abort();
                    } catch (e) {
                    }
                    xhr = $.getJSON(url, {query: query}, function (data) {
                        response(data);
                    });
                },
                renderItem: function (item, search) {
                    var code = item.code || item.number;
                    var id = item.id;
                    // escape special characters
                    search = search.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
                    var re = new RegExp("(" + search.split(' ').join('|') + ")", "gi");
                    return '<div class="autocomplete-suggestion" data-id="' + id + '" data-val="' + code + '">' + code.replace(re, "<b>$1</b>") + '</div>';
                },
                onSelect: function (e, term, item, that) {
                    $(that).trigger('change');
                }
            });

            var button = $('<button value="xxx">Szukaj</button>');
            button.bind('click', function () {
                openLookup(name);
            });

            var wrapper = $('<span></span>');
            wrapper.append($ac);
            wrapper.append(button);

            return wrapper;
        }

        function storageLocationLookup_createElement(value, options) {
            return createLookupElement('storageLocation', value, '/integration/rest/documentPositions/storagelocations.html', options);
        }

        function palletNumbersLookup_createElement(value, options) {
            return createLookupElement('palletNumber', value, '/rest/palletnumbers', options);
        }

        function getFieldValue(field, rowId) {
            var productInput = $('#product');
            var selector = null;

            if (productInput.length) {
                // edit form
                selector = $('#' + field);

            } else {
                // edit inline
                selector = $('#' + rowId + '_' + field);
            }

            var element = $(selector);
            if (element.length && element[0].tagName.toLowerCase() === 'span') {
                element = $('input', element);
            }

            return element.val();
        }

        var available_additionalunits = null;
        function updateUnitsInGridByProduct(productNumber, additionalUnitValue) {
            $.get('/integration/rest/documentPositions/units/' + productNumber + ".html", function (units) {
                available_additionalunits = units['available_additionalunits'];
                var gridData = $('#grid').jqGrid('getRowData');

                var productInput = $('#product');

                if (productInput.length) {
                    // edit form
                    var unitInput = $('#unit').val(units['unit']);
                    var rowId = getRowIdFromElement(unitInput);
                    var additionalUnitInput = $('#givenunit');
                    additionalUnitInput[0].options.length = 0;
                    angular.forEach(units['available_additionalunits'], function (value, key) {
                        additionalUnitInput.append('<option value="' + value.key + '">' + value.value + '</option>');
                    });
                    if (!additionalUnitValue) {
                        additionalUnitValue = units['additionalunit'];
                    }
                    additionalUnitInput.val(additionalUnitValue);
                    updateConversionByGivenUnitValue(additionalUnitValue, rowId);

                } else {
                    // edit inline
                    var patternProduct = /(id=\".+_product\")/ig;
                    for (var i = 0; i < gridData.length; i++) {
                        var product = gridData[i]['product'];
                        if (product.toLowerCase().indexOf('<input') >= 0) {
                            var matched = product.match(patternProduct)[0];
                            var numberOfInput = matched.toUpperCase().replace("ID=\"", "").replace("_PRODUCT\"", "");
                            var productValue = getFieldValue('product', numberOfInput);

                            if (productValue === productNumber) {
                                // update input
                                $('#' + numberOfInput + '_unit').val(units['unit']);

                                // set additionalunit available options                        
                                var additionalUnitInput = $('#' + numberOfInput + '_givenunit');

                                additionalUnitInput[0].options.length = 0;
                                angular.forEach(units['available_additionalunits'], function (value, key) {
                                    additionalUnitInput.append('<option value="' + value.key + '">' + value.value + '</option>');
                                });

                                // update additionalunit
                                if (!additionalUnitValue) {
                                    additionalUnitValue = units['additionalunit'];
                                }
                                additionalUnitInput.val(additionalUnitValue);

                                // update conversion           
                                updateConversionByGivenUnitValue(additionalUnitValue, numberOfInput);
                            }
                        }
                    }
                }
            });
        }

        function productsLookup_createElement(value, options) {
            var lookup = createLookupElement('product', value, '/rest/products', options);

            $('input', lookup).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    conversionModified = false;
                    updateUnitsInGridByProduct(t.val());
                }, 500));
            });

            return lookup;
        }

        function additionalCodeLookup_createElement(value, options) {
            var url = '/rest/additionalcodes';
            var lookup = createLookupElement('additionalCode', value, url, options);

            $('input', lookup).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    updateProductByAdditionalCode(t.val(), getRowIdFromElement(t), url);
                }, 500));
            });

            return lookup;
        }

        function updateProductByAdditionalCode(additionalCode, rowId, url) {
            $.getJSON(url, {query: additionalCode}, function (data) {
                var product = '';

                product = data.filter(function (element, index) {
                    return element.code === additionalCode;
                })[0];

                if (product) {
                    product = product.productnumber;
                }
                if (product) {
                    var productField = updateFieldValue('product', product, rowId);
                    productField.trigger('change');
                }

            });
        }

        function lookup_value(elem, operation, value) {
            if (operation === 'get') {
                return $('input', elem).val();

            } else if (operation === 'set') {
                return $('input', elem).val(value);
            }
        }

        function input_value(elem, operation, value) {
            if (operation === 'get') {
                return $(elem).val();

            } else if (operation === 'set') {
                return $('input', elem).val(value);
            }
        }

        function touchManuallyQuantityField(rowId) {
            var productInput = $('#product');

            if (productInput.length) {
                // edit form
                $('#quantity').trigger('change');

            } else {
                // edit inline
                $('#' + rowId + '_quantity').trigger('change');
            }
        }

        function updateConversionByGivenUnitValue(givenUnitValue, rowId) {
            var conversion = '';

                if (available_additionalunits) {
                    var entry = available_additionalunits.filter(function (element, index) {
                        return element.key === givenUnitValue;
                    })[0];
                    if (entry) {
                        quantities[rowId || 0] = {from: entry.quantityfrom, to: entry.quantityto};
                        if (!firstLoad && !conversionModified) {
                            conversion = roundTo(parseFloat(entry.quantityto) / parseFloat(entry.quantityfrom));
                        }
                    }
                if (!firstLoad && !conversionModified) {
                    updateFieldValue('conversion', conversion, rowId);
                }
                touchManuallyQuantityField(rowId);
                firstLoad = false;
            }
        }

        function quantity_createElement(value, options) {
            var $input = $('<input type="number" min="0" step="0.00001" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            $($input).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));

                validateElement(t, validatorNumber);

                $(this).data("timeout", setTimeout(function () {
                    var rowId = getRowIdFromElement(t);

                    var conversion = getFieldValue('conversion', rowId);
                    var newGivenQuantity = null;
                    var quantity = t.val();

                    if (quantities[rowId]) {
                        newGivenQuantity = roundTo(quantity * conversion);
                    }
                    newGivenQuantity = roundTo(newGivenQuantity);
                    if (!newGivenQuantity || t.hasClass('error-grid')) {
                        newGivenQuantity = '';
                    }

                    updateFieldValue('givenquantity', newGivenQuantity, rowId);
                }, 500));
            });

            return $input;
        }

        function price_createElement(value, options) {
            var $input = $('<input type="number" min="0" step="0.00001" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                validateElement(t, validatorNumber);
            });

            return $input;
        }

        function givenquantity_createElement(value, options) {
            var $input = $('<input type="number" min="0" step="0.00001" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                validateElement(t, validatorNumber);

                window.clearTimeout(t.data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    var rowId = getRowIdFromElement(t);

                    var conversion = getFieldValue('conversion', rowId);
                    var newQuantity = null;
                    if (quantities[rowId]) {
                        newQuantity = roundTo(t.val() * (1 / conversion));
                    }
                    newQuantity = roundTo(newQuantity);
                    if (!newQuantity || t.hasClass('error-grid')) {
                        newQuantity = '';
                    }

                    updateFieldValue('quantity', newQuantity, rowId);
                }, 500));
            });

            return $input;
        }

        function conversion_createElement(value, options) {
            var $input = $('<input type="number" min="0" step="0.00001" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            $($input).bind('change keydown paste input', function () {
                var t = $(this);
                conversionModified = true;
                window.clearTimeout(t.data("timeout"));

                validateElement(t, validatorNumber);

                console.log(t.val());
                $(this).data("timeout", setTimeout(function () {
                    var rowId = getRowIdFromElement(t);

                    var quantity = getFieldValue('quantity', rowId);
                    var newGivenQuantity = null;
                    if (quantities[rowId]) {
                        newGivenQuantity = roundTo(t.val() * quantity);
                    }
                    newGivenQuantity = roundTo(newGivenQuantity);
                    if (!newGivenQuantity || t.hasClass('error-grid')) {
                        newGivenQuantity = '';
                    }

                    updateFieldValue('givenquantity', newGivenQuantity, rowId);
                }, 500));

            });

            return $input;
        }

        function givenunit_createElement(value, options) {
            var $select = $('<select id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '">');

            var rowId = options.rowId;
            var gridData = $('#grid').jqGrid('getRowData');

            var productNumber = '';
            var currentElement = gridData.filter(function (element, index) {
                return element.id === rowId;
            })[0];
            if (currentElement) {
                productNumber = currentElement.product;
            }

            if (productNumber.toLowerCase().indexOf('<input') >= 0) {
                productNumber = getFieldValue('product', rowId);
            }

            updateUnitsInGridByProduct(productNumber, value);

            $select.bind('change', function () {
                var newValue = $(this).val();
                conversionModified = false;
                updateConversionByGivenUnitValue(newValue, getRowIdFromElement($(this)));
            });

            return $select;
        }

        function givenunit_value(elem, operation, value) {
            if (operation === 'get') {
                return $(elem).val();

            } else if (operation === 'set') {
                return $('select', elem).val(value);
            }
        }

        function errorfunc(rowID, response) {
            var message = JSON.parse(response.responseText).message;
            message = translateMessages(message);

            showMessage({
                type: "failure",
                content: message
            });

            return true;
        }

        function successfunc(rowID, response) {
            $("#add_new_row").removeClass("disableButton");
            $("#add_grid").show();
            $("#edit_grid").show();
            $("#del_grid").show();
            showMessage({
                type: 'success',
                content: QCD.translate('qcadooView.message.saveMessage')
            });

            return true;
        }

        function errorCallback(response) {
            showMessage({
                type: "failure",
                content: response.data.message
            });
        }

        function aftersavefunc() {
            $("#grid").trigger("reloadGrid");
        }

         function cancelEditing(myGrid) {
            var lrid;
            if (typeof lastSel !== "undefined") {
                // cancel editing of the previous selected row if it was in editing state.
                 // jqGrid hold intern savedRow array inside of jqGrid object,
                // so it is safe to call restoreRow method with any id parameter
                // if jqGrid not in editing state
                $('#grid').jqGrid('restoreRow',lastSel);

                 // now we need to restore the icons in the formatter:"actions"
                lrid = $.jgrid.jqID(lastSel);
                $("tr#" + lrid + " div.ui-inline-edit, " + "tr#" + lrid + " div.ui-inline-del").show();
                $("tr#" + lrid + " div.ui-inline-save, " + "tr#" + lrid + " div.ui-inline-cancel").hide();
             }
         }

        $scope.resize = function () {
            console.log('resize');
            jQuery('#grid').setGridWidth($("#window\\.positionsGridTab").width() - 25, true);
            jQuery('#grid').setGridHeight($("#window\\.positionsGridTab").height() - 150);
        }
        $("#window\\.positionsGridTab").resize($scope.resize);

        var gridEditOptions = {
            keys: true,
            url: '../../integration/rest/documentPositions.html',
            mtype: 'PUT',
            errorfunc: errorfunc,
            successfunc: successfunc,
            aftersavefunc: aftersavefunc
        };

        var gridAddOptions = {
            rowID: "0",
            url: '../../integration/rest/documentPositions.html',
            initdata: {
            },
            position: "first",
            useDefValues: true,
            useFormatter: true,
            addRowParams: angular.extend({
                extraparam: {}
            }, gridEditOptions)
        };

        var config = {
            url: '../../integration/rest/documentPositions/' + getDocumentId() + '.html',
            datatype: "json",
            height: '100%',
            autowidth: true,
            rowNum: 20,
            rowList: [20, 30, 50, 100, 200],
            sortname: 'id',
            toolbar: [true, "top"],
            rownumbers: false,
            altRows: true,
            altclass: 'qcadooRowClass',
            errorTextFormat: function (response) {
                return translateMessages(JSON.parse(response.responseText).message);
            },
            colNames: ['ID', QCD.translate('qcadooView.gridColumn.document'), QCD.translate('qcadooView.gridColumn.number'), QCD.translate('qcadooView.gridColumn.actions'), QCD.translate('qcadooView.gridColumn.product'), QCD.translate('qcadooView.gridColumn.additionalCode'), 
                QCD.translate('qcadooView.gridColumn.quantity'), QCD.translate('qcadooView.gridColumn.unit'), QCD.translate('qcadooView.gridColumn.givenquantity'),
                QCD.translate('qcadooView.gridColumn.givenunit'), QCD.translate('qcadooView.gridColumn.conversion'), QCD.translate('qcadooView.gridColumn.price'),
                QCD.translate('qcadooView.gridColumn.expirationdate'), QCD.translate('qcadooView.gridColumn.productiondate'), QCD.translate('qcadooView.gridColumn.batch'),
                QCD.translate('qcadooView.gridColumn.palletNumber'), QCD.translate('qcadooView.gridColumn.typeOfPallet'),
                QCD.translate('qcadooView.gridColumn.storageLocation')/*, 'resource_id'*/],
            colModel: [
                {
                    name: 'id',
                    index: 'id',
                    key: true,
                    hidden: true
                },
                {
                    name: 'document',
                    index: 'document',
                    hidden: true,
                    editable: true,
                    editoptions: {
                        defaultValue: getDocumentId()
                    }

                },
                {
                    name: 'number',
                    index: 'number',
                    search: false,
                    width: 50,
                    hidden: false,
                    editable: false
                },
                 {
                    name:'act',
                    index:'act',
                    width:55,
                    align:'center',
                    sortable:false,
                    search:false,
                    formatter:'actions',
                    formatoptions:{
                        keys: true, // we want use [Enter] key to save the row and [Esc] to cancel editing.
                        editOptions: gridEditOptions,
                        url : '../../integration/rest/documentPositions/' + 1 + '.html',
                        delbutton: false,
                        onEdit: function (id) {

                            if (typeof (lastSel) !== "undefined" && id !== lastSel) {
                                cancelEditing(id);
                            }

                             $("#add_new_row").addClass("disableButton");
                                                        $("#add_grid").hide();
                                                        $("#edit_grid").hide();
                                                        $("#del_grid").hide();
                            gridEditOptions.url = '../../integration/rest/documentPositions/' + id + '.html';
                            lastSel = id;
                        },
                        afterRestore :  function () {
                            $("#add_new_row").removeClass("disableButton");
                            $("#add_grid").show();
                            $("#edit_grid").show();
                            $("#del_grid").show();
                        }
                    }
                 },
                 {
                    name: 'product',
                    index: 'product',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: productsLookup_createElement,
                        custom_value: lookup_value,
                    },
                    formoptions: {
                        rowpos: 2,
                        colpos: 1
                    }
                },
                {
                    name: 'additionalCode',
                    index: 'additionalCode',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: additionalCodeLookup_createElement,
                        custom_value: lookup_value
                    },
                    formoptions: {
                        rowpos: 3,
                        colpos: 1
                    },
                },
                {
                    name: 'quantity',
                    index: 'quantity',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: quantity_createElement,
                        custom_value: input_value
                    },
                    formoptions: {
                        rowpos: 4,
                        colpos: 1
                    },
                },
                {
                    name: 'unit',
                    index: 'unit',
                    editable: true,
                    editoptions: {readonly: 'readonly'},
                    width: 60,
                    formoptions: {
                        rowpos: 5,
                        colpos: 1
                    },
                },
                {
                    name: 'givenquantity',
                    index: 'givenquantity',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: givenquantity_createElement,
                        custom_value: input_value
                    },
                    formoptions: {
                        rowpos: 6,
                        colpos: 1
                    },
                },
                {
                    name: 'givenunit',
                    index: 'givenunit',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: givenunit_createElement,
                        custom_value: givenunit_value
                    },
                    formoptions: {
                        rowpos: 7,
                        colpos: 1
                    },
                },
                {
                    name: 'conversion',
                    index: 'conversion',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: conversion_createElement,
                        custom_value: input_value
                    },
                    formoptions: {
                        rowpos: 8,
                        colpos: 1
                    },
                },
                {
                    name: 'price',
                    index: 'price',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: price_createElement,
                        custom_value: input_value
                    },
                    formoptions: {
                        rowpos: 2,
                        colpos: 2
                    },
                },
                {
                    name: 'expirationdate',
                    index: 'expirationdate',
                    width: 150,
                    editable: true,
                    required: true,
                    edittype: "text",
                    editoptions: {
                        dataInit: function (element) {
                            var options = $.datepicker.regional[window.locale];
                            options.showOn = 'button';
                            options.buttonImage = '/qcadooView/public/css/crud/images/form/f_calendar.png';
                            options.buttonImageOnly = true;
                            options.buttonText = 'Wybierz';
                            $(element).datepicker(options);
                        }
                    },
                    formoptions: {
                        rowpos: 3,
                        colpos: 2
                    },
                },
                {
                    name: 'productiondate',
                    index: 'productiondate',
                    width: 150,
                    editable: true,
                    required: true,
                    edittype: "text",
                    editoptions: {
                        dataInit: function (element) {
                            var options = $.datepicker.regional[window.locale];
                            options.showOn = 'button';
                            options.buttonImage = '/qcadooView/public/css/crud/images/form/f_calendar.png';
                            options.buttonImageOnly = true;
                            options.buttonText = 'Wybierz';
                            $(element).datepicker(options);
                        }
                    },
                    formoptions: {
                        rowpos: 4,
                        colpos: 2
                    },
                },
                {
                    name: 'batch',
                    index: 'batch',
                    editable: true,
                    required: true,
                    formoptions: {
                        rowpos: 5,
                        colpos: 2
                    },
                },
                {
                    name: 'palletNumber',
                    index: 'palletNumber',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: palletNumbersLookup_createElement,
                        custom_value: lookup_value
                    },
                    formoptions: {
                        rowpos: 6,
                        colpos: 2
                    },
                },
                {
                    name: 'typeOfPallet',
                    index: 'typeOfPallet',
                    editable: true,
                    required: true,
                    edittype: 'select',
                    editoptions: {
                    },
                    formoptions: {
                        rowpos: 7,
                        colpos: 2
                    },
                },
                {
                    name: 'storageLocation',
                    index: 'storageLocation',
                    editable: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: storageLocationLookup_createElement,
                        custom_value: lookup_value
                    },
                    formoptions: {
                        rowpos: 8,
                        colpos: 2
                    },
                }/*,
                 {
                 name: 'resource_id',
                 index: 'resource_id',
                 editable: true,
                 edittype: 'custom',
                 editoptions: {
                 custom_element: editProductId_createElement,
                 custom_value: editProductId_value
                 }
                 }*/
            ],
            pager: "#jqGridPager",
            gridComplete: function () {
                var grid = $('#grid');
                var rows = grid.jqGrid('getDataIDs');
                if($scope.config.readOnly){
                for (i = 0; i < rows.length; i++)
                {
                     $("tr#" + rows[i] + " div.ui-inline-edit").hide();

                }
                }
                //setTimeout(function() { $scope.resize(); }, 1000);

            },
            onSelectRow: function (id) {
               // gridEditOptions.url = '../../integration/rest/documentPositions/' + id + '.html';

//                if ($scope.editedRow) {
//
//                } else {
//                    $scope.editedRow = id;
//                    jQuery('#grid').editRow(id, gridEditOptions);
//                }
              //  jQuery('#grid').editRow(id, gridEditOptions);
                }
            },
            ajaxRowOptions: {
                contentType: "application/json"
            },
            serializeRowData: function (postdata) {
                delete postdata.oper;

                return validateSerializeData(postdata);
            },
            beforeSubmit: function (postdata, formid) {
                return [false, 'ble'];
            },
//            serializeGridData: function (postData) {
//                var queryData = {};
//                angular.forEach(config.colModel, function (value, key) {
//                    queryData[value.index] = postData[value.index];
//                    delete postData[value.index];
//                });
//                postData.queryData = queryData;
//                
//                return postData;
//            },
        };

        function prepareGridConfig(config) {
            var hideColumnInGrid = function (columnIndex, responseDate) {
                if (columnIndex === 'storageLocation' && !responseDate.showstoragelocation) {
                    return true;
                }
                if (columnIndex === 'additionalCode' && !responseDate.showadditionalcode) {
                    return true;
                }
                if (columnIndex === 'productiondate' && !responseDate.showproductiondate) {
                    return true;
                }
                if (columnIndex === 'expirationdate' && !responseDate.showexpirationdate) {
                    return true;
                }
                if (columnIndex === 'palletNumber' && !responseDate.showpallet) {
                    return true;
                }
                if (columnIndex === 'typeOfPallet' && !responseDate.showtypeofpallet) {
                    return true;
                }
                if (columnIndex === 'batch' && !responseDate.showbatch) {
                    return true;
                }
                if (columnIndex === 'price' && !responseDate.showprice) {
                    return true;
                }

                return false;
            };

            $http({
                method: 'GET',
                url: '../../integration/rest/documentPositions/gridConfig/' + config.document_id + '.html'

            }).then(function successCallback(response) {
                config.readOnly = response.data.readOnly;

                angular.forEach(config.colModel, function (value, key) {
                    if (hideColumnInGrid(value.index, response.data)) {
                        config.colModel[key].hidden = true;
                        config.colModel[key].editrules = config.colModel[key].editrules || {};
                        config.colModel[key].editrules.edithidden = true;
                    }
                });

                $http({
                    method: 'GET',
                    url: '../../rest/typeOfPallets'

                }).then(function successCallback(response) {
                    selectOptionsTypeOfPallets = [':' + translateMessages('qcadooView.typeOfPallet.emptyItem')];
                    angular.forEach(response.data, function (value, key) {
                        selectOptionsTypeOfPallets.push(value.key + ':' + value.value);
                    });

                    config.colModel.filter(function (element, index) {
                        return element.index === 'typeOfPallet';
                    })[0].editoptions.value = selectOptionsTypeOfPallets.join(';');

                    var newConfig = {};
                    newConfig = angular.merge(newConfig, config);
                    $scope.config = newConfig;

                    $('#gridWrapper').unblock();

                }, errorCallback);
            }, errorCallback);

            return config;
        }

        $scope.documentIdChanged = function (id) {
            config.url = '../../integration/rest/documentPositions/' + id + '.html';
            config.document_id = id;

            config.colModel.filter(function (element, index) {
                return element.index === 'document';
            })[0].editoptions.defaultValue = id;

            prepareGridConfig(config);
        };

        $scope.addNewRow = function () {
            $("#add_new_row").addClass("disableButton");
            $("#add_grid").hide();
            $("#edit_grid").hide();
            $("#del_grid").hide();
            jQuery('#grid').addRow(gridAddOptions);
        }

        $scope.data = [];

        // dont close inline edit after fail validations
        $.extend($.jgrid.inlineEdit, {restoreAfterError: false});

        $.extend(true, $.jgrid.inlineEdit, {
            beforeSaveRow: function (option, rowId) {
                if(rowId === '0'){
                    option.url = '../../integration/rest/documentPositions.html';
                    option.errorfunc= errorfunc;
                    option.successfunc= successfunc;
                    option.aftersavefunc= aftersavefunc;
                } else {
                    option.url = '../../integration/rest/documentPositions/' + rowId + '.html';
                    option.errorfunc= errorfunc;
                    option.successfunc= successfunc;
                    option.aftersavefunc= aftersavefunc;
                }
                option.mtype= 'PUT';
            }
        });

        // disable close modal on off click
        $.jqm.params.closeoverlay = false;

        $('#gridWrapper').block({
            message: '<h2>Pozycje będą dostępne po zapisie dokumentu</h2>',
            centerY: false,
            centerX: false,
            css: {
                top: '70px',
                left: ($(window).width() / 2) - 300 + 'px',
            }
        });

    }]);
