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

if ($('#buttonsElement').length) {
    function goToMenuPosition(position) {
        if (window.parent.goToMenuPosition) {
            window.parent.goToMenuPosition(position);
        } else {
            window.location = "/main.html"
        }
    }

    $(document).ready(function() {
        $(".card").each( function( index, element ){
            $(this).fadeIn((index + 1) * 250);
        });
        $(".card").hover(
            function() {
                $(this).addClass('shadow').removeClass('bg-warning').addClass('bg-secondary');
            }, function() {
                $(this).removeClass('shadow').addClass('bg-warning').removeClass('bg-secondary');
            }
        );
    });
}
