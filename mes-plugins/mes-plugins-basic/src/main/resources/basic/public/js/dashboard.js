Chart.platform.disableCSSInjection = true;
new Chart('chart', {
    type: 'pie',
    data: {
        datasets: [{
            data: [15, 15, 6],
            backgroundColor: [
                'chocolate',
                'orange',
                'forestgreen'
            ]
        }],
        labels: ['Oczekujące', 'Realizowane', 'Gotowe']
    },
    options: {
        title: {
            display: true,
            text: 'Dzienna produkcja'
        },
        legend: {
            position: 'bottom'
        }
    }
});