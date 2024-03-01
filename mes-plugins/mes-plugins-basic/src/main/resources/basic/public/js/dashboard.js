var QCD = QCD || {};

QCD.dashboardContext = {};

QCD.dashboardContext.items = [];

QCD.dashboardContext.ordersPending = {};
QCD.dashboardContext.ordersInProgress = {};
QCD.dashboardContext.ordersCompleted = {};

QCD.dashboardContext.operationalTasksPending = {};
QCD.dashboardContext.operationalTasksInProgress = {};
QCD.dashboardContext.operationalTasksCompleted = {};

QCD.dashboardContext.getItems = function getItems() {
    return QCD.dashboardContext.items;
}

QCD.dashboardContext.getOrdersPending = function getOrdersPending() {
    return QCD.dashboardContext.ordersPending;
}
QCD.dashboardContext.getOrdersInProgress = function getOrdersInProgress() {
    return QCD.dashboardContext.ordersInProgress;
}
QCD.dashboardContext.getOrdersCompleted = function getOrdersCompleted() {
    return QCD.dashboardContext.ordersCompleted;
}

QCD.dashboardContext.getOperationalTasksPending = function getOperationalTasksPending() {
    return QCD.dashboardContext.operationalTasksPending;
}
QCD.dashboardContext.getOperationalTasksInProgress = function getOperationalTasksInProgress() {
    return QCD.dashboardContext.operationalTasksInProgress;
}
QCD.dashboardContext.getOperationalTasksCompleted = function getOperationalTasksCompleted() {
    return QCD.dashboardContext.operationalTasksCompleted;
}

var messagesController = new QCD.MessagesController();

QCD.dashboard = (function () {
	function init() {
	    initDailyProductionChart();
	    initOrders();
	    initOperationalTasks();

		registerChart();
		registerButtons();
		registerKanban();
	}

	function registerChart() {
        if ($('#dashboardChart').length) {
            Chart.platform.disableCSSInjection = true;

            Chart.plugins.register({
                afterDraw: function (chart) {
                    if (chart.data.datasets[0].data[0] === 0 && chart.data.datasets[0].data[1] === 0 && chart.data.datasets[0].data[2] === 0) {
                        let ctx = chart.chart.ctx;
                        let width = chart.chart.width;
                        let height = chart.chart.height

                        chart.clear();

                        ctx.save();
                        ctx.textAlign = 'center';
                        ctx.textBaseline = 'middle';
                        ctx.font = "16px";
                        ctx.fillText(QCD.translate('basic.dashboard.dailyProductionChart.noData'), width / 2, height / 2);
                        ctx.restore();
                    }
                }
            });
        }
    }

    function initDailyProductionChart() {
        if ($('#dashboardChart').length) {
            $.get("/rest/dailyProductionChart/data",
                function (data) {
                    new Chart('chart', {
                        type: 'pie',
                        data: {
                            datasets: [{
                                data: data,
                                borderWidth: 0,
                                backgroundColor: [
                                    '#C7D1D9',
                                    '#D9AFA0',
                                    '#639AA6'
                                ]
                            }],
                            labels: [
                                QCD.translate('basic.dashboard.dailyProductionChart.pending.label'),
                                QCD.translate('basic.dashboard.dailyProductionChart.inProgress.label'),
                                QCD.translate('basic.dashboard.dailyProductionChart.done.label')
                            ]
                        },
                        options: {
                            title: {
                                display: true,
                                text: QCD.translate('basic.dashboard.dailyProductionChart.header'),
                                fontSize: 16,
                                fontFamily: '"Helvetica Neue"',
                                fontColor: 'black'
                            },
                            legend: {
                                position: 'bottom',
                                labels: {
                                    fontColor: 'black'
                                }
                            }
                        }
                    });

                    setKanbanHeight();
                }
            );
        }
    }

    function registerButtons() {
        if ($("#dashboardButtons").length) {
            $("#dashboardButtons .card").each(function(index, element) {
                $(this).fadeIn((index + 1) * 250);
            });

            $("#dashboardButtons .card").hover(
                function() {
                    $(this).removeClass('bg-secondary').addClass('shadow-sm').addClass('bg-success').addClass('card-hover');
                }, function() {
                    $(this).addClass('bg-secondary').removeClass('shadow-sm').removeClass('bg-success').removeClass('card-hover');
                }
            );
        }
    }

    function registerKanban() {
        if (!$("#dashboardChart").length) {
            setKanbanHeight();
        }

        if ($("#dashboardKanban #ordersPending").length) {
            $.each(QCD.dashboardContext.getOrdersPending(), function (index, order) {
                appendOrder('ordersPending', order);
            });

            $.each(QCD.dashboardContext.getOrdersInProgress(), function (index, order) {
                appendOrder('ordersInProgress', order);
            });

            $.each(QCD.dashboardContext.getOrdersCompleted(), function (index, order) {
                appendOrder('ordersCompleted', order);
            });

            updateDropzones();
        }
        if ($("#dashboardKanban #operationalTasksPending").length) {
            $.each(QCD.dashboardContext.getOperationalTasksPending(), function (index, operationalTask) {
                appendOperationalTask('operationalTasksPending', operationalTask);
            });

            $.each(QCD.dashboardContext.getOperationalTasksInProgress(), function (index, operationalTask) {
                appendOperationalTask('operationalTasksInProgress', operationalTask);
            });

            $.each(QCD.dashboardContext.getOperationalTasksCompleted(), function (index, operationalTask) {
                appendOperationalTask('operationalTasksCompleted', operationalTask);
            });
        }

        $("#dashboardSearch").fadeIn(500);

        $("#dashboardKanban .card.bg-light").each(function (index, element) {
            $(this).fadeIn((index + 1) * 250);
        });

        $("#dashboardKanban .items .card").hover(
            function() {
                $(this).addClass('shadow-sm');
            }, function() {
                $(this).removeClass('shadow-sm');
            }
        );
    }

    function setKanbanHeight() {
        let containerHeight = $(".container").height();
        let dashboardButtonsHeight = $("#dashboardButtons").height();
        let dashboardChartHeight = $("#dashboardChart").height();
        let dashboardSearchHeight = $("#dashboardSearch").height();
        let cardTitleHeight = $(".card-title").height();

        let dashboardPaddingHeight = 20;
        let cardTitlePaddingHeight = 8;
        let cardTitleMarginHeight = 12;
        let cardPaddingHeight = 16;

        let headerHeight = 0;
        let height = 0;
        let marginTop = 0;

        if (dashboardButtonsHeight == undefined) {
            dashboardButtonsHeight = 0;
        } else {
            dashboardButtonsHeight = dashboardButtonsHeight + (dashboardPaddingHeight * 2);
        }
        if (dashboardChartHeight == undefined) {
            dashboardChartHeight = 0;
        } else {
            marginTop = 40;
            dashboardChartHeight = dashboardChartHeight + dashboardPaddingHeight;
        }

        dashboardSearchHeight = dashboardSearchHeight + dashboardPaddingHeight;
        cardTitleHeight = cardTitleHeight + (cardTitlePaddingHeight * 2) + cardTitleMarginHeight;

        headerHeight = (dashboardButtonsHeight > dashboardChartHeight) ? dashboardButtonsHeight : dashboardChartHeight;

        height = containerHeight - headerHeight - dashboardSearchHeight - (cardPaddingHeight * 2) - cardTitleHeight + marginTop;

        $("#dashboardSearch").css("margin-top", -marginTop + "px");
        $("#dashboardKanban .items").css("height",  height + "px");
    }

    function addItem(item) {
        QCD.dashboardContext.items.push(item);
    }

    function appendOrder(ordersType, order) {
        addItem(order);

        $('#' + ordersType).append(
            createOrderDiv(order)
        );
    }

    function prependOrder(ordersType, order) {
        addItem(order);

        $('#' + ordersType).prepend(
            createOrderDiv(order)
        );
    }

    function appendOperationalTask(operationalTasksType, operationalTask) {
        addItem(operationalTask);

        $('#' + operationalTasksType).append(
            createOperationalTaskDiv(operationalTasksType, operationalTask)
        );
    }

    function prependOperationalTask(operationalTasksType, operationalTask) {
        addItem(operationalTask);

        $('#' + operationalTasksType).prepend(
            createOperationalTaskDiv(operationalTasksType, operationalTask)
        );
    }

    function initOrders() {
        if ($('#dashboardKanban #ordersPending').length) {
            getOrdersPending();
            getOrdersInProgress();
            getOrdersCompleted();
        }
    }

    function getOrdersPending() {
        $.ajax({
            url : "/rest/dashboardKanban/ordersPending",
            type : "GET",
            async : false,
            beforeSend : function() {
                //$("#loader").modal('show');
            },
            success : function(data) {
                QCD.dashboardContext.ordersPending = data;
            },
            error : function(data) {
                console.log("error")
            },
            complete : function() {
                //$("#loader").modal('hide');
            }
        });
    }

    function getOrdersInProgress() {
        $.ajax({
            url : "/rest/dashboardKanban/ordersInProgress",
            type : "GET",
            async : false,
            beforeSend : function() {
                //$("#loader").modal('show');
            },
            success : function(data) {
                QCD.dashboardContext.ordersInProgress = data;
            },
            error : function(data) {
                console.log("error")
            },
            complete : function() {
                //$("#loader").modal('hide');
            }
        });
    }

    function getOrdersCompleted() {
        $.ajax({
            url : "/rest/dashboardKanban/ordersCompleted",
            type : "GET",
            async : false,
            beforeSend : function() {
                //$("#loader").modal('show');
            },
            success : function(data) {
                QCD.dashboardContext.ordersCompleted = data;
            },
            error : function(data) {
                console.log("error")
            },
            complete : function() {
                //$("#loader").modal('hide');
            }
        });
    }

    function initOperationalTasks() {
        if ($('#dashboardKanban #operationalTasksPending').length) {
            getOperationalTasksPending();
            getOperationalTasksInProgress();
            getOperationalTasksCompleted();
        }
    }

    function getOperationalTasksPending() {
        $.ajax({
            url : "/rest/dashboardKanban/operationalTasksPending",
            type : "GET",
            async : false,
            beforeSend : function() {
                //$("#loader").modal('show');
            },
            success : function(data) {
                QCD.dashboardContext.operationalTasksPending = data;
            },
            error : function(data) {
                console.log("error")
            },
            complete : function() {
                //$("#loader").modal('hide');
            }
        });
    }

    function getOperationalTasksInProgress() {
        $.ajax({
            url : "/rest/dashboardKanban/operationalTasksInProgress",
            type : "GET",
            async : false,
            beforeSend : function() {
                //$("#loader").modal('show');
            },
            success : function(data) {
                QCD.dashboardContext.operationalTasksInProgress = data;
            },
            error : function(data) {
                console.log("error")
            },
            complete : function() {
                //$("#loader").modal('hide');
            }
        });
    }

    function getOperationalTasksCompleted() {
        $.ajax({
            url : "/rest/dashboardKanban/operationalTasksCompleted",
            type : "GET",
            async : false,
            beforeSend : function() {
                //$("#loader").modal('show');
            },
            success : function(data) {
                QCD.dashboardContext.operationalTasksCompleted = data;
            },
            error : function(data) {
                console.log("error")
            },
            complete : function() {
                //$("#loader").modal('hide');
            }
        });
    }

    function filterKanbanReload() {
        let value = $("#search").val();

        filterKanban(value);
    }

	return {
		init: init,
		initOrders: initOrders,
		appendOrder: appendOrder,
		prependOrder: prependOrder,
		prependOperationalTask: prependOperationalTask,
		filterKanbanReload: filterKanbanReload
	};

})();

$(document).ready(function() {
    QCD.dashboard.init();

    if (QCD.wizardToOpen) {
        if (QCD.wizardToOpen == 'orders') {
            addOrder();
        } else {
            addOperationalTask();
        }
    }
});

const drag = (event) => {
    event.dataTransfer.setData("text/plain", event.target.id);
    event.dataTransfer.setData(event.target.id, '');
}

const drop = (event) => {
    event.preventDefault();

    const data = event.dataTransfer.getData("text/plain");
    const orderId = data.replace('order', '');
    const element = document.querySelector(`#${data}`);

    $.ajax({
        url: "/rest/dashboardKanban/updateOrderState/" + orderId,
        type: "PUT",
        async: false,
        beforeSend: function () {
            // $("#loader").modal('show');
        },
        success: function (response) {
            if (response.message) {
                window.parent.addMessage({
                    type: 'failure',
                    title: QCD.translate('basic.dashboard.orderStateChange.error'),
                    content: response.message
                });
                removeClass(event.target, "droppable");
            } else {
                const doc = new DOMParser().parseFromString(createOrderDiv(response.order), 'text/html');
                try {
                    element.remove();
                    event.target.removeChild(event.target.firstChild);
                    event.target.appendChild(doc.body.firstChild);

                    unwrap(event.target);
                } catch (error) {
                    console.warn("can't move the item to the same place")
                }

                updateDropzones();
            }
        },
        error: function () {
            console.log("error")
            removeClass(event.target, "droppable");
        },
        complete: function () {
            // $("#loader").modal('hide');
        }
    });
}

const allowDrop = (event) => {
    let path = event.composedPath();
    if (hasClass(event.target, "dropzone")
        && (path[1].id === 'ordersInProgress' && document.getElementById(event.dataTransfer.types[1]).parentElement.id === 'ordersPending'
            || path[1].id === 'ordersCompleted' && document.getElementById(event.dataTransfer.types[1]).parentElement.id === 'ordersInProgress'
        )) {
        event.preventDefault();
        addClass(event.target, "droppable");
    }
}

const clearDrop = (event) => {
    removeClass(event.target, "droppable");
}

const updateDropzones = () => {
    $('.dropzone').remove();

    $('<div class="dropzone rounded" ondrop="drop(event)" ondragover="allowDrop(event)" ondragleave="clearDrop(event)"> &nbsp; </div>').insertAfter('.card .draggable');

    $(".items:not(:has(.card.draggable))").append($('<div class="dropzone rounded" ondrop="drop(event)" ondragover="allowDrop(event)" ondragleave="clearDrop(event)"> &nbsp; </div>'));
};

function hasClass(target, className) {
    return new RegExp("(\\s|^)" + className + "(\\s|$)").test(target.className);
}

function addClass(element, className) {
    if (!hasClass(element, className)) {
        element.className += " " + className;
    }
}

function removeClass(element, className) {
    if (hasClass(element, className)) {
        var reg = new RegExp("(\\s|^)" + className + "(\\s|$)");

        element.className = element.className.replace(reg, " ");
    }
}

function unwrap(node) {
    node.replaceWith(...node.childNodes);
}

function goToMenuPosition(position) {
    if (window.parent.goToMenuPosition) {
        window.parent.goToMenuPosition(position);
    } else {
        window.location = "/main.html"
    }
}

function goToPage(url, isPage) {
    url = window.parent.encodeParams(url);
    if (window.parent.goToPage) {
        window.parent.goToPage(url, null, isPage);
    } else {
        window.location = "/main.html"
    }
}

function createOrderDiv(order) {
    var quantityMade = order.doneQuantity ? order.doneQuantity : 0;

    if (QCD.quantityMadeOnTheBasisOfDashboard === '02reportedProduction') {
        quantityMade = order.reportedProductionQuantity ? order.reportedProductionQuantity : 0;
    }

    let doneInPercent = Math.round(quantityMade * 100 / order.plannedQuantity);
    let product = order.productNumber;
    if (order.dashboardShowForProduct === '02name') {
        product = order.productName;
    } else if (order.dashboardShowForProduct === '03both') {
        product = order.productNumber + ', ' + order.productName;
    }

    var orderDiv = '<div class="card draggable" id="order' + order.id + '" draggable="true" ondragstart="drag(event)">' +
                           '<div class="card-header bg-secondary py-2">';

    if (QCD.enableOrdersLinkOnDashboard === 'true') {
        orderDiv = orderDiv + '<a href="#" class="card-title text-white" onclick="goToOrderDetails(' + order.id + ')">' + order.number + '</a>';
    } else {
        orderDiv = orderDiv + '<span class="card-title text-white">' + order.number + '</span>';
    }

    orderDiv = orderDiv + '</div>' +
    '<div class="card-body py-2">' +
    (order.productionLineNumber ? '<span class="float-left"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.productionLineNumber.label") + ':</span> ' + order.productionLineNumber + '</span>' : '') +
    (order.deadline ? '<span class="float-right"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.deadline.label") + ':</span> ' + order.deadline + '</span>' : '') +
    ((order.productionLineNumber || order.deadline) ? '<br/>' : '') +
    ('<span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.product.label") + ':</span> ' + product + '<br/>') +
    ((order.plannedQuantity && order.productUnit) ? '<span class="float-left"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.plannedQuantity.label") + ':</span> ' + order.plannedQuantity + ' ' + order.productUnit + '</span>' : '') +
    ((order.state == "03inProgress" || order.state == "04completed") ? '<span class="float-right"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.doneQuantity.label") + ':</span> ' + quantityMade + ' ' + order.productUnit + '</span>' : '') +
    (order.plannedQuantity ? '<br/>' : '') +
    (order.companyName ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.companyName.label") + ':</span> ' + order.companyName + '<br/>' : '') +
    (order.addressNumber ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.addressNumber.label") + ':</span> ' + order.addressNumber + '<br/>' : '') +
    (order.masterOrderNumber ? '<span class="float-left"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.masterOrderNumber.label") + ':</span> ' + order.masterOrderNumber +
    (order.masterOrderQuantity ? '</span><span class="float-right"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.masterOrderQuantity.label") + ':</span> ' + order.masterOrderQuantity + ' ' + order.productUnit : '') + '</span><br/>' : '') +
    (order.orderCategory ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.orderCategory.label") + ':</span> ' + order.orderCategory + '<br/>' : '') +
    (order.dashboardShowDescription ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.orders.description.label") + ':</span> ' + (order.description ? order.description : '') + '<br/>' : '');

    if (QCD.enableRegistrationTerminalOnDashboard === 'true') {
        orderDiv = orderDiv +((order.state == "03inProgress" && order.typeOfProductionRecording == "02cumulated") ? '<a href="#" class="badge badge-success float-right" onclick="goToProductionTrackingTerminal(' + order.id + ', null, null)">' + QCD.translate("basic.dashboard.orders.showTerminal.label") + '</a>' : '');
    }
    if (QCD.enablePrintLabelOnDashboard === 'true') {
        orderDiv = orderDiv + ((order.state == "03inProgress") ? '<span><a href="#" style="margin-right:5px;" class="badge badge-success float-right" onclick="printLabel(' + order.id + ')">' + QCD.translate("basic.dashboard.orders.printLabel.label") + '</a></span>' : '');
    }

    orderDiv = orderDiv +'</div>' +
    '<div class="card-footer">' + '<div class="progress">' + '<div class="progress-bar progress-bar-striped bg-info" role="progressbar" style="width: ' + doneInPercent + '%;" aria-valuenow="' + doneInPercent + '" aria-valuemin="0" aria-valuemax="100">' + doneInPercent + '%</div>' + '</div>' + '</div>' +
    '</div>';

    return orderDiv;
}

function createOperationalTaskDiv(operationalTasksType, operationalTask) {
    let doneInPercent = Math.round(operationalTask.usedQuantity * 100 / operationalTask.plannedQuantity);

    operationalTask.usedQuantity = operationalTask.usedQuantity ? operationalTask.usedQuantity : 0;

    let orderProduct = operationalTask.orderProductNumber;
    if (operationalTask.dashboardShowForProduct === '02name') {
        orderProduct = operationalTask.orderProductName;
    } else if (operationalTask.dashboardShowForProduct === '03both') {
        orderProduct = operationalTask.orderProductNumber + ', ' + operationalTask.orderProductName;
    }

    let product = operationalTask.productNumber;
    if (operationalTask.dashboardShowForProduct === '02name') {
        product = operationalTask.productName;
    } else if (operationalTask.dashboardShowForProduct === '03both' && operationalTask.productNumber && operationalTask.productName) {
        product = operationalTask.productNumber + ', ' + operationalTask.productName;
    }

    var opTaskDiv = '<div class="card" id="operationalTask' + operationalTask.id + '">' +
                                '<div class="card-header bg-secondary py-2">';
    if (QCD.enableOrdersLinkOnDashboard === 'true') {
        opTaskDiv = opTaskDiv + '<a href="#" class="card-title text-white" onclick="goToOperationalTaskDetails(' + operationalTask.id + ')">' + operationalTask.number + '</a>';
    } else {
         opTaskDiv = opTaskDiv + '<span class="card-title text-white">' + operationalTask.number + '</span>';
    }

    opTaskDiv = opTaskDiv +  '</div>' +
    '<div class="card-body py-2">' +
    '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.name.label") + ':</span> ' + operationalTask.name + '<br/>';

    if (QCD.enableOrdersLinkOnDashboard === 'true') {
        opTaskDiv = opTaskDiv + ((operationalTask.type == "02executionOperationInOrder" && operationalTask.orderNumber) ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.orderNumber.label") + ':</span> <a href="#" onclick="goToOrderDetails(' + operationalTask.orderId + ')">' + operationalTask.orderNumber + '</a><br/>' : '');
    } else {
       opTaskDiv = opTaskDiv + ((operationalTask.type == "02executionOperationInOrder" && operationalTask.orderNumber) ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.orderNumber.label") + ':</span> <span>' + operationalTask.orderNumber + '<span><br/>' : '');
    }

    opTaskDiv = opTaskDiv +  (operationalTask.workstationNumber ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.workstationNumber.label") + ':</span> ' + operationalTask.workstationNumber + '<br/>' : '') +
    (operationalTask.type == "02executionOperationInOrder" ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.orderProduct.label") + ':</span> ' + orderProduct + '<br/>' : '') +
    ((operationalTask.type == "02executionOperationInOrder" && product) ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.product.label") + ':</span> ' + product + '<br/>' : '') +
    ((operationalTask.type == "02executionOperationInOrder" && operationalTask.plannedQuantity && operationalTask.productUnit) ? '<span class="float-left"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.plannedQuantity.label") + ':</span> ' + operationalTask.plannedQuantity + ' ' + operationalTask.productUnit + '</span>' : '') +
    ((operationalTask.type == "02executionOperationInOrder" && operationalTasksType != 'operationalTasksPending') ? '<span class="float-right"><span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.usedQuantity.label") + ':</span> ' + operationalTask.usedQuantity + ' ' + operationalTask.productUnit + '</span>' : '') +
    ((operationalTask.type == "02executionOperationInOrder" && operationalTask.plannedQuantity) ? '<br/>' : '') +
    (operationalTask.staffName ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.staffName.label") + ':</span> ' + operationalTask.staffName + '<br/>' : '') +
    (operationalTask.dashboardShowDescription ? '<span class="font-weight-bold">' + QCD.translate("basic.dashboard.operationalTasks.description.label") + ':</span> ' + (operationalTask.description ? operationalTask.description : '') + '<br/>' : '');

    if (QCD.enableRegistrationTerminalOnDashboard === 'true') {
        opTaskDiv = opTaskDiv + ((operationalTask.type == "02executionOperationInOrder" && operationalTask.state == "02started") ? '<a href="#" class="badge badge-success float-right" onclick="goToProductionTrackingTerminal(null, ' + operationalTask.id + ', ' + (operationalTask.workstationNumber ? '\'' + operationalTask.workstationNumber + '\'' : null) + ')">' + QCD.translate("basic.dashboard.operationalTasks.showTerminal.label") + '</a>' : '');
    }

    opTaskDiv = opTaskDiv + '</div>' +
    (operationalTask.type == "02executionOperationInOrder" ? '<div class="card-footer">' + '<div class="progress">' + '<div class="progress-bar progress-bar-striped bg-info" role="progressbar" style="width: ' + doneInPercent + '%;" aria-valuenow="' + doneInPercent + '" aria-valuemin="0" aria-valuemax="100">' + doneInPercent + '%</div>' + '</div>' + '</div>' : '') +
    '</div><div class="empty"> &nbsp; </div>';

    return opTaskDiv;
}

function filterKanban(value) {
    let keys = [ "masterOrderNumber", "orderNumber", "number", "description", "orderCategory", "productionLineNumber", "staffName", "workstationNumber", "productNumber", "orderProductNumber", "companyName" ];

    let results = QCD.dashboardContext.getItems().filter(item => Object.keys(item).some(key => keys.includes(key) && (item[key] != null) && item[key].toString().toLowerCase().includes(value.toLowerCase())));

    if (value == '') {
        $(".items .card").show();
        $(".items .empty").show();

        if ($("#dashboardKanban #ordersPending").length) {
            updateDropzones();
        }
    } else {
        $(".items .card").hide();
        $(".items .empty").hide();

        $(".dropzone").remove();

        $.each(results, function (index, item) {
            $("#order" + item.id).show();
            $('<div class="dropzone rounded" ondrop="drop(event)" ondragover="allowDrop(event)" ondragleave="clearDrop(event)"> &nbsp; </div>').insertAfter("#order" + item.id);
            $("#operationalTask" + item.id).show();
        });

        if ($("#dashboardKanban #ordersPending").length) {
            $(".items:not(:has(.card.draggable))").append($('<div class="dropzone rounded" ondrop="drop(event)" ondragover="allowDrop(event)" ondragleave="clearDrop(event)"> &nbsp; </div>'));
        }
    }
}

function addOrder() {
    QCD.orderDefinitionWizard.init();
}

function addOperationalTask() {
    QCD.operationalTasksDefinitionWizard.init();
}

function goToOrderDetails(id) {
    goToPage("orders/orderDetails.html?context=" + JSON.stringify({
        "form.id": id,
        "form.undefined": null
    }), true);
}

function goToOperationalTaskDetails(id) {
    goToPage("orders/operationalTaskDetails.html?context=" + JSON.stringify({
        "form.id": id,
        "form.undefined": null
    }), true);
}

function goToProductionTrackingTerminal(orderId, operationalTaskId, workstationNumber) {
    let url = "/productionRegistrationTerminal.html";
    if (orderId) {
        url += "?orderId=" + orderId;
    } else if (operationalTaskId) {
        url += "?operationalTaskId=" + operationalTaskId;
        if (workstationNumber) {
            url += '&workstationNumber=' + workstationNumber;
        }
    }
    goToPage(url, false);
}

function printLabel(orderId) {
    let url = "orders/ordersLabelReport.pdf?id=" + orderId;
    window.open(url, '_blank');
}

function showMessage(type, title, content, autoClose) {
    messagesController.addMessage({
        type : type,
        title : title,
        content : content,
        autoClose : autoClose,
        extraLarge : false
    });
}

function logoutIfSessionExpired(data) {
	if ($.trim(data) == "sessionExpired" || $.trim(data).substring(0, 20) == "<![CDATA[ERROR PAGE:") {
		window.location = "/login.html?timeout=true";
	}
}
