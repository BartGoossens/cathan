var listCreated = false;
//var database = "test2"
refreshList();

function fillList(data) {
    $("#list").empty();
    $("#checklist").empty();
    $("#del").parent().hide();
    $.each( data.results, function( key, val ) {
        if (val.state === "new") {
            var listItem = "<li id=" + val.id + " state=" + val.state + "><a href=\"#\"  class=\"ui-btn ui-btn-icon-left ui-icon-shop\">" + val.value + "</a></li>";
            $("#list").append(listItem);
            $("#list").listview("refresh");
        } else {
            $("#del").parent().show();
            var listItem = "<li id=" + val.id + " state=" + val.state + "><a href=\"#\"  class=\"ui-btn ui-btn-icon-left ui-icon-check\"><del>" + val.value + "</del></a></li>";
            $("#checklist").append(listItem);
            $("#checklist").listview("refresh");
        }
    });
}

function refreshList(){
    if(!listCreated){
        $("#items").append("<ul id='list' data-role=\"listview\" data-inset='true'></ul>");
        $("#items").append("<input type=\"button\" value=\"Verwijder afgevinkte items\" id='del' data-icon=\"delete\"/>");
        $("#items").append("<ul id='checklist' data-role=\"listview\" data-inset='true'></ul>");
        listCreated = true;
        $("#items").trigger("create");
        $("#del").parent().hide();
        $('#list').on('click', 'li', function() {
            $("#del").parent().show();
            var $this = $(this);
            var listItem = "<li id=" + $(this).attr("id") + " state=\"check\"><a href=\"#\"  class=\"ui-btn ui-btn-icon-left ui-icon-refresh\"><del>" + $(this).text() + "</del></a></li>";
            $("#checklist").append(listItem);
            $this.remove();
            $("#list").listview("refresh");
             $("#checklist").listview("refresh");
            $.post ("state/" + $(this).attr("id") +"/check", fillList);
        });
        $('#checklist').on('click', 'li', function() {
            var $this = $(this);
            var listItem = "<li id=" + $(this).attr("id") + " state=\"new\"><a href=\"#\"  class=\"ui-btn ui-btn-icon-left ui-icon-refresh\">" + $(this).text() + "</a></li>";
            $("#list").append(listItem);
            $this.remove();
            if ($("#checklist li").length <= 0) {
                $("#del").parent().hide();
            }
            $("#list").listview("refresh");
            $("#checklist").listview("refresh");
            $.post ("state/" + $(this).attr("id") +"/new", fillList);
        });
    }
    $.getJSON("db", fillList);
}

function appendToList(){
    var value = $("#item").val();
    if(value.length > 0) {
        $.post("add/" + value, fillList);
        var listItem = "<li><a href=\"#\"  class=\"ui-btn ui-btn-icon-left ui-icon-refresh\">" + value + "</a></li>";
        $("#list").append(listItem);
        $("#list").listview("refresh");
        $("#item").val('');
    }
}

$( "#del" ).bind( "click", function(event, ui) {
    console.log("Delete checked");
    $(this).parent().hide();
    $("#checklist").empty();
    $.post ("purge", fillList);
});

$( "#plus" ).bind( "click", function(event, ui) {
        appendToList();
});

$( "#item" ).bind( "change", function(event, ui) {
        appendToList();
});