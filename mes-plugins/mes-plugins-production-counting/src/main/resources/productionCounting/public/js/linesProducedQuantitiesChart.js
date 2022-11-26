var QCD = QCD || {};
var chart=null;

QCD.linesProducedQuantitiesChart = (function() {

    function generate() {
        let params = {
            dateFrom: document.getElementById("window.mainTab.form.gridLayout.dateFrom_input").value,
            dateTo: document.getElementById("window.mainTab.form.gridLayout.dateTo_input").value
        };

        QCD.components.elements.utils.LoadingIndicator.blockElement($('body'));

        $.get("/rest/lProducedQuantitiesChart/validate", params, function(message) {
            if (message) {
                QCD.components.elements.utils.LoadingIndicator.unblockElement($('body'));

                new QCD.MessagesController().addMessage({
                    type: 'failure',
                    title: QCD.translate('qcadooView.notification.failure'),
                    content: QCD.translate(message),
                    autoClose: false,
                    extraLarge: false
                });
            } else {
                if(chart != null){
                    chart.destroy();
                }
                mainController.getComponentByReferenceName('window').setActiveTab('chartTab');
                $('#linesProducedQuantitiesChart').height($('#window_windowContent').height() - 50);

                $.get("/rest/lProducedQuantitiesChart/data", params, function(data) {
                    const bgColor = {
                        id: 'bgColor',
                        beforeDraw: (chart) => {
                            const {ctx, width, height} = chart;
                            ctx.fillStyle = 'white';
                            ctx.fillRect(0, 0, width, height);
                            ctx.restore();
                        }
                    }

                    const colors = ['#ADF7B6','#b499cb','#A0CED9','#cb99c9','#ffb347'];

                    const datasets = [];
                    let factoryDataset;
                    for (let key in data.datasets) {
                        if(key === QCD.translate('productionCounting.linesProducedQuantitiesChart.chart.factory.label')){
                            factoryDataset = {
                                label: key,
                                data: data.datasets[key],
                                backgroundColor: '#FCF5C7'
                            }
                        } else {
                            datasets.push({
                                label: key,
                                data: data.datasets[key],
                                backgroundColor: colors[datasets.length % 5]
                            });
                        }
                    }

                    datasets.sort((a, b) =>
                        {   if (a.label < b.label) {
                            return -1;
                        }
                        if (a.label > b.label) {
                            return 1;
                        }

                        return 0;
                    });

                    datasets.unshift(factoryDataset);

                    chart = new Chart('chart', {
                        type: 'bar',
                        data: {
                            datasets: datasets,
                            labels: data.labels
                        },
                        options: {
                            maintainAspectRatio: false,
                            scales: {
                                yAxes: [{
                                    ticks: {
                                        beginAtZero: true
                                    }
                                }]
                            },
                            title: {
                                display: true,
                                text: QCD.translate('productionCounting.linesProducedQuantitiesChart.chart.title'),
                                fontSize: 16,
                                fontFamily: '"Helvetica Neue"',
                                fontColor: 'black'
                            },
                            legend: {
                                align: 'start',
                                labels: {
                                    fontColor: 'black'
                                }
                            }
                        },
                        plugins: [bgColor]
                    });
                    QCD.components.elements.utils.LoadingIndicator.unblockElement($('body'));
                }, 'json');
            }
        }, 'text');
    }

    function exportToPdf() {
        const pdf = new jsPDF('l');
        if(chart != null){
            const chartEl = document.getElementById('chart');
            const image = chartEl.toDataURL('image/png', 1.0);
            pdf.addImage(image, 'PNG', 10, 10, 280, 150);
        }
        pdf.save(QCD.translate('productionCounting.linesProducedQuantitiesChart.chart.fileName') + '.pdf');
    }

    return {
        generate: generate,
        exportToPdf: exportToPdf
    }

})();
