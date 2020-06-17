Chart.platform.disableCSSInjection = true;
$.get("/rest/dailyProductionChart/data",
    function (data) {
        new Chart('chart', {
            type: 'pie',
            data: {
                datasets: [{
                    data: data,
                    backgroundColor: [
                        'chocolate',
                        'orange',
                        'forestgreen'
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
                    fontColor: 'white'
                },
                legend: {
                    position: 'bottom',
                    labels: {
                        fontColor: 'white'
                    }
                }
            }
        });
    }
);
