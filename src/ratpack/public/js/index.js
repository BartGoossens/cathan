  var listCreated = false;

  function appendToList(){

  if(!listCreated){
      $("#items").append("<ul id='list' data-role=\"listview\" data-inset='true'></ul>");
      listCreated = true;
      $("#items").trigger("create");
            $('#list').on('click', 'li', function() {
                alert("Works"); // id of clicked li by directly accessing DOMElement property
                var $this = $(this);
                $this.remove();
            });
  }
  var value = $("#item").val();
  var listItem = "<li>" + value + "</li>";
  $("#list").append(listItem);
//  $("#list").append("<li>test</li>");
  $("#list").listview("refresh");
  }
