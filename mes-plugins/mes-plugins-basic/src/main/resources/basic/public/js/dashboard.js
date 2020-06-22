if ($('#chartElement').length) {
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
