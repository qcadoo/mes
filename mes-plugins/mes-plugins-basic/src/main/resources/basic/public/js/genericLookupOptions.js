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

                    var positionsHeader = QCD.translate('documentGrid.gridHeader.' + scope.$parent.recordName);
                    var cancelHeader = QCD.translate('documentGrid.gridHeader.cancel');
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
        $scope.recordName = '';
        var _this = this;
        var messagesController = new QCD.MessagesController();

        $scope.init = function (recordName) {
            $scope.recordName = recordName;
            prepareGridConfig();
        }

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

        function prepareGridConfig() {
            var config = {
                url: '/rest/' + $scope.recordName + '/records'+document.location.search,
                datatype: "json",
                height: '100%',
                autowidth: true,
                rowNum: 20,
                rowList: [20, 30, 50, 100, 200],
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
                    var row = jQuery('#grid').jqGrid('getRowData', id);
                    parent.frames[0].onSelectLookupRow(row, $scope.recordName);
                },
                ajaxRowOptions: {
                    contentType: "application/json"
                }
            };

            $http({
                method: 'GET',
                url: '/rest/' + $scope.recordName + '/config'

            }).then(function successCallback(response) {
                config = angular.merge(config, response.data);

                angular.forEach(config.colNames, function (value, key) {
                    config.colNames[key] = getSpecificTranslationOrDefault(value, $scope.recordName);
                });

                angular.forEach(config.colModel, function (value, key) {
                    if (config.colModel[key].formatter && typeof config.colModel[key].formatter === 'string') {
                        config.colModel[key].formatter = eval('(' + config.colModel[key].formatter + ')');
                    }

                });

                $scope.config = config;
            });

        }
        
        function getSpecificTranslationOrDefault(value, recordName) {
        	var translation = QCD.translate('documentGrid.gridColumn.' + recordName + '.' + value);
        	if(translation.startsWith('[') && translation.endsWith(']')) {
        		translation = QCD.translate('documentGrid.gridColumn.' + value);
        	}
        	return translation;
        }

    }]);
