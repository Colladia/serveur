function onClickButton () {
    $.ajax({
        url: "http://localhost:8182/test",
        method: "GET"
        //dataType: "json",
        //data: {mod: "a"}
    })
    .done(function(data) {
        console.log("OK");
        document.getElementById("get-zone").innerHTML = data;
    })
    .fail(function() {
        console.log("KO");
    });
}
