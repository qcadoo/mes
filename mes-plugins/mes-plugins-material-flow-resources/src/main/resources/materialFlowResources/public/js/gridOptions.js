/*
 * jQuery resize event - v1.1 - 3/14/2010
 * http://benalman.com/projects/jquery-resize-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */
(function ($, h, c) {
        var a = $([]), e = $.resize = $.extend($.resize, {}), i, k = "setTimeout", j = "resize",
            d = j + "-special-event", b = "delay", f = "throttleWindow";
        e[b] = 250;
        e[f] = true;
        $.event.special[j] = {
            setup: function () {
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
            }
        };

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
        }
    }
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
                    var deleteHeader = QCD.translate('documentGrid.gridHeader.delete');
                    var addNewRowButton = '<div id="add_new_row" class="headerActionButton headerButtonEnabled ' +
                        (newValue.readOnly ? 'disabled-button"' : '" onclick="return addNewRow();"') + '> <a href="#"><span>' +
                        '<div class="icon" id="add_new_icon"></div>' +
                        '<div class="hasIcon">' + newHeader + '</div></div>';
                    var deleteRowButton = '<div id="delete_row" class="headerActionButton headerButtonEnabled ' +
                        (newValue.readOnly ? 'disabled-button"' : '" onclick="return deleteRow();"') + '> <a href="#"><span>' +
                        '<div class="icon" id="delete_icon"></div>' +
                        '<div class="hasIcon">' + deleteHeader + '</div></div>';

                    var gridTitle = '<div class="gridTitle">' + positionsHeader + ' <span id="rows-num">(0)</span></div>';

                    $('#t_grid').append('<div class="t_grid__container"></div>');
                    $('#t_grid .t_grid__container').append(gridTitle);
                    $('#t_grid .t_grid__container').append(addNewRowButton);
                    $('#t_grid .t_grid__container').append(deleteRowButton);

                    $(table).jqGrid('filterToolbar');
                    mainController.getComponentByReferenceName("positionsGrid").setComponentChanged(false);

                    if (!newValue.readOnly) {
                        $(table).navGrid('#jqGridPager',
                            // the buttons to appear on the toolbar of the grid
                            {
                                edit: false,
                                add: false,
                                del: true,
                                search: false,
                                refresh: false,
                                view: false,
                                position: "left",
                                cloneToTop: false
                            },
                            {}, {},
                            // options for the Delete Dailog
                            {
                                mtype: "DELETE",
                                serializeDelData: function () {
                                    return ""; // don't send and body for the HTTP DELETE
                                },
                                onclickSubmit: function (params, postdata) {
                                    params.url = '../../rest/rest/documentPositions/' + encodeURIComponent(postdata) + ".html";
                                },
                                afterComplete: function (response, postdata, formid) {
                                    refreshForm();
                                },
                                errorTextFormat: function (response) {
                                    return translateMessages(JSON.parse(response.responseText).message);
                                }
                            });
                        $("#del_grid").hide();
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
    return +(Math.floor(parseFloat(n) + "e+" + places) + "e-" + places);
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

function refreshForm() {
    var mainViewComponent = mainController.getComponentByReferenceName("form") || mainController.getComponentByReferenceName("grid");
    if (mainViewComponent) {
        mainViewComponent.performRefresh();
    }
}

function documentIdChanged(id) {
    saveAllRows();
    angular.element($("#GridController")).scope().documentIdChanged(id);
    return false;
}

function getSelectedRowId() {
    return jQuery('#grid').jqGrid('getGridParam', 'selarrrow');
}

function addNewRow() {
    angular.element($("#GridController")).scope().addNewRow();
}

function deleteRow() {
    angular.element($("#GridController")).scope().deleteRow();
}

function openLookup(name, parameters) {
    var lookupHtml = '/lookup.html'
    if (name == 'attribute') {
        mainController.openModal('body', '../' + name + "/" + parameters.custom_attr_name + lookupHtml, false, function onModalClose() {
        }, function onModalRender(modalWindow) {
        }, {width: 1000, height: 560});
    } else {
        if (parameters) {
            var urlParams = $.param(parameters);
            lookupHtml = lookupHtml + "?" + urlParams;
        }

        mainController.openModal('body', '../' + name + lookupHtml, false, function onModalClose() {
        }, function onModalRender(modalWindow) {
        }, {width: 1000, height: 560});
    }
}

function updateFieldValue(field, value, rowId) {
    // edit inline
    var selector = $("[id='" + rowId + '_' + field + "']");

    var element = $(selector);
    if (element.length && element[0].tagName.toLowerCase() === 'span') {
        element = $('input', element);
    }

    if (element.is(':checkbox')) {
        return element.prop('checked', value);
    } else {
        return element.val(value);
    }
}

function clearSelect(field, rowId) {
    var selector = $('#' + rowId + '_' + field);
    $(selector).empty();
    $(selector).val([]);
}

function onSelectLookupRow(row, recordName) {
    if (row) {
        var code = row.code || row.number || row.value;
        recordName = recordName.replace('attribute/', '');
        var rowId = $('#product').length ? null : jQuery('#grid').jqGrid('getGridParam', 'selrow');
        var field = updateFieldValue(recordName, code, rowId);
        if (recordName == "batch") {
            if (row.id == 0) {
                var fieldBatchId = updateFieldValue("batchId", null, rowId);
                fieldBatchId.trigger('change');
            } else {
                var fieldBatchId = updateFieldValue("batchId", row.id, rowId);
                fieldBatchId.trigger('change');
            }
        }
        field.trigger('change');
    }

    mainController.closeThisModalWindow();
}

var messagesController = new QCD.MessagesController();
var columnConfiguration;
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
        $ac.keyup(function () {
            if (!this.value) {
                if (name == "batch") {
                    var rowId = $('#product').length ? null : jQuery('#grid').jqGrid('getGridParam', 'selrow');
                    var fieldBatchId = updateFieldValue("batchId", null, rowId);
                    fieldBatchId.trigger('change');
                }
            }
        });
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
                var code = item.code || item.number || item.value;
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
                    if (name == "batch") {
                        var rowId = $('#product').length ? null : jQuery('#grid').jqGrid('getGridParam', 'selrow');

                        var fieldBatchId = updateFieldValue("batchId", item.context.dataset.id, rowId);
                        fieldBatchId.trigger('change');
                    }
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

        var isReadonly
        if (name == 'attribute') {
            isReadonly = getColModelByIndex(options.custom_attr_name).editoptions.readonly === 'readonly';
        } else {
            isReadonly = getColModelByIndex(name).editoptions.readonly === 'readonly';
        }

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
                    batch: getFieldValue('batch', rowId),
                    batchId: getFieldValue('batchId', rowId),
                    context: getDocumentId()
                }
            } else {
                params = {
                    product: getFieldValue('product', rowId),
                    conversion: 1,
                    batch: getFieldValue('batch', rowId),
                    batchId: getFieldValue('batchId', rowId),
                    context: getDocumentId()
                }
            }
            return params;
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
        $.get('/rest/rest/documentPositions/resourceByNumber/' + getDocumentId() + '/' + Base64.encodeURI(resource) + ".html", function (resource) {
            if (resource !== '') {
                updateFieldValue('batch', resource['batch'], rowId);
            }
            updateFieldValue('productionDate', resource['productionDate'], rowId);
            updateFieldValue('expirationDate', resource['expirationDate'], rowId);
            updateFieldValue('storageLocation', resource['storageLocation'], rowId);
            updateFieldValue('palletNumber', resource['palletNumber'], rowId);
            updateFieldValue('price', resource['price'], rowId);
            updateFieldValue('typeOfLoadUnit', resource['typeOfLoadUnit'], rowId);
            updateFieldValue('waste', resource['waste'], rowId);
            if ($scope.config.outDocument) {
                var positionQuantity = getFieldValue('quantity', rowId);
                var resourceQuantity = resource['quantity'];
                if (positionQuantity >= resourceQuantity) {
                    updateFieldValue('lastResource', resource['lastResource'], rowId);
                }
            }
            angular.forEach(columnConfiguration, function (columnInGrid, key) {
                if (columnInGrid.forAttribute) {
                    updateFieldValue(columnInGrid.name, '', rowId);
                }
            });
            angular.forEach(resource['attrs'], function (value, key) {
                updateFieldValue(key, value, rowId);
            });
        });
    }

    function clearResourceRelatedFields(rowId) {
        var fieldnames = ['resource', 'productionDate', 'expirationDate', 'storageLocation', 'palletNumber', 'price', 'typeOfLoadUnit', 'waste', 'lastResource'];

        for (var i in fieldnames) {
            updateFieldValue(fieldnames[i], '', rowId);
        }

        angular.forEach(columnConfiguration, function (columnInGrid, key) {
            if (columnInGrid.forAttribute) {
                updateFieldValue(columnInGrid.name, '', rowId);
            }
        });
    }

    function palletNumbersLookup_createElement(value, options) {
        var lookup = createLookupElement('palletNumber', value, '/rest/palletnumbers', options);

        $('input', lookup).bind('change keydown paste input', function () {
            var t = $(this);
            window.clearTimeout(t.data("timeout"));
            $(this).data("timeout", setTimeout(function () {
                if (t.val()) {
                    fillTypeOfLoadUnitFromLoadUnit(t.val(), getRowIdFromElement(t))
                } else {
                    updateFieldValue('typeOfLoadUnit', '', getRowIdFromElement(t));
                }
            }, 500));
        });

        return lookup;
    }

    function fillTypeOfLoadUnitFromLoadUnit(pallet, rowId) {
        $.get('/rest/rest/documentPositions/typeOfLoadUnitByLoadUnit/' + getDocumentId() + '/' + Base64.encodeURI(pallet) + ".html", function (typeOfLoadUnit) {
            if (typeOfLoadUnit !== '') {
                updateFieldValue('typeOfLoadUnit', typeOfLoadUnit, rowId);
            }
        });
    }

    function attributeLookup_createElement(value, options) {
        var url = '/rest/attribute/' + options.custom_attr_name;
        var params = {};
        params.custom_attr_name = options.custom_attr_name;
        params.url = url;
        var lookup = createLookupElement('attribute', value, url, options, function () {
            return {
                custom_attr_name: options.custom_attr_name,
                url: url
            };
        });

        return lookup;
    }

    function getFieldValue(field, rowId) {
        return getField(field, rowId).val();
    }

    function getField(field, rowId) {
        // edit inline
        var selector = $('#' + rowId + '_' + field);

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
        $.get('/rest/rest/documentPositions/product/' + Base64.encodeURI(productNumber) + ".html", function (product) {
            updateFieldValue('productName', product.name, _rowID);
        });
    }

    var available_additionalunits = null;

    function updateUnitsInGridByProduct(productNumber, additionalUnitValue) {
        if (!productNumber) {
            return;
        }
        $.get('/rest/rest/documentPositions/units/' + Base64.encodeURI(productNumber) + ".html", function (units) {
            available_additionalunits = units['available_additionalunits'];
            var gridData = $('#grid').jqGrid('getRowData');

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
        });
    }

    var storageLocations = null;

    function updateStorageLocations(productNumber, document) {
        $.get('/rest/rest/documentPositions/storageLocation/' + Base64.encodeURI(productNumber) + "/" + Base64.encodeURI(document) + ".html", function (location) {
            if (location) {
                var gridData = $('#grid').jqGrid('getRowData');

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
        });
    }

    function updateResource(productNumber, conversion, ac, batch, batchId) {
        var params;
        if (hasAdditionalUnit) {
            params = {
                context: getDocumentId(),
                product: productNumber,
                ac: ac,
                conversion: conversion,
                batch: batch,
                batchId: batchId
            }
        } else {
            params = {
                context: getDocumentId(),
                product: productNumber,
                ac: ac,
                conversion: 1,
                batch: batch,
                batchId: batchId
            }

        }
        $.get('/rest/rest/documentPositions/resource.html?' + $.param(params), function (resource) {
            var gridData = $('#grid').jqGrid('getRowData');

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
                        var conversion = getFieldValue('conversion', getRowIdFromElement(t));
                        var batch = getFieldValue('batch', getRowIdFromElement(t));
                        var batchId = getFieldValue('batchId', getRowIdFromElement(t));
                        updateResource(t.val(), conversion, ac, batch, batchId);
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
                    updateFieldValue('lastResource', '0', getRowIdFromElement(t));

                    clearSelect('givenunit', getRowIdFromElement(t));
                }
            }, 500));
        });

        return lookup;
    }

    function batchLookup_createElement(value, options) {
        var lookup = createLookupElement('batch', value, '/rest/rest/documentPositions/batch.html', options, function () {
            return {
                product: getFieldValue('product', getRowIdFromElement($('input', lookup)))
            };
        });

        $('input', lookup).bind('change keydown paste input', function () {
            var t = $(this);
            window.clearTimeout(t.data("timeout"));
            $(this).data("timeout", setTimeout(function () {

            }, 500));
        });

        return lookup;
    }

    function storageLocationLookup_createElement(value, options) {
        var lookup = createLookupElement('storageLocation', value, '/rest/rest/documentPositions/storagelocations.html', options, function () {
            return {
                product: getFieldValue('product', getRowIdFromElement($('input', lookup))),
                location: getDocumentId()
            };
        });

        $('input', lookup).bind('change keydown paste input', function () {
            var t = $(this);
            window.clearTimeout(t.data("timeout"));
            $(this).data("timeout", setTimeout(function () {
                conversionModified = false;
            }, 500));
        });

        return lookup;
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
        quantityValue = undefined;

        // edit inline
        $('#' + rowId + '_quantity').trigger('change');
    }

    function touchManuallyGivenQuantityField(rowId) {
        givenquantityValue = undefined;

        // edit inline
        $('#' + rowId + '_givenquantity').trigger('change');
    }

    function getColModelByIndex(index, c) {
        c = c || $scope.config;
        var col = c.colModel.filter(function (element, i) {
            return element.name === index;
        })[0];
        if (!col) {
            console.error(index);
        }
        return col;
    }

    function getColModelOrPrepareForAttribute(columnProperties, c) {
        c = c || $scope.config;
        var col = c.colModel.filter(function (element, i) {
            return element.index === columnProperties.name;
        })[0];
        if (columnProperties.forAttribute) {
            var attrColModel = {};
            attrColModel.name = columnProperties.name;
            attrColModel.index = "attrs." + columnProperties.name;
            attrColModel.jsonmap = "attrs." + columnProperties.name;
            attrColModel.editable = true;
            if (columnProperties.attributeDataType == '01calculated') {
                attrColModel.edittype = 'custom';
                var editoptions = {};
                editoptions.custom_element = attributeLookup_createElement;
                editoptions.custom_value = lookup_value;
                editoptions.custom_attr_name = columnProperties.name;
                attrColModel.editoptions = editoptions;
            } else if (columnProperties.attributeValueType == '02numeric') {
                attrColModel.formatter = numberFormatter;
                attrColModel.unformat = numberUnformat;
                attrColModel.edittype = 'custom';
                var editoptions = {};
                editoptions.custom_element = attribute_createElement;
                editoptions.custom_value = input_value;
                editoptions.custom_attr_name = columnProperties.name;
                attrColModel.editoptions = editoptions;
            } else {
                var editoptions = {};
                attrColModel.editoptions = editoptions;
            }

            col = attrColModel;
        } else if (!col) {
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
                        var product = getFieldValue('product', rowId);
                        var resource = getFieldValue('resource', rowId);
                        var batch = getFieldValue('batch', rowId);
                        var batchId = getFieldValue('batchId', rowId);
                        if (!resource) {
                            updateResource(product, conversion, ac, batch, batchId);
                        }
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
                    var givenUnitValue = getFieldValue('givenunit', rowId);

                    var newGivenQuantity = null;

                    if (quantities[rowId]) {
                        newGivenQuantity = roundTo(quantityValueNew * conversion);
                    }

                    var isInteger = false;
                    if (available_additionalunits) {
                        var entry = available_additionalunits.filter(function (element, index) {
                            return element.key === givenUnitValue;
                        })[0];
                        if (entry && entry.isinteger) {
                            isInteger = true;
                        }
                    }
                    if (isInteger) {
                        newGivenQuantity = Math.round(newGivenQuantity);
                    } else {
                        newGivenQuantity = roundTo(newGivenQuantity);
                    }
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

    function sellingPrice_createElement(value, options) {
        var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
        $input.val(value);
        $input.attr('readonly', getColModelByIndex('sellingPrice').editoptions.readonly === 'readonly');

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

    function attribute_createElement(value, options) {
        var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
        $input.val(value);
        $input.attr('readonly', getColModelByIndex(options.name).editoptions.readonly === 'readonly');

        var aValue = value;
        var aValueNew;
        $($input).bind('change keydown paste input', function () {
            var t = $(this);

            window.clearTimeout(t.data("timeout"));
            aValueNew = t.val();
            if (aValue !== aValueNew) {
                aValue = aValueNew;

                $(this).data("timeout", setTimeout(function () {
                    gridRunner(function () {
                        parseAndValidateInputNumber(t);
                    });
                }, 500));
            }
        });

        return $input;
    }

    var givenquantityValue;

    function givenquantity_createElement(value, options) {
        var $input = $('<input type="customNumber" id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
        $input.val(value);

        givenquantityValue = value;
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
                        var product = getFieldValue('product', getRowIdFromElement(t));
                        var batch = getFieldValue('batch', getRowIdFromElement(t));
                        var batchId = getFieldValue('batchId', getRowIdFromElement(t));
                        updateResource(product, t.val(), ac, batch, batchId);
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
        refreshForm();
    }

    function prepareViewOnStartEdit() {
        mainController.getComponentByReferenceName("positionsGrid").setComponentChanged(true);
        $("#add_new_row").addClass("disableButton");
        $("#delete_row").addClass("disableButton");
    }

    function prepareViewOnEndEdit() {
        mainController.getComponentByReferenceName("positionsGrid").setComponentChanged(false);
        $("#add_new_row").removeClass("disableButton");
        $("#delete_row").removeClass("disableButton");
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
        var $grid = jQuery('#grid').setGridWidth($("#window\\.positionsGridTab").width() - 23, true);
        if ($grid.is(':visible')) {
            var $flowGridLayout = $('div.flow-grid-layout-item');
            var containerHeight = $flowGridLayout.innerHeight();
            var gridHeight = $('.ui-jqgrid-bdiv', $flowGridLayout).outerHeight();
            var totalGridHeight = $('#gbox_grid', $flowGridLayout).outerHeight();
            var newGridHeightToFillWholeSpace = containerHeight - totalGridHeight + gridHeight;
            $grid.setGridHeight(newGridHeightToFillWholeSpace);
            config.height = newGridHeightToFillWholeSpace;
        }
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
        initdata: {},
        position: "first",
        useDefValues: true,
        useFormatter: true,
        addRowParams: angular.extend({
            extraparam: {}
        }, gridEditOptions)
    };

    var config = {
        url: '../../rest/rest/documentPositions/' + getDocumentId() + '.html',
        regional: ((window.locale == 'de') || (window.locale == 'fr')) ? 'en' : window.locale,
        datatype: "json",
        height: '100%',
        autowidth: true,
        rowNum: 20,
        rowList: [20, 30, 50, 100, 200],
        sortname: 'number',
        toolbar: [true, "top"],
        rownumbers: false,
        altRows: true,
        multiselect: true,
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
                        cancelEditing();
                        prepareViewOnEndEdit();
                        $("#grid").trigger("reloadGrid");
                        viewRefresh();
                    }
                }
            },
            {
                name: 'product',
                index: 'product',
                width: 250,
                editable: true,
                required: true,
                edittype: 'custom',
                editoptions: {
                    custom_element: productsLookup_createElement,
                    custom_value: lookup_value,
                }
            },
            {
                name: 'productName',
                index: 'productName',
                editable: true,
                editoptions: {readonly: 'readonly'},
                searchoptions: {}
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
                }
            },
            {
                name: 'unit',
                index: 'unit',
                editable: true,
                stype: 'select',
                editoptions: {readonly: 'readonly'},
                searchoptions: {}
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
                }
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
                searchoptions: {}
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
                }
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
                }
            },
            {
                name: 'sellingPrice',
                index: 'sellingPrice',
                editable: true,
                required: true,
                edittype: 'custom',
                formatter: numberFormatter,
                unformat: numberUnformat,
                editoptions: {
                    custom_element: sellingPrice_createElement,
                    custom_value: input_value
                }
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
                            var locale = (window.locale == 'cn') ? 'zh-CN' : window.locale;

                            var options = $.datepicker.regional[locale];

                            if (!options) {
                                options = $.datepicker.regional[''];
                            }

                            options.showOn = 'button';
                            options.buttonText = '';
                            options.altField = element;
                            $(element).datepicker(options);
                            $(element).mask("2999-19-39");
                        }
                    }
                }
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
                            var locale = (window.locale == 'cn') ? 'zh-CN' : window.locale;

                            var options = $.datepicker.regional[locale];

                            if (!options) {
                                options = $.datepicker.regional[''];
                            }

                            options.showOn = 'button';
                            options.buttonText = '';
                            options.altField = element;
                            $(element).datepicker(options);
                            $(element).mask("2999-19-39");
                        }
                    }
                }
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
                }
            },
            {
                name: 'resourceNumber',
                index: 'resourceNumber',
                editable: false
            },
            {
                name: 'pickingDate',
                index: 'pickingDate',
                editable: false
            },
            {
                name: 'pickingWorker',
                index: 'pickingWorker',
                editable: false
            },
            {
                name: 'batch',
                index: 'batch',
                editable: true,
                edittype: 'custom',
                editoptions: {
                    custom_element: batchLookup_createElement,
                    custom_value: lookup_value
                }
            },
            {
                name: 'batchId',
                index: 'batchId',
                editable: true,
                hidden: true
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
                }
            },
            {
                name: 'typeOfLoadUnit',
                index: 'typeOfLoadUnit',
                editable: true,
                required: true,
                edittype: 'select',
                stype: 'select',
                editoptions: {},
                searchoptions: {}
            },
            {
                name: 'storageLocation',
                index: 'storageLocation',
                editable: true,
                edittype: 'custom',
                editoptions: {
                    custom_element: storageLocationLookup_createElement,
                    custom_value: lookup_value
                }
            },
            {
                name: 'waste',
                index: 'waste',
                editable: true,
                edittype: 'checkbox',
                formatter: 'checkbox',
                width: 60,
                editoptions: {
                    value: '1:0'
                },
                searchoptions: {
                    sopt: ['eq', 'ne'],
                    value: ': ;1:' + translateMessages('documentGrid.yes') + ';0:' + translateMessages('documentGrid.no')
                },
                stype: 'select'
            },
            {
                name: 'lastResource',
                index: 'lastResource',
                editable: true,
                edittype: 'checkbox',
                formatter: 'checkbox',
                width: 70,
                editoptions: {
                    value: '1:0'
                },
                searchoptions: {
                    sopt: ['eq', 'ne'],
                    value: ': ;1:' + translateMessages('documentGrid.yes') + ';0:' + translateMessages('documentGrid.no')
                },
                stype: 'select'
            }
        ],
        pager: "#jqGridPager",
        gridComplete: function () {
            var grid = $('#grid');
            var rows = grid.jqGrid('getDataIDs');
            if ($scope.config.readOnly) {
                for (i = 0; i < rows.length; i++) {
                    $("tr#" + rows[i] + " div.ui-inline-edit").hide();
                }
            }
            $('#rows-num').text('(' + grid.getGridParam('records') + ')');
        },
        onSelectRow: function (rowid, status) {
        },
        beforeSelectRow: function (rowid, e) {
            var $td = $(e.target).closest("tr.jqgrow>td");
            if ($td.length > 0) {
                var $grid = $(this);
                var i = $.jgrid.getCellIndex($td);
                var cm = $grid.jqGrid('getGridParam', 'colModel');
                if (cm[i].name === 'act') {
                    if (e.target.className === 'ui-icon ui-icon-cancel') {
                        return false;
                    }
                    $grid.jqGrid('resetSelection');
                    return true;
                }
                return (cm[i].name === 'cb');
            }
            return false;
        },
        beforeRequest: function () {
            $.cookie("jqgrid_conf", JSON.stringify({
                rowNum: $(this).getGridParam("rowNum")
            }));
        },
        ajaxRowOptions: {
            contentType: "application/json"
        },
        serializeRowData: function (postdata) {
            delete postdata.oper;
            postdata.attrs = {};
            angular.forEach(columnConfiguration, function (columnInGrid, key) {
                if (columnInGrid.forAttribute) {
                    postdata.attrs[columnInGrid.name] = postdata[columnInGrid.name];
                    delete postdata[columnInGrid.name];
                }
            });

            return validateSerializeData(postdata);
        },
        beforeSubmit: function (postdata, formid) {
            return [false, 'ble'];
        }
    };


    function prepareGridConfig(config) {
        var c = $.cookie("jqgrid_conf");
        if (c) {
            $.extend(config, JSON.parse(c));
        }

        var readOnlyInType = function (outDocument, columnIndex) {
            if (outDocument && (columnIndex === 'expirationDate' || columnIndex === 'productionDate' ||
                columnIndex === 'price' || columnIndex === 'waste' ||
                columnIndex === 'palletNumber' || columnIndex === 'typeOfLoadUnit' || columnIndex === 'storageLocation')) {
                return true;
            }
            if ((columnIndex === 'resource') && (!outDocument)) {
                return true;
            }
            if (columnIndex === 'lastResource') {
                return true;
            }
            if (!outDocument && columnIndex === 'sellingPrice') {
                return true;
            }

            return false;
        };

        $http({
            method: 'GET',
            url: '../../rest/rest/documentPositions/gridConfig/' + config.document_id + '.html'

        }).then(function successCallback(response) {
            columnConfiguration = response.data.columns;
            config.readOnly = response.data.readOnly;
            config.suggestResource = response.data.suggestResource;
            config.outDocument = response.data.outDocument;

            var columns = [getColModelByIndex('id', config), getColModelByIndex('document', config), getColModelByIndex('batchId', config)];
            var colNames = ['ID', 'document', 'batchId'];

            angular.forEach(response.data.columns, function (columnInGrid, key) {
                var gridColModel = getColModelOrPrepareForAttribute(columnInGrid, config);

                if (!columnInGrid.checked) {
                    gridColModel.hidden = true;
                    gridColModel.editrules = gridColModel.editrules || {};
                    gridColModel.editrules.edithidden = true;
                }
                if (gridColModel.editoptions) {
                    delete gridColModel.editoptions.disabled;
                    delete gridColModel.editoptions.readonly;
                }
                if (readOnlyInType(config.outDocument, columnInGrid.name)) {
                    gridColModel.editoptions = gridColModel.editoptions || {};
                    if (gridColModel.edittype === 'select' || gridColModel.edittype === 'checkbox') {
                        gridColModel.editoptions.disabled = 'disabled';
                    } else {
                        gridColModel.editoptions.readonly = 'readonly';
                    }
                }

                if (columnInGrid.forAttribute && config.outDocument) {
                    gridColModel.editoptions.readonly = 'readonly';
                }

                columns.push(gridColModel);
                if (columnInGrid.forAttribute) {
                    colNames.push(columnInGrid.name);
                } else {
                    colNames.push(QCD.translate('documentGrid.gridColumn.' + columnInGrid.name));
                }
            });

            config.colModel = columns;
            config.colNames = colNames;

            $http({
                method: 'GET',
                url: '../../rest/typeOfLoadUnits'
            }).then(function successCallback(response) {
                var selectOptionsTypeOfLoadUnits = [':' + translateMessages('documentGrid.allItem')];
                var selectOptionsTypeOfLoadUnitsEdit = [':' + translateMessages('documentGrid.emptyItem')];
                angular.forEach(response.data, function (value, key) {
                    selectOptionsTypeOfLoadUnits.push(value.key + ':' + value.value);
                    selectOptionsTypeOfLoadUnitsEdit.push(value.key + ':' + value.value);
                });

                getColModelByIndex('typeOfLoadUnit', config).editoptions.value = selectOptionsTypeOfLoadUnitsEdit.join(';');
                getColModelByIndex('typeOfLoadUnit', config).searchoptions.value = selectOptionsTypeOfLoadUnits.join(';');

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

    $scope.deleteRow = function () {
        $("#del_grid").click();
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
        message: '<h2>' + QCD.translate('documentGrid.firstSaveDocument') + '</h2>',
        centerY: false,
        centerX: false,
        css: {
            top: '70px',
            left: ($(window).width() / 2) - 300 + 'px',
        }
    });
}]);
