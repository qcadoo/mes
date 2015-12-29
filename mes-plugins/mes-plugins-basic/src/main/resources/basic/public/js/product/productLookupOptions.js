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

                    var positionsHeader = QCD.translate('qcadooView.gridHeader.products');
                    var cancelHeader = QCD.translate('qcadooView.gridHeader.cancel');
                    var cancelButton = '<div id="cancel_button" class="headerActionButton" onclick="return cancelGrid();"> <a href="#"><span>' +
                            '<div id="cancel_icon"></div>' +
                            '<div class="hasIcon">' + cancelHeader + '</div></div>';

                    var gridTitle = '<div class="gridTitle">' + positionsHeader + '</div>';

                    $('#t_grid').append(gridTitle);
                    $('#t_grid').append(cancelButton);

                    $(table).jqGrid('filterToolbar');
                }
            });
        }
    }
});

function cancelGrid() {
    parent.frames[0].onSelectLookupRow(null);
}

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
            url: '',
            datatype: "json",
            height: '100%',
            autowidth: true,
            rowNum: 150,
            sortname: 'id',
            toolbar: [true, "top"],
            errorTextFormat: function (response) {
                return translateMessages(JSON.parse(response.responseText).message);
            },
            colNames: [],
            colModel: [],
            pager: "#jqGridPager",
            gridComplete: function () {
            },
            onSelectRow: function (id) {
                var row =  jQuery('#grid').jqGrid ('getRowData', id);
                parent.frames[0].onSelectLookupRow(row);
            },
            ajaxRowOptions: {
                contentType: "application/json"
            }
        };


        function prepareGridConfig(config) {

            $http({
                method: 'GET',
                url: '/rest/productGridConfigForLookup'

            }).then(function successCallback(response) {
                console.log(response.data);

                config = angular.merge(config, response.data);
                
                $scope.config = config;
            });

        }

        prepareGridConfig(config);

    }]);
