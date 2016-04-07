function onClickPostButton (server) {
    $.ajax({
        url: "http://"+server+":8182",
        method: "POST",
        data: {value : prompt()}
    })
    .done(function(data) {
        console.log("POST request --> OK");
        document.getElementById("get-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("POST request --> KO");
    });
}

function periodicGet (server) {
    $.ajax({
        url: "http://"+server+":8182",
        method: "GET"
    })
    .done(function(data) {
        console.log("GET request --> OK");
        document.getElementById("get-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("GET request --> KO");
    })
    .always(function () {
        setTimeout(periodicGet, 500, server);
    });
}

var server = "localhost";
//var server = prompt("server adress");
periodicGet(server);
