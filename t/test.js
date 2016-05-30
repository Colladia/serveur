var server = "http://localhost:8182/";

function onClickGetNoPathButton () {
    $.ajax({
        url: server,
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

function onClickGetButton () {
    var path = document.getElementsByName("GetPath")[0].value;
    var clock = document.getElementsByName("GetClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "GET",
        dataType: "text",
        contentType: "application/json",
        data: {"last-clock": clock}
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
    var clock = document.getElementsByName("PutClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "PUT",
        dataType: "text",
        contentType: "application/json",
        data: {"properties": properties, "last-clock": clock}
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
    var clock = document.getElementsByName("PutClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "PUT",
        dataType: "text",
        contentType: "application/json",
        data: {"last-clock": clock}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("put-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}

function onClickDelButton () {
    var path = document.getElementsByName("DelPath")[0].value;
    var clock = document.getElementsByName("DelClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "DELETE",
        dataType: "text",
        contentType: "application/json",
        data: {"last-clock": clock}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("del-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}

function onClickDelPropButton () {
    var path = document.getElementsByName("DelPath")[0].value;
    var properties = document.getElementsByName("DelProperties")[0].value;
    var clock = document.getElementsByName("DelClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "DELETE",
        dataType: "text",
        contentType: "application/json",
        data: {"properties-list": properties, "last-clock": clock}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("del-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}

function onClickPostButton () {
    var path = document.getElementsByName("PostPath")[0].value;
    var properties = document.getElementsByName("PostProperties")[0].value;
    var clock = document.getElementsByName("PostClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "POST",
        dataType: "text",
        contentType: "application/json",
        data: {"properties": properties, "last-clock": clock}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("post-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}

function onClickAutoPosButton () {
    var path = document.getElementsByName("PostPath")[0].value;
    var clock = document.getElementsByName("PostClock")[0].value;
    if (clock=="") {
        clock="-1"
    }
    
    $.ajax({
        url: server+path,
        method: "POST",
        dataType: "text",
        contentType: "application/json",
        data: {"options": "[\"auto-positioning\"]", "last-clock": clock}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("post-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}
