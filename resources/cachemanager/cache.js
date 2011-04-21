

var refreshTable = function(){

  $.getJSON('cacheController?method=list', populateQueries);

}

var populateQueries = function(data){
  var ph = $("#lines").empty();


  for (row in data) {
  
    var r = data[row];

    var row = $("<div class='span-24 last row' id='query_" + r.id + "'></div>");

    // Name
    var name = r.cdaFile + " (" + r.dataAccessId + ")";
    row.append("<div class='span-15 left'>" + name + "</div>");

    row.append("<div class='span-1'>" + r.executeAtStart + "</div>");
    row.append("<div class='span-2'>" + formatDate(r.lastExecuted) + " </div>");
    row.append("<div class='span-2'>" + formatDate(r.nextExecution) + " </div>");
    row.append("<div class='span-2'>" + r.cronString + " </div>");
    var deleteButton = "<a  href='cacheController?method=delete&id=" + r.id + "'><img src='cachemanager/delete-24x24.png' class='button' alt='delete'></a>";
    row.append("<div class='span-2 last'>" + deleteButton + "</div>");

    ph.append(row);
  
  }
}


var formatDate = function(date){

  var d = new Date(date);
  return d.getFullYear() + "-" + pad(d.getMonth()+1) + "-"+ pad(d.getDate()) + "<br/>"+pad(d.getHours())+":" + pad(d.getMinutes());
}

var pad = function(n){

  return ("0"+n).substr(n.toFixed().length-1);

}