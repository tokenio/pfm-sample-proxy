function initiate() {
    // prevent multiple clicking
    unbindClick();

    var XHR = new XMLHttpRequest();

    // Set up our request
    XHR.open('POST', 'http://localhost:3000/request-access', true);

    XHR.setRequestHeader("Content-Type", "application/json; charset=utf-8");

    // Define what happens on successful data submission
    XHR.addEventListener("load", function (event) {
        window.location.assign(event.target.responseURL);
    });

    // Send the data; HTTP headers are set automatically
    XHR.send();
}

function bindClick() {
    // Add click listener
    el.addEventListener('click', initiate);
}

function unbindClick() {
    // Remove click listener
    el.removeEventListener('click', initiate);
}

var el = document.getElementById("tokenConnectBtn");
bindClick();
