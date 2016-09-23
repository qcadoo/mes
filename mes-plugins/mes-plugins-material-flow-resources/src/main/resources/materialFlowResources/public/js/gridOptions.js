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

function gridRunner(action) {
    QCD.components.elements.utils.LoadingIndicator.blockElement(parent.$('body'));
    action();
    QCD.components.elements.utils.LoadingIndicator.unblockElement(parent.$('body'));
}

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
    var rawValue = element.value.replace(/^0+(?!\.|,|$)/, '');

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
    
    return $element.val();
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

                    var positionsHeader = QCD.translate('documentGrid.gridHeader.positions');
                    var newHeader = QCD.translate('documentGrid.gridHeader.new');
                    var addNewRowButton = '<div id="add_new_row" class="headerActionButton headerButtonEnabled ' + (newValue.readOnly ? 'disabled-button"' : '" onclick="return addNewRow();"') + '> <a href="#"><span>' +
                            '<div class="icon" id="add_new_icon"></div>' +
                            '<div class="hasIcon">' + newHeader + '</div></div>';

                    var gridTitle = '<div class="gridTitle">' + positionsHeader + ' <span id="rows-num">(0)</span></div>';

                    $('#t_grid').append('<div class="t_grid__container"></div>');
                    $('#t_grid .t_grid__container').append(gridTitle);
                    $('#t_grid .t_grid__container').append(addNewRowButton);

                    $(table).jqGrid('filterToolbar');
                    mainController.getComponentByReferenceName("positionsGrid").setComponentChanged(false);

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
                                                params.url = '../../rest/rest/documentPositions/' + postdata.grid_id + ".html";
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
                                                        params.url = '../../rest/rest/documentPositions.html';
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
                                                                params.url = '../../rest/rest/documentPositions/' + encodeURIComponent(postdata) + ".html";
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

function saveAllRows() {
    var grid = $("#grid");
    var ids = grid.jqGrid('getDataIDs');

    for (var i = 0; i < ids.length; i++) {
        grid.saveRow(ids[i]);
    }
}

function viewRefresh() {
    angular.element($("#GridController")).scope().cancelEditing();
}

function documentIdChanged(id) {
    saveAllRows();
    angular.element($("#GridController")).scope().documentIdChanged(id);
    return false;
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

function clearSelect(field,rowId)
{
        var productInput = $('#product');
        var selector = null;

        if (productInput.length) {
            // edit form
            selector = $('#' + field);

        } else {
            // edit inline
            selector = $('#' + rowId + '_' + field);
        }
    selector = $('#' + rowId + '_' + field);
    $(selector).empty();
   $(selector).val([]);
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
        var hasAdditionalUnit = false;

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
                                    code: QCD.translate('documentGrid.autocomplete.noResults')
                                }]);
                        } else if (data.entities.length === 0) {
                            autoCompleteResult = false;
                            response([{
                                    id: 0,
                                    code: QCD.translate('documentGrid.autocomplete.tooManyResults') + ' (' + data.numberOfResults + ')'
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
            $ac.attr('disabled', isReadonly);
            button.attr('disabled', isReadonly);

            return wrapper;
        }

        function resourceLookup_createElement(value, options) {
            var lookup = createLookupElement('resource', value, '/rest/rest/documentPositions/resources.html', options, function () {
                var rowId = getRowIdFromElement($('input', lookup));
                var params;
                if (hasAdditionalUnit) {
                    params = {
                        product: getFieldValue('product', rowId),
                        conversion: getFieldValue('conversion', rowId),
                        ac: getFieldValue('additionalCode', rowId),
                        context: getDocumentId()
                    }
                } else {
                    params = {
                        product: getFieldValue('product', rowId),
                        conversion: 1,
                        ac: getFieldValue('additionalCode', rowId),
                        context: getDocumentId()
                    }
                }
                return  params;
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
            $.get('/rest/rest/documentPositions/resourceByNumber/' + getDocumentId() + '/' + encodeURIComponent(resource).replace('%2F', '%252F') + ".html", function (resource) {
                updateFieldValue('batch', resource['batch'], rowId);
                updateFieldValue('productionDate', resource['productionDate'], rowId);
                updateFieldValue('expirationDate', resource['expirationDate'], rowId);
                updateFieldValue('storageLocation', resource['storageLocation'], rowId);
                updateFieldValue('palletNumber', resource['palletNumber'], rowId);
                updateFieldValue('additionalCode', resource['additionalCode'], rowId);
                updateFieldValue('price', resource['price'], rowId);
                updateFieldValue('typeOfPallet', resource['typeOfPallet'], rowId);
                updateFieldValue('waste', resource['waste'], rowId);
            });
        }

        function clearResourceRelatedFields(rowId) {
            var fieldnames = ['resource', 'batch', 'productionDate', 'expirationDate', 'storageLocation', 'palletNumber', 'price', 'typeOfPallet', 'waste'];
            for (var i in fieldnames) {
                updateFieldValue(fieldnames[i], '', rowId);
            }
        }

        function palletNumbersLookup_createElement(value, options) {
            var lookup = createLookupElement('palletNumber', value, '/rest/palletnumbers', options);

            return lookup;
        }

        function getFieldValue(field, rowId) {
            return getField(field, rowId).val();
        }

        function getField(field, rowId) {
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

            return element;
        }

        function updateNameInGridByProduct(productNumber, _rowID) {
            if (!productNumber) {
                return;
            }
            $.get('/rest/rest/documentPositions/product/' + productNumber + ".html", function (product) {
                updateFieldValue('productName', product.name, _rowID);
            });
        }

        var available_additionalunits = null;

        function updateUnitsInGridByProduct(productNumber, additionalUnitValue) {
            if (!productNumber) {
                return;
            }
            $.get('/rest/rest/documentPositions/units/' + productNumber + ".html", function (units) {
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
                    hasAdditionalUnit = units['additionalunit'] !== units['unit'];
                    if (hasAdditionalUnit) {
                        additionalUnitInput.attr('disabled', 'disabled');
                    } else {
                        additionalUnitInput.removeAttr('disabled');
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
                                hasAdditionalUnit = units['additionalunit'] !== units['unit'];
                                if (hasAdditionalUnit) {
                                    additionalUnitInput.attr('disabled', 'disabled');
                                } else {
                                    additionalUnitInput.removeAttr('disabled');
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
            $.get('/rest/rest/documentPositions/storageLocation/' + productNumber + "/" + document + ".html", function (location) {
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
            var params;
            if (hasAdditionalUnit) {
                params = {
                    context: getDocumentId(),
                    product: productNumber,
                    ac: ac,
                    conversion: conversion
                }
            } else {
                params = {
                    context: getDocumentId(),
                    product: productNumber,
                    ac: ac,
                    conversion: 1
                }
            }
            $.get('/rest/rest/documentPositions/resource.html?' + $.param(params), function (resource) {
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
                    updateNameInGridByProduct(t.val(), getRowIdFromElement(t));
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
                        updateFieldValue('productName', '', getRowIdFromElement(t));
                        updateFieldValue('unit', '', getRowIdFromElement(t));
                        updateFieldValue('givenunit', '', getRowIdFromElement(t));
                        updateFieldValue('givenquantity', '', getRowIdFromElement(t));
                        updateFieldValue('conversion', '', getRowIdFromElement(t));
                        updateFieldValue('waste', '0', getRowIdFromElement(t));

                        clearSelect('givenunit', getRowIdFromElement(t));
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
            var productnumber = getFieldValue('product', rowId);
            getJsonByQuery(url, {query: additionalCode, productnumber: productnumber}, function (data) {
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
            var lookup = createLookupElement('storageLocation', value, '/rest/rest/documentPositions/storagelocations.html', options, function () {
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
            $.get('/rest/rest/documentPositions/productFromLocation/' + location + ".html", function (newProduct) {
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

        function numberFormatter(cellvalue, options, rowObject) {
            var val = cellvalue || '';
            return '<span class="number-cell">' + val + '</span>';
        }

        function numberUnformat(cellvalue, options, cell) {
            var val = $('span', cell).text();
            return val || '';
        }

        function touchManuallyQuantityField(rowId) {
            var productInput = $('#product');
            quantityValue = undefined;

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
            var col = c.colModel.filter(function (element, i) {
                return element.index === index;
            })[0];
            if (!col) {
                console.error(index);
            }
            return col;
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

            var conversionReadonly = getColModelByIndex('conversion').editoptions.readonly === 'readonly' || (getFieldValue('unit', rowId) === getFieldValue('givenunit', rowId));
            getField('conversion', rowId).attr('readonly', conversionReadonly)
        }

        var quantityValue;
        function quantity_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            quantityValue = value;
            var quantityValueNew;
            $($input).bind('change keydown paste input', function () {
                var t = $(this);
                window.clearTimeout(t.data("timeout"));

                quantityValueNew = t.val();
                if (quantityValue !== quantityValueNew) {
                    quantityValue = quantityValueNew;
                    $(this).data("timeout", setTimeout(gridRunner(function () {
                        quantityValueNew = parseAndValidateInputNumber(t);

                        var rowId = getRowIdFromElement(t);

                        var conversion = getFieldValue('conversion', rowId);
                        var newGivenQuantity = null;

                        if (quantities[rowId]) {
                            newGivenQuantity = roundTo(quantityValueNew * conversion);
                        }
                        newGivenQuantity = roundTo(newGivenQuantity);
                        if (!newGivenQuantity || t.hasClass('error-grid')) {
                            newGivenQuantity = '';
                        }

                        updateFieldValue('givenquantity', newGivenQuantity, rowId);
                    }, 500)));
                }
            });


            return $input;
        }

        function price_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);
            $input.attr('readonly', getColModelByIndex('price').editoptions.readonly === 'readonly');

            var priceValue = value;
            var priceValueNew;
            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                window.clearTimeout(t.data("timeout"));
                priceValueNew = t.val();
                if (priceValue !== priceValueNew) {
                    priceValue = priceValueNew;

                    $(this).data("timeout", setTimeout(function () {
                        gridRunner(function () {
                            parseAndValidateInputNumber(t);
                        });
                    }, 500));
                }
            });

            return $input;
        }

        function givenquantity_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            var givenquantityValue = value;
            var givenquantityValueNew;
            $($input).bind('change keydown paste input', function () {
                var t = $(this);

                window.clearTimeout(t.data("timeout"));
                givenquantityValueNew = t.val();
                if (givenquantityValue !== givenquantityValueNew) {
                    givenquantityValue = givenquantityValueNew;
                    $(this).data("timeout", setTimeout(gridRunner(function () {
                        parseAndValidateInputNumber(t);

                        var rowId = getRowIdFromElement(t);

                        var conversion = getFieldValue('conversion', rowId);

                        var newQuantity = null;
                        if (quantities[rowId]) {
                            newQuantity = roundTo(givenquantityValueNew * (1 / conversion));
                        }
                        newQuantity = roundTo(newQuantity);
                        if (!newQuantity || t.hasClass('error-grid')) {
                            newQuantity = '';
                        }

                        updateFieldValue('quantity', newQuantity, rowId);
                    }, 500)));
                }
            });

            return $input;
        }

        function conversion_createElement(value, options) {
            var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);
            $input.attr('readonly', getColModelByIndex('conversion').editoptions.readonly === 'readonly');

            var conversionValue = value;
            var conversionValueNew;
            $($input).bind('change keydown paste input', function () {
                var t = $(this);
                conversionModified = true;
                window.clearTimeout(t.data("timeout"));

                conversionValueNew = t.val();
                if (conversionValue !== conversionValueNew) {
                    conversionValue = conversionValueNew;
                    $(this).data("timeout", setTimeout(gridRunner(function () {
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
                    }, 500)));
                }
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
            showMessage('failure', QCD.translate('documentGrid.notification.failure'), message);
            return true;
        }

        function successfunc(rowID, response) {
            prepareViewOnEndEdit();
            showMessage('success', QCD.translate('documentGrid.notification.success'), QCD.translate('documentGrid.message.saveMessage'));
            return true;
        }

        function errorCallback(response) {
            showMessage('failure', QCD.translate('documentGrid.notification.failure'), response.data.message);
        }

        function aftersavefunc() {
            $("#grid").trigger("reloadGrid");
        }

        function prepareViewOnStartEdit() {
            mainController.getComponentByReferenceName("positionsGrid").setComponentChanged(true);
            $("#add_new_row").addClass("disableButton");
            $("#add_grid").hide();
            $("#edit_grid").hide();
            $("#del_grid").hide();
        }

        function prepareViewOnEndEdit() {
            mainController.getComponentByReferenceName("positionsGrid").setComponentChanged(false);
            $("#add_new_row").removeClass("disableButton");
            $("#add_grid").show();
            $("#edit_grid").show();
            $("#del_grid").show();
        }

        function cancelEditing() {
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
        $scope.cancelEditing = cancelEditing;

        $scope.resize = function () {
            jQuery('#grid').setGridWidth($("#window\\.positionsGridTab").width() - 23, true);
        };
        $("#window\\.positionsGridTab").resize($scope.resize);

        var gridEditOptions = {
            keys: true,
            url: '../../rest/rest/documentPositions.html',
            mtype: 'PUT',
            errorfunc: errorfunc,
            successfunc: successfunc,
            aftersavefunc: aftersavefunc
        };

        var gridAddOptions = {
            rowID: "0",
            url: '../../rest/rest/documentPositions.html',
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
            url: '../../rest/rest/documentPositions/' + getDocumentId() + '.html',
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
                    editable: false,
                    formatter: numberFormatter
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
                        url: '../../rest/rest/documentPositions/' + 1 + '.html',
                        delbutton: false,
                        onEdit: function (id) {
                            if (typeof (lastSel) !== "undefined" && id !== lastSel) {
                                cancelEditing(id);
                            }
                            prepareViewOnStartEdit();
                            gridEditOptions.url = '../../rest/rest/documentPositions/' + id + '.html';
                            lastSel = id;
                        },
                        afterRestore: function () {
                            prepareViewOnEndEdit();
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
                    name: 'productName',
                    index: 'productName',
                    editable: true,
                    editoptions: {readonly: 'readonly'},
                    searchoptions: {},
                    formoptions: {
                        rowpos: 3,
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
                        rowpos: 4,
                        colpos: 1
                    },
                },
                {
                    name: 'quantity',
                    index: 'quantity',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    formatter: numberFormatter,
                    unformat: numberUnformat,
                    editoptions: {
                        custom_element: quantity_createElement,
                        custom_value: input_value,
                    },
                    formoptions: {
                        rowpos: 5,
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
                    formoptions: {
                        rowpos: 6,
                        colpos: 1
                    },
                },
                {
                    name: 'givenquantity',
                    index: 'givenquantity',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    formatter: numberFormatter,
                    unformat: numberUnformat,
                    editoptions: {
                        custom_element: givenquantity_createElement,
                        custom_value: input_value
                    },
                    formoptions: {
                        rowpos: 7,
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
                        rowpos: 8,
                        colpos: 1
                    },
                },
                {
                    name: 'conversion',
                    index: 'conversion',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    formatter: numberFormatter,
                    unformat: numberUnformat,
                    editoptions: {
                        custom_element: conversion_createElement,
                        custom_value: input_value
                    },
                    formoptions: {
                        rowpos: 9,
                        colpos: 1
                    },
                },
                {
                    name: 'price',
                    index: 'price',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    formatter: numberFormatter,
                    unformat: numberUnformat,
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
                    name: 'expirationDate',
                    index: 'expirationDate',
                    width: 150,
                    editable: true,
                    required: true,
                    edittype: "text",
                    editoptions: {
                        dataInit: function (element) {
                            if (getColModelByIndex('expirationDate').editoptions.readonly !== 'readonly') {
                                var options = $.datepicker.regional[window.locale];
                                options.showOn = 'button';
//                                options.buttonImage = '/qcadooView/public/css/crud/images/form/f_calendar.png';
//                                options.buttonImageOnly = true;
                                options.buttonText = '';
                                options.altField = element;
                                $(element).datepicker(options);
                                $(element).mask("2999-19-39");
                            }
                        }
                    },
                    formoptions: {
                        rowpos: 3,
                        colpos: 2
                    },
                },
                {
                    name: 'productionDate',
                    index: 'productionDate',
                    width: 150,
                    editable: true,
                    required: true,
                    edittype: "text",
                    editoptions: {
                        dataInit: function (element) {
                            if (getColModelByIndex('productionDate').editoptions.readonly !== 'readonly') {
                                var options = $.datepicker.regional[window.locale];
                                options.showOn = 'button';
//                                options.buttonImage = '/qcadooView/public/css/crud/images/form/f_calendar.png';
//                                options.buttonImageOnly = true;
                                options.buttonText = '';
                                options.altField = element;
                                $(element).datepicker(options);
                                $(element).mask("2999-19-39");
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
                },
                {
                	name: 'waste',
                	index: 'waste',
                	editable: true,
                	edittype: 'checkbox',
                	formatter: 'checkbox',
                	editoptions: {
                		value: '1:0'
                	},
                	formoptions: {
                		rowpos: 10,
                		colpos: 1
                	},
                	searchoptions: {
                		sopt: ['eq', 'ne'],
                		value:': ;1:'+translateMessages('documentGrid.yes')+';0:'+translateMessages('documentGrid.no')
        			},
        			stype: 'select'
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
                $('#rows-num').text('(' + rows.length + ')');
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
                if (outDocument && (columnIndex === 'expirationDate' || columnIndex === 'productionDate' ||
                        columnIndex === 'batch' || columnIndex === 'price' || columnIndex === 'waste' ||
                        columnIndex === 'palletNumber' || columnIndex === 'typeOfPallet' || columnIndex === 'storageLocation')) {
                    return true;
                }
                if (!outDocument && (columnIndex === 'resource')) {
                    return true;
                }
                return false;
            };

            $http({
                method: 'GET',
                url: '../../rest/rest/documentPositions/gridConfig/' + config.document_id + '.html'

            }).then(function successCallback(response) {
                config.readOnly = response.data.readOnly;
                config.suggestResource = response.data.suggestResource;
                config.outDocument = response.data.outDocument;

                var columns = [getColModelByIndex('id', config), getColModelByIndex('document', config)];
                var colNames = ['ID', 'document'];

                angular.forEach(response.data.columns, function (showColumnInGrid, key) {
                    var gridColModel = getColModelByIndex(key, config);

                    if (!showColumnInGrid) {
                        gridColModel.hidden = true;
                        gridColModel.editrules = gridColModel.editrules || {};
                        gridColModel.editrules.edithidden = true;
                    }
                    if (readOnlyInType(config.outDocument, key, response.data)) {
                        gridColModel.editoptions = gridColModel.editoptions || {};
                        if (gridColModel.edittype === 'select' || gridColModel.edittype === 'checkbox') {
                            gridColModel.editoptions.disabled = 'disabled';

                        } else {
                            gridColModel.editoptions.readonly = 'readonly';
                        }
                    }

                    columns.push(gridColModel);
                    colNames.push(QCD.translate('documentGrid.gridColumn.' + key));
                });

                config.colModel = columns;
                config.colNames = colNames;

                $http({
                    method: 'GET',
                    url: '../../rest/typeOfPallets'

                }).then(function successCallback(response) {
                    var selectOptionsTypeOfPallets = [':' + translateMessages('documentGrid.allItem')];
                    var selectOptionsTypeOfPalletsEdit = [':' + translateMessages('documentGrid.emptyItem')];
                    angular.forEach(response.data, function (value, key) {
                        selectOptionsTypeOfPallets.push(value.key + ':' + value.value);
                        selectOptionsTypeOfPalletsEdit.push(value.key + ':' + value.value);
                    });

                    getColModelByIndex('typeOfPallet', config).editoptions.value = selectOptionsTypeOfPalletsEdit.join(';');
                    getColModelByIndex('typeOfPallet', config).searchoptions.value = selectOptionsTypeOfPallets.join(';');

                    $http({
                        method: 'GET',
                        url: '../../rest/units'

                    }).then(function successCallback(response) {
                        selectOptionsUnits = [':' + translateMessages('documentGrid.allItem')];
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
            config.url = '../../rest/rest/documentPositions/' + id + '.html';
            config.document_id = id;

            config.colModel.filter(function (element, index) {
                return element.index === 'document';
            })[0].editoptions.defaultValue = id;

            prepareGridConfig(config);
        };

        $scope.addNewRow = function () {
            prepareViewOnStartEdit();
            jQuery('#grid').addRow(gridAddOptions);
        }

        $scope.data = [];

        // dont close inline edit after fail validations
        $.extend($.jgrid.inlineEdit, {restoreAfterError: false});

        $.jgrid.edit = $.jgrid.edit || {};
        $.jgrid.edit.addCaption = '';
        $.jgrid.edit.editCaption = '';
        $.jgrid.edit.bSubmit = '<label>' + $.jgrid.edit.bSubmit + '</label>';
        $.jgrid.edit.bCancel = '<label>' + $.jgrid.edit.bCancel + '</label>';

        $.extend(true, $.jgrid.inlineEdit, {
            beforeSaveRow: function (option, rowId) {
                if (rowId === '0') {
                    option.url = '../../rest/rest/documentPositions.html';
                    option.errorfunc = errorfunc;
                    option.successfunc = successfunc;
                    option.aftersavefunc = aftersavefunc;
                } else {
                    option.url = '../../rest/rest/documentPositions/' + rowId + '.html';
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
            message: '<h2>'+QCD.translate('documentGrid.firstSaveDocument')+'</h2>',
            centerY: false,
            centerX: false,
            css: {
                top: '70px',
                left: ($(window).width() / 2) - 300 + 'px',
            }
        });

    }]);
