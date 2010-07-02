
populateQueries = function(data){
    var html = "";
    for (row in data) {
        var name = data[row].cdaFile + "(" + data[row].dataAccessId + ")";
        var deleteButton = "<a  href='cacheController?method=delete&id=" + data[row].id + "'><img src='cachemanager/delete-24x24.png' class='button' alt='delete'></a>";
        html += "<div class='span-24 last row' id='query_" + data[row].id + "'><div class='span-6'>"+ name +"</div><div class='span-16'>" + "&nbsp;"+ "</div><div class='span-2 last' style='text-align:center'>" + deleteButton + "</div></div>";
    }
    $("#lines").html(html);
}
