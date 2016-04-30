var server = "http://localhost:8182/";

function onClickGetButton () {
    var path = document.getElementsByName("GetPath")[0].value;
    
    $.ajax({
        url: server+path,
        method: "GET"
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("get-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}


function onClickPutPropButton () {
    var path = document.getElementsByName("PutPath")[0].value;
    var properties = document.getElementsByName("PutProperties")[0].value;
    
    $.ajax({
        url: server+path,
        method: "PUT",
        dataType: "text",
        contentType: "application/json",
        data: {"properties": properties}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("put-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}

function onClickPutButton () {
    var path = document.getElementsByName("PutPath")[0].value;
    var properties = document.getElementsByName("PutProperties")[0].value;
    
    $.ajax({
        url: server+path,
        method: "PUT",
        dataType: "text"
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("put-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}
