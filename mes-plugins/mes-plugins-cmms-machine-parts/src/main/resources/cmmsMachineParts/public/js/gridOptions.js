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

                    var positionsHeader = QCD.translate('actionsGrid.gridHeader.actions');
                    var newHeader = QCD.translate('actionsGrid.gridHeader.new');
                    var addNewRowButton = '<div id="add_new_row" class="headerActionButton headerButtonEnabled ' + (newValue.readOnly ? 'disabled-button"' : '" onclick="return addNewRow();"') + '> <a href="#"><span>' +
                            '<div class="icon" id="add_new_icon"></div>' +
                            '<div class="hasIcon">' + newHeader + '</div></div>';

                    var gridTitle = '<div class="gridTitle">' + positionsHeader + ' <span id="rows-num">(0)</span></div>';

                    $('#t_grid').append('<div class="t_grid__container"></div>');
                    $('#t_grid .t_grid__container').append(gridTitle);
                    $('#t_grid .t_grid__container').append(addNewRowButton);

                    $(table).jqGrid('filterToolbar');
                    mainController.getComponentByReferenceName("actionsGrid").setComponentChanged(false);

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
                                        {edit: false, add: false, del: true, search: false, refresh: false, view: false, position: "left", cloneToTop: false},
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
                                                params.url = '../../rest/rest/actions/' + postdata.grid_id + ".html";
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
                                                        params.url = '../../rest/rest/actions.html';
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
                                                                params.url = '../../rest/rest/actions/' + encodeURIComponent(postdata) + ".html";
                                                            },
                                                            errorTextFormat: function (response) {
                                                                translateAndShowMessages(response)
                                                                return null;
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
    QCD.components.elements.utils.LoadingIndicator.blockElement(parent.$('body'));
    var grid = $("#grid");
    var ids = grid.jqGrid('getDataIDs');

    for (var i = 0; i < ids.length; i++) {
        grid.saveRow(ids[i]);
    }
    QCD.components.elements.utils.LoadingIndicator.unblockElement(parent.$('body'));
}

function viewRefresh() {
    angular.element($("#GridController")).scope().cancelEditing();
}

function plannedEventIdChanged(id) {
    saveAllRows();
    angular.element($("#GridController")).scope().plannedEventIdChanged(id);
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
    var selector = $('#' + rowId + '_' + field);
    var element = $(selector);
    if (element.length && element[0].tagName.toLowerCase() === 'span') {
        element = $('input', element);
    }

    return element.val(value);
}

function clearSelect(field,rowId)
{
        var  selector = $('#' + rowId + '_' + field);
    selector = $('#' + rowId + '_' + field);
    $(selector).empty();
   $(selector).val([]);
}

function onSelectLookupRow(row, recordName) {
    if (row) {
        var code = row.code || row.number;

        var rowId = jQuery('#grid').jqGrid('getGridParam', 'selrow');
        var field = updateFieldValue(recordName, code, rowId);
        field.trigger('change');
    }

    mainController.closeThisModalWindow();
}

var messagesController = new QCD.MessagesController();

myApp.controller('GridController', ['$scope', '$window', '$http', function ($scope, $window, $http) {
        var _this = this;
        var lastSel;

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

        function getPlannedEventId() {
            if (context) {
                var contextObject = JSON.parse(context);
                if (contextObject && contextObject['window.mainTab.form.id']) {
                    return contextObject['window.mainTab.form.id'];
                }
            }

            var config = angular.element($("#GridController")).scope().config;

            return config ? config.plannedevent_id : 0;
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
                                    code: QCD.translate('actionsGrid.autocomplete.noResults')
                                }]);
                        } else if (data.entities.length === 0) {
                            autoCompleteResult = false;
                            response([{
                                    id: 0,
                                    code: QCD.translate('actionsGrid.autocomplete.tooManyResults') + ' (' + data.numberOfResults + ')'
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

        function getFieldValue(field, rowId) {
            return getField(field, rowId).val();
        }

        function getField(field, rowId) {
            var selector = $('#' + rowId + '_' + field);

            var element = $(selector);
            if (element.length && element[0].tagName.toLowerCase() === 'span') {
                element = $('input', element);
            }

            return element;
        }

        function actionLookup_createElement(value, options) {
            var lookup = createLookupElement('action', value, '../../rest/rest/actions/actions.html', options, function () {
                return  {
                    context: getPlannedEventId()
                };
            });
            return lookup;
        }

        function responsibleWorkerLookup_createElement(value, options) {
            var lookup = createLookupElement('responsibleWorker', value, '../../rest/rest/actions/workers.html', options);

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


        function description_createElement(value, options) {
            var $input = $('<input id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            return $input;
        }
        function reason_createElement(value, options) {
            var $input = $('<input id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '" />');
            $input.val(value);

            return $input;
        }

        function state_createElement(value, options) {
            var $select = $('<select id="' + options.id + '" name="' + options.name + '" rowId="' + options.rowId + '">');
            return $select;
        }

        function state_value(elem, operation, value) {
            if (operation === 'get') {
                return $(elem).val();

            } else if (operation === 'set') {
                return $('select', elem).val(value);
            }
        }

        function errorfunc(rowID, response) {
            var message = JSON.parse(response.responseText).message;
            message = translateMessages(message);
            showMessage('failure', QCD.translate('actionsGrid.notification.failure'), message);
            QCD.components.elements.utils.LoadingIndicator.unblockElement(parent.$('body'));
            
            return true;
        }

        function successfunc(rowID, response) {
            prepareViewOnEndEdit();
            showMessage('success', QCD.translate('actionsGrid.notification.success'), QCD.translate('actionsGrid.message.saveMessage'));
            return true;
        }

        function errorCallback(response) {
            showMessage('failure', QCD.translate('actionsGrid.notification.failure'), response.data.message);
        }

        function aftersavefunc() {
            $("#grid").trigger("reloadGrid");
            QCD.components.elements.utils.LoadingIndicator.unblockElement(parent.$('body'));
        }

        function prepareViewOnStartEdit() {
            mainController.getComponentByReferenceName("actionsGrid").setComponentChanged(true);
            $("#add_new_row").addClass("disableButton");
            //$("#add_grid").hide();
            //$("#edit_grid").hide();
            //$("#del_grid").hide();
        }

        function prepareViewOnEndEdit() {
            mainController.getComponentByReferenceName("actionsGrid").setComponentChanged(false);
            $("#add_new_row").removeClass("disableButton");
            //$("#add_grid").show();
            //$("#edit_grid").show();
            //$("#del_grid").show();
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
            jQuery('#grid').setGridWidth($("#window\\.actionsTab").width() - 23, true);
        };
        $("#window\\.actionsTab").resize($scope.resize);

        var gridEditOptions = {
            keys: true,
            url: '../../rest/rest/actions.html',
            mtype: 'PUT',
            errorfunc: errorfunc,
            successfunc: successfunc,
            aftersavefunc: aftersavefunc
        };

        var gridAddOptions = {
            rowID: "0",
            url: '../../rest/rest/actions.html',
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
            url: '../../rest/rest/actions/' + getPlannedEventId() + '.html',
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
                    name: 'plannedEvent',
                    index: 'plannedEvent',
                    hidden: true,
                    editable: true,
                    editoptions: {
                        defaultValue: getPlannedEventId()
                    }

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
                        url: '../../rest/rest/actions/' + 1 + '.html',
                        delbutton: false,
                        onEdit: function (id) {
                            if (typeof (lastSel) !== "undefined" && id !== lastSel) {
                                cancelEditing(id);
                            }
                            prepareViewOnStartEdit();
                            gridEditOptions.url = '../../rest/rest/actions/' + id + '.html';
                            lastSel = id;
                        },
                        afterRestore: function () {
                            prepareViewOnEndEdit();
                        }
                    }
                },
                {
                    name: 'action',
                    index: 'action',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: actionLookup_createElement,
                        custom_value: lookup_value,
                    },
                    formoptions: {
                        rowpos: 2,
                        colpos: 1
                    }
                },
                {
                    name: 'responsibleWorker',
                    index: 'responsibleWorker',
                    editable: true,
                    required: true,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: responsibleWorkerLookup_createElement,
                        custom_value: lookup_value
                    },
                    formoptions: {
                        rowpos: 3,
                        colpos: 1
                    },
                },
                {
                    name: 'description',
                    index: 'description',
                    editable: true,
                    required: false,
                    edittype: 'custom',
                    editoptions: {
                        custom_element: description_createElement,
                        custom_value: input_value,
                    },
                    formoptions: {
                        rowpos: 4,
                        colpos: 1
                    },
                },
                {
                    name: 'state',
                    index: 'state',
                    editable: true,
                    edittype: 'select',
                    width: 55,
                    stype: 'select',
                    editoptions: {},
                    searchoptions: {},
                    formoptions: {
                        rowpos: 5,
                        colpos: 1
                    },
                },
                {
                    name: 'reason',
                    index: 'reason',
                    editable: true,
                    required: false,
                    edittype: 'custom',
                    width: 220,
                    editoptions: {
                        custom_element: reason_createElement,
                        custom_value: input_value,
                    },
                    formoptions: {
                        rowpos: 6,
                        colpos: 1
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

            $http({
                method: 'GET',
                url: '../../rest/rest/actions/gridConfig/' + config.plannedevent_id + '.html'

            }).then(function successCallback(response) {
                config.readOnly = response.data.readOnly;

                $http({
                    method: 'GET',
                    url: '../../rest/rest/actions/states.html'

                }).then(function successCallback(response) {
                    var selectOptionsStates = [':' + translateMessages('actionsGrid.allItem')];
                    var selectOptionsStatesEdit = [':' + translateMessages('actionsGrid.emptyItem')];
                    angular.forEach(response.data, function (value, key) {
                        selectOptionsStates.push(value.key + ':' + value.value);
                        selectOptionsStatesEdit.push(value.key + ':' + value.value);
                    });

                    getColModelByIndex('state', config).editoptions.value = selectOptionsStatesEdit.join(';');
                    getColModelByIndex('state', config).searchoptions.value = selectOptionsStates.join(';');


                    var colNames = ['ID', 'plannedEvent'];
                    colNames.push(QCD.translate('actionsGrid.gridColumn.act'));
                    colNames.push(QCD.translate('actionsGrid.gridColumn.action'));
                    colNames.push(QCD.translate('actionsGrid.gridColumn.responsibleWorker'));
                    colNames.push(QCD.translate('actionsGrid.gridColumn.description'));
                    colNames.push(QCD.translate('actionsGrid.gridColumn.state'));
                    colNames.push(QCD.translate('actionsGrid.gridColumn.reason'));

                    config.colNames = colNames;
                    var newConfig = {};
                    newConfig = angular.merge(newConfig, config);
                    $scope.config = newConfig;
                    $('#gridWrapper').unblock();
                }, errorCallback)
            }, errorCallback);

            return config;
        }

        $scope.plannedEventIdChanged = function (id) {
            config.url = '../../rest/rest/actions/' + id + '.html';
            config.plannedevent_id = id;

            config.colModel.filter(function (element, index) {
                return element.index === 'plannedEvent';
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
                QCD.components.elements.utils.LoadingIndicator.blockElement(parent.$('body'));
                
                if (rowId === '0') {
                    option.url = '../../rest/rest/actions.html';
                    option.errorfunc = errorfunc;
                    option.successfunc = successfunc;
                    option.aftersavefunc = aftersavefunc;
                } else {
                    option.url = '../../rest/rest/actions/' + rowId + '.html';
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
            message: '<h2>'+QCD.translate('actionsGrid.firstSaveDocument')+'</h2>',
            centerY: false,
            centerX: false,
            css: {
                top: '70px',
                left: ($(window).width() / 2) - 300 + 'px',
            }
        });

    }]);
