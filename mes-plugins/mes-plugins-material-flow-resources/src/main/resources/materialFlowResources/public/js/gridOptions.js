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

function parseAndValidateInputNumber($element) {
    function countEmptyElements(arr) {
        var counterOfEmptyElements = 0;
        for (var index in arr) {
            if (!arr[index]) {
                counterOfEmptyElements++;
            }
        }

        return counterOfEmptyElements;
    }
    var element = $element[0];
    var rawValue = element.value;

    try {
        if (!rawValue) {
            throw 'Empty element value';
        }

        var valueArray = rawValue.split(/[.,]/);

        if (!((valueArray.length === 1 || valueArray.length === 2) && countEmptyElements(valueArray) === 0)) {
            throw 'Value with wrong separator';
        }

        var value = rawValue.replace(',', '.');
        var unitPart = valueArray[0];
        var fractionPart = '1' + (valueArray[1] || '0');

        if (unitPart !== parseFloat(unitPart).toString()) {
            throw 'Invalid unit part';
        }
        if (fractionPart !== parseFloat(fractionPart).toString()) {
            throw 'Invalid fraction part';
        }

        $element.removeClass('error-grid');
        if (value !== $element.val()) {
            $element.val(value);
        }

    } catch (exception) {
        $element.addClass('error-grid');
        $element.val(rawValue);
    }
}

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
                    $(element).empty();
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

                    $('#t_grid').append('<div class="t_grid__container"></div>');
                    $('#t_grid .t_grid__container').append(gridTitle);
                    $('#t_grid .t_grid__container').append(addNewRowButton);

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
                                                var dlgWidth = 800;
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
                                                        var dlgWidth = 800;
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
            var msg = messageArray[i].split('"').join("&#039;");
            msg = QCD.translate(msg);
            if (msg.substr(0, 1) === '[' && msg.substr(-1, 1) === ']') {
                msg = msg.substr(1, msg.length - 2);
            }
            message.push(msg);
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

function openLookup(name, parameters) {
    var lookupHtml = '/lookup.html'
    if (parameters) {
        var urlParams = $.param(parameters);
        lookupHtml = lookupHtml + "?" + urlParams;
    }
    mainController.openModal('body', '../' + name + lookupHtml, false, function onModalClose() {
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

        function getJsonByQuery(url, params, callback) {
            if (params && params.query) {
                return $.ajax({
                    dataType: "json",
                    url: url,
                    data: params,
                    success: function (data) {
                        callback(data);
                    }
                });

            } else {
                callback({entities: [], numberOfResults: 0});
            }
        }

        function getRowIdFromElement(el) {
            var rowId = el.attr('rowId');
            if ('_empty' === rowId) {
                rowId = 0;
            }

            return rowId;
        }

        function showMessage(type, title, content) {
            mainController.showMessage({
                type: type,
                title: title,
                content: content
            });
        }

        function getDocumentId() {
            if (context) {
                var contextObject = JSON.parse(context);
                if (contextObject && contextObject['window.generalTab.form.id']) {
                    return contextObject['window.generalTab.form.id'];
                }
            }

            var config = angular.element($("#GridController")).scope().config;

            return config ? config.document_id : 0;
        }

        function createLookupElement(name, value, url, options, getParametersFunction) {
            var $ac = $('<input class="eac-square" rowId="' + options.rowId + '" />');
            $ac.val(value);
            $ac.autoComplete({
                minChars: 0,
                autoCompleteResult: false,
                source: function (query, response) {
                    var parameters = getParametersFunction ? getParametersFunction() : {};
                    try {
                        xhr.abort();
                    } catch (e) {
                    }
                    xhr = getJsonByQuery(url, $.extend({query: query}, parameters), function (data) {
                        if (data.numberOfResults === 0) {
                            autoCompleteResult = false;
                            response([{
                                    id: 0,
                                    code: QCD.translate('qcadooView.autocomplete.noResults')
                                }]);
                        } else if (data.entities.length === 0) {
                            autoCompleteResult = false;
                            response([{
                                    id: 0,
                                    code: QCD.translate('qcadooView.autocomplete.tooManyResults') + ' (' + data.numberOfResults + ')'
                                }]);
                        } else {
                            autoCompleteResult = true;
                            response(data.entities);
                        }
                    });
                },
                renderItem: function (item, search) {
                    var code = item.code || item.number;
                    var id = item.id;
                    // escape special characters
                    search = search.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
                    var re = new RegExp("(" + search.split(' ').join('|') + ")", "gi");
                    if (autoCompleteResult) {
                        return '<div class="autocomplete-suggestion" data-id="' + id + '" data-val="' + code + '">' + code.replace(re, "<b>$1</b>") + '</div>';
                    } else {
                        return '<div class="autocomplete-no-result" data-id="' + id + '" data-val="' + code + '">' + code.replace(re, "<b>$1</b>") + '</div>';
                    }
                },
                onSelect: function (e, term, item, that) {
                    if (autoCompleteResult) {
                        $(that).trigger('change');
                    }
                }
            });

            var button = $('<button class="editable__searchBtn" value="xxx"></button>');
            button.bind('click', function () {
                var parameters = getParametersFunction ? getParametersFunction() : {};
                openLookup(name, parameters);
            });

            var wrapper = $('<span></span>');
            wrapper.append($ac);
            wrapper.append(button);

            var isReadonly = getColModelByIndex(name).editoptions.readonly === 'readonly';
            $ac.attr('readonly', isReadonly);
            button.attr('disabled', isReadonly);

            return wrapper;
        }

        function resourceLookup_createElement(value, options) {
            var lookup = createLookupElement('resource', value, '/integration/rest/documentPositions/resources.html', options, function () {
                var rowId = getRowIdFromElement($('input', lookup));
                return  {
                    product: getFieldValue('product', rowId),
                    conversion: getFieldValue('conversion', rowId),
                    additionalCode: getFieldValue('additionalCode', rowId),
                    context: getDocumentId()

                };
            });

            $('input', lookup).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    if (t.val()) {
                        fillWithAttributesFromResource(t.val(), getRowIdFromElement(t))
                    } else {
                    	clearResourceRelatedFields(getRowIdFromElement(t));
                    }
                }, 500));
            });
            return lookup;
        }

        function fillWithAttributesFromResource(resource, rowId) {
            $.get('/integration/rest/documentPositions/resourceByNumber/' + getDocumentId() + '/' + encodeURIComponent(resource).replace('%2F', '%252F') + ".html", function (resource) {
                updateFieldValue('batch', resource['batch'], rowId);
                updateFieldValue('productiondate', resource['productionDate'], rowId);
                updateFieldValue('expirationdate', resource['expirationDate'], rowId);
                updateFieldValue('storageLocation', resource['storageLocation'], rowId);
                updateFieldValue('palletNumber', resource['palletNumber'], rowId);
                updateFieldValue('additionalCode', resource['additionalCode'], rowId);
                updateFieldValue('price', resource['price'], rowId);
                updateFieldValue('typeOfPallet', resource['typeOfPallet'], rowId);
            });
        }

        function clearResourceRelatedFields(rowId) {
            var fieldnames = ['resource', 'batch', 'productiondate', 'expirationdate', 'storageLocation', 'palletNumber', 'price', 'typeOfPallet'];
            for (var i in fieldnames) {
                updateFieldValue(fieldnames[i], '', rowId);
            }
        }

        function palletNumbersLookup_createElement(value, options) {
            var lookup = createLookupElement('palletNumber', value, '/rest/palletnumbers', options);

            return lookup;
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
            if (!productNumber) {
                return;
            }
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

        var storageLocations = null;
        function updateStorageLocations(productNumber, document) {
            $.get('/integration/rest/documentPositions/storageLocation/' + productNumber + "/" + document + ".html", function (location) {
                if (location) {
                    var gridData = $('#grid').jqGrid('getRowData');

                    var productInput = $('#product');

                    if (productInput.length) {
                        // edit form
                        updateFieldValue('storageLocation', location['number'], 0);

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
                                    updateFieldValue('storageLocation', location['number'], numberOfInput);
                                }
                            }
                        }
                    }
                }
            });
        }

        function updateResource(productNumber, conversion, ac) {
            var params = {
                context: getDocumentId(),
                product: productNumber,
                additionalCode: ac,
                conversion: conversion
            }
            $.get('/integration/rest/documentPositions/resource.html?' + $.param(params), function (resource) {
                var gridData = $('#grid').jqGrid('getRowData');

                var productInput = $('#product');

                if (productInput.length) {
                    // edit form
                    if (resource) {
                        updateFieldValue('resource', resource['number'], 0);
                        fillWithAttributesFromResource(resource['number'], 0);
                    } else {
                        clearResourceRelatedFields(0);
                    }

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
                                if (resource) {
                                    updateFieldValue('resource', resource['number'], numberOfInput);
                                    fillWithAttributesFromResource(resource['number'], numberOfInput);
                                } else {
                                    clearResourceRelatedFields(numberOfInput);
                                }
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
                    if (t.val()) {
                        if (!$scope.config.outDocument) {
                            updateStorageLocations(t.val(), getDocumentId());
                        } else if ($scope.config.suggestResource) {
                            var conversion = getFieldValue('conversion', getRowIdFromElement(t))
                            var ac = getFieldValue('additionalCode', getRowIdFromElement(t))
                            updateResource(t.val(), conversion, ac);
                        }
                    } else {
                        updateFieldValue('storageLocation', '', getRowIdFromElement(t));
                        updateFieldValue('resource', '', getRowIdFromElement(t));
                        updateFieldValue('batch', '', getRowIdFromElement(t));
                    }
                    clearAdditionalCode(t.val(), getRowIdFromElement(t));
                }, 500));
            });

            return lookup;
        }

        function clearAdditionalCode(newProductNumber, rowId) {
            var additionalCode = getFieldValue('additionalCode', rowId);

            var url = '/rest/additionalcodes';
            getJsonByQuery(url, {query: additionalCode}, function (data) {
                var product = '';
                product = data.entities.filter(function (element, index) {
                    return element.code === additionalCode;
                })[0];

                if (product) {
                    product = product.productnumber;
                }

                if (product !== newProductNumber) {
                    var additionalCodeField = updateFieldValue('additionalCode', '', rowId);
                    additionalCodeField.trigger('change');
                }
            });
        }

        function additionalCodeLookup_createElement(value, options) {
            var url = '/rest/additionalcodes';
            var lookup = createLookupElement('additionalCode', value, url, options, function () {
                return  {
                    productnumber: getFieldValue('product', getRowIdFromElement($('input', lookup))),
                    context: getDocumentId()
                };
            });

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
            getJsonByQuery(url, {query: additionalCode}, function (data) {
                var product = '';

                product = data.entities.filter(function (element, index) {
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

        function storageLocationLookup_createElement(value, options) {
            var lookup = createLookupElement('storageLocation', value, '/integration/rest/documentPositions/storagelocations.html', options, function () {
                return  {
                    product: getFieldValue('product', getRowIdFromElement($('input', lookup))),
                    location: getDocumentId()
                };
            });

            $('input', lookup).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    conversionModified = false;
                    updateProductFromLocation(t.val(), getRowIdFromElement(t));
                }, 500));
            });

            return lookup;
        }

        function updateProductFromLocation(location, rowNumber) {
            $.get('/integration/rest/documentPositions/productFromLocation/' + location + ".html", function (newProduct) {
                if (newProduct) {
                	var productField = updateFieldValue('product', newProduct['name'], rowNumber);
                	productField.trigger('change');
                }

            }, 'json');
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

        function getColModelByIndex(index, c) {
            c = c || $scope.config;
            return c.colModel.filter(function (element, i) {
                return element.index === index;
            })[0];
        }

        function updateConversionByGivenUnitValue(givenUnitValue, rowId) {
            var conversion = '';

            if (available_additionalunits) {
                var entry = available_additionalunits.filter(function (element, index) {
                    return element.key === givenUnitValue;
                })[0];
                if (entry) {
                    quantities[rowId || 0] = {from: entry.quantityfrom, to: entry.quantityto};
                    conversion = roundTo(parseFloat(entry.quantityto) / parseFloat(entry.quantityfrom));
                }
                if (!conversionModified) {
                    if (!firstLoad || getFieldValue('conversion', rowId) === '') {
                        updateFieldValue('conversion', conversion, rowId);
                        if ($scope.config.outDocument && $scope.config.suggestResource) {
                            var product = getFieldValue('product', rowId)
                            var ac = getFieldValue('additionalCode', rowId)
                            updateResource(product, conversion, ac);
                        }
                    }
                }
                touchManuallyQuantityField(rowId);
                firstLoad = false;
            }
        }

        function quantity_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            $($input).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));

                $(this).data("timeout", setTimeout(function () {
                    parseAndValidateInputNumber(t);

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
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);
            $input.attr('readonly', getColModelByIndex('price').editoptions.readonly === 'readonly');

            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                window.clearTimeout(t.data("timeout"));

                $(this).data("timeout", setTimeout(function () {
                    parseAndValidateInputNumber(t);
                }, 500));

            });

            return $input;
        }

        function givenquantity_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                window.clearTimeout(t.data("timeout"));

                $(this).data("timeout", setTimeout(function () {
                    parseAndValidateInputNumber(t);

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
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);
            $input.attr('readonly', getColModelByIndex('conversion').editoptions.readonly === 'readonly');

            $($input).bind('change keydown paste input', function () {
                var t = $(this);
                conversionModified = true;
                window.clearTimeout(t.data("timeout"));

                $(this).data("timeout", setTimeout(function () {
                    parseAndValidateInputNumber(t);

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
                    if ($scope.config.outDocument && $scope.config.suggestResource) {
                        var product = getFieldValue('product', getRowIdFromElement(t))
                        var ac = getFieldValue('additionalCode', getRowIdFromElement(t))
                        updateResource(product, t.val(), ac);
                    }
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
            showMessage('failure', QCD.translate('qcadooView.notification.failure'), message);
            return true;
        }

        function successfunc(rowID, response) {
            $("#add_new_row").removeClass("disableButton");
            $("#add_grid").show();
            $("#edit_grid").show();
            $("#del_grid").show();
            showMessage('success', QCD.translate('qcadooView.notification.success'), QCD.translate('qcadooView.message.saveMessage'));
            return true;
        }

        function errorCallback(response) {
            showMessage('failure', QCD.translate('qcadooView.notification.failure'), response.data.message);
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
                $('#grid').jqGrid('restoreRow', lastSel);

                // now we need to restore the icons in the formatter:"actions"
                lrid = $.jgrid.jqID(lastSel);
                $("tr#" + lrid + " div.ui-inline-edit, " + "tr#" + lrid + " div.ui-inline-del").show();
                $("tr#" + lrid + " div.ui-inline-save, " + "tr#" + lrid + " div.ui-inline-cancel").hide();
            }
        }

        $scope.resize = function () {
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
                QCD.translate('qcadooView.gridColumn.expirationdate'), QCD.translate('qcadooView.gridColumn.productiondate'), QCD.translate('qcadooView.gridColumn.resource'),
                QCD.translate('qcadooView.gridColumn.batch'), QCD.translate('qcadooView.gridColumn.palletNumber'), QCD.translate('qcadooView.gridColumn.typeOfPallet'),
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
                    name: 'act',
                    index: 'act',
                    width: 55,
                    align: 'center',
                    sortable: false,
                    search: false,
                    formatter: 'actions',
                    formatoptions: {
                        keys: true, // we want use [Enter] key to save the row and [Esc] to cancel editing.
                        editOptions: gridEditOptions,
                        url: '../../integration/rest/documentPositions/' + 1 + '.html',
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
                        afterRestore: function () {
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
                    stype: 'select',
                    editoptions: {readonly: 'readonly'},
                    searchoptions: {},
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
                    stype: 'select',
                    editoptions: {
                        custom_element: givenunit_createElement,
                        custom_value: givenunit_value
                    },
                    searchoptions: {},
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
                            if (getColModelByIndex('expirationdate').editoptions.readonly !== 'readonly') {
                                var options = $.datepicker.regional[window.locale];
                                options.showOn = 'button';
                                options.buttonImage = '/qcadooView/public/css/crud/images/form/f_calendar.png';
                                options.buttonImageOnly = true;
                                options.buttonText = 'Wybierz';
                                options.altField = element;
                                $(element).datepicker(options);
                            }
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
                            if (getColModelByIndex('productiondate').editoptions.readonly !== 'readonly') {
                                var options = $.datepicker.regional[window.locale];
                                options.showOn = 'button';
                                options.buttonImage = '/qcadooView/public/css/crud/images/form/f_calendar.png';
                                options.buttonImageOnly = true;
                                options.buttonText = 'Wybierz';
                                options.altField = element;
                                $(element).datepicker(options);
                            }
                        }
                    },
                    formoptions: {
                        rowpos: 4,
                        colpos: 2
                    },
                },
                {
                    name: 'resource',
                    index: 'resource',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: resourceLookup_createElement,
                        custom_value: lookup_value
                    },
                    formoptions: {
                        rowpos: 5,
                        colpos: 2
                    },
                },
                {
                    name: 'batch',
                    index: 'batch',
                    editable: true,
                    required: true,
                    formoptions: {
                        rowpos: 6,
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
                        rowpos: 7,
                        colpos: 2
                    },
                },
                {
                    name: 'typeOfPallet',
                    index: 'typeOfPallet',
                    editable: true,
                    required: true,
                    edittype: 'select',
                    stype: 'select',
                    editoptions: {},
                    searchoptions: {},
                    formoptions: {
                        rowpos: 8,
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
                        rowpos: 9,
                        colpos: 2
                    },
                }
            ],
            pager: "#jqGridPager",
            gridComplete: function () {
                var grid = $('#grid');
                var rows = grid.jqGrid('getDataIDs');
                if ($scope.config.readOnly) {
                    for (i = 0; i < rows.length; i++)
                    {
                        $("tr#" + rows[i] + " div.ui-inline-edit").hide();

                    }
                }

            },
            onSelectRow: function (id) {
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
        };

        function prepareGridConfig(config) {
            var readOnlyInType = function (outDocument, columnIndex, responseDate) {
                if (outDocument && (columnIndex === 'expirationdate' || columnIndex === 'productiondate' ||
                        columnIndex === 'batch' || columnIndex === 'price' ||
                        columnIndex === 'palletNumber' || columnIndex === 'typeOfPallet' || columnIndex === 'storageLocation')) {
                    return true;
                }
                if (!outDocument && columnIndex === 'resource') {
                    return true;
                }
                return false;
            };

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
                if (columnIndex === 'resource' && !responseDate.showresource) {
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
                config.suggestResource = response.data.suggestResource;
                config.outDocument = response.data.outDocument;

                angular.forEach(config.colModel, function (value, key) {
                    if (hideColumnInGrid(value.index, response.data)) {
                        config.colModel[key].hidden = true;
                        config.colModel[key].editrules = config.colModel[key].editrules || {};
                        config.colModel[key].editrules.edithidden = true;
                    }
                    if (readOnlyInType(config.outDocument, value.index, response.data)) {
                        config.colModel[key].editoptions = config.colModel[key].editoptions || {};
                        if (config.colModel[key].edittype === 'select') {
                            config.colModel[key].editoptions.disabled = 'disabled';

                        } else {
                            config.colModel[key].editoptions.readonly = 'readonly';
                        }
                    }
                });

                $http({
                    method: 'GET',
                    url: '../../rest/typeOfPallets'

                }).then(function successCallback(response) {
                    var selectOptionsTypeOfPallets = [];
                    angular.forEach(response.data, function (value, key) {
                        selectOptionsTypeOfPallets.push(value.key + ':' + value.value);
                    });

                    getColModelByIndex('typeOfPallet', config).editoptions.value = ':' + translateMessages('qcadooView.emptyItem') + ";" + selectOptionsTypeOfPallets.join(';');
                    getColModelByIndex('typeOfPallet', config).searchoptions.value = ':' + translateMessages('qcadooView.allItem') + ";" + selectOptionsTypeOfPallets.join(';');

                    $http({
                        method: 'GET',
                        url: '../../rest/units'

                    }).then(function successCallback(response) {
                        selectOptionsUnits = [':' + translateMessages('qcadooView.allItem')];
                        angular.forEach(response.data, function (value, key) {
                            selectOptionsUnits.push(value.key + ':' + value.value);
                        });

                        getColModelByIndex('unit', config).searchoptions.value = selectOptionsUnits.join(';');
                        getColModelByIndex('givenunit', config).searchoptions.value = selectOptionsUnits.join(';');

                        var newConfig = {};
                        newConfig = angular.merge(newConfig, config);
                        $scope.config = newConfig;

                        $('#gridWrapper').unblock();

                    }, errorCallback);
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
                if (rowId === '0') {
                    option.url = '../../integration/rest/documentPositions.html';
                    option.errorfunc = errorfunc;
                    option.successfunc = successfunc;
                    option.aftersavefunc = aftersavefunc;
                } else {
                    option.url = '../../integration/rest/documentPositions/' + rowId + '.html';
                    option.errorfunc = errorfunc;
                    option.successfunc = successfunc;
                    option.aftersavefunc = aftersavefunc;
                }
                option.mtype = 'PUT';
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
