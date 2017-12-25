var listCreated = false;
//var database = "test2"
refreshList();

function fillList(data) {
    $("#list").empty();
    $.each( data.results, function( key, val ) {
        console.log("got: " + val);
        var listItem = "<li id=" + key + ">" + val + "</li>";
        $("#list").append(listItem);
        $("#list").listview("refresh");
    });
}

function refreshList(){
    if(!listCreated){
        $("#items").append("<ul id='list' data-role=\"listview\" data-inset='true'></ul>");
        listCreated = true;
        $("#items").trigger("create");
        $('#list').on('click', 'li', function() {
            var $this = $(this);
            $.post ("delete/" + $(this).text(), fillList);
            $this.remove();
        });
    }
    $.getJSON("db", fillList);
}

function appendToList(){
    var value = $("#item").val();
    $.post("add/" + value, fillList);
    var listItem = "<li>" + value + "</li>";
    $("#list").append(listItem);
    $("#list").listview("refresh");
    $("#item").val('');
}

$( "#plus" ).bind( "click", function(event, ui) {
        appendToList()
});