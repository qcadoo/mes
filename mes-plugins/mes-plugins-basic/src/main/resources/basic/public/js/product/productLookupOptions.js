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
                    var gridTitle = '<div class="gridTitle">' + positionsHeader + '</div>';

                    $('#t_grid').append(gridTitle);

                    $(table).jqGrid('filterToolbar');
                }
            });
        }
    }
});

myApp.controller('GridController', ['$scope', '$window', '$http', function ($scope, $window, $http) {
        var _this = this;
        var messagesController = new QCD.MessagesController();

        function showMessage(message) {
            messagesController.addMessage(message);
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

        $scope.resize = function () {
            console.log('resize');
            jQuery('#grid').setGridWidth($("#window\\.positionsGridTab").width() - 25, true);
            jQuery('#grid').setGridHeight($("#window\\.positionsGridTab").height() - 150);
        }
        $("#window\\.positionsGridTab").resize($scope.resize);

        var config = {
            url: '/rest/productsForLookup',
            datatype: "json",
            height: '100%',
            autowidth: true,
            rowNum: 150,
            sortname: 'id',
            toolbar: [true, "top"],
            errorTextFormat: function (response) {
                return translateMessages(JSON.parse(response.responseText).message);
            },
            colNames: ['ID', 'number', 'name'/*, 'globalTypeOfMaterial', 'family'*/],
            colModel: [
                {
                    name: 'id',
                    index: 'id',
                    key: true,
                    hidden: true
                },
                {
                    name: 'number',
                    index: 'number',
                    editable: false,
                    editoptions: {readonly: 'readonly'}
                },
                {
                    name: 'name',
                    index: 'name',
                    editable: false,
                    editoptions: {readonly: 'readonly'}
                },
//                {
//                    name: 'globalTypeOfMaterial',
//                    index: 'globalTypeOfMaterial',
//                    editable: false,
//                    editoptions: {readonly: 'readonly'}
//                },
//                {
//                    name: 'family',
//                    index: 'family',
//                    editable: false,
//                    editoptions: {readonly: 'readonly'}
//                }
            ],
            pager: "#jqGridPager",
            gridComplete: function () {
            },
            onSelectRow: function (id) {
                console.log('selected row' + id)
            },
            ajaxRowOptions: {
                contentType: "application/json"
            }
        };
        
        $scope.config = config;
    }]);
