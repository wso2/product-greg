function drawCanvas() {

    $('body').noisy({
        'intensity': 1,
        'size': '300',
        'opacity': 0.127,
        'fallback': '',
        'monochrome': false
    }).css('background-color', '#fff39d');

    $('#graphFrame').noisy({
        'intensity': 1,
        'size': '300',
        'opacity': 0.127,
        'fallback': '',
        'monochrome': false
    }).css('background-color', '#ddd39d');
}

