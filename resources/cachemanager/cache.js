

var refreshTable = function(){

  $.getJSON('cacheController?method=list', populateQueries);

}


var populateQueries = function(data){
  var ph = $("#lines").empty();


  for (row in data.queries) {
  
    var r = data.queries[row];

    var row = $("<div class='span-24 last row "+ (r.success?"":"error") +"' id='query_" + r.id + "'></div>");

    // Name
    var name = r.cdaFile + " (" + r.dataAccessId + ")";
    row.append("<div class='span-7 left'>" + name + "</div>");

    var paramPh = $("<dl></dl></div>");
    for (var param in r.parameters){
      paramPh.append("<dt>"+param+"</dt><dd>"+r.parameters[param]+"</dd>");
    }
    $("<div class='span-5 left'></div>").append(paramPh).appendTo(row);


    row.append("<div class='span-2'>" + formatDate(r.lastExecuted) + " </div>");
    row.append("<div class='span-2'>" + formatDate(r.nextExecution) + " </div>");
    row.append("<div class='span-2'>" + r.cronString + " </div>");
    row.append("<div class='span-2'>" + r.timeElapsed + " </div>");
    row.append("<div class='span-2'>" + (r.success?"Success":"Failed") + " </div>");

    var deleteButton = $("<a  href='javascript:'><img src='cachemanager/delete-24x24.png' class='button' alt='delete'></a>");
    deleteButton.click(function(){
      if(confirm("Want to delete this scheduler?")){
        $.getJSON("cacheController?method=delete&id=" + r.id,function(){
          refreshTable();
        })
      }
    })

    var refreshButton = $("<a  href='javascript:'><img src='cachemanager/refresh-24x24.png' class='button' alt='refresh'></a>");
    refreshButton.click(function(){

      var myself = this;
      $(this).find("img").attr("src","cachemanager/processing.png");
      $.getJSON("cacheController?method=execute&id=" + r.id,function(){
        refreshTable();
      })

    })


    $("<div class='span-2 last operations'></div>").append(refreshButton).append(deleteButton).appendTo(row);

    ph.append(row);
  
  }
}


var formatDate = function(date){

  var d = new Date(date);
  return d.getFullYear() + "-" + pad(d.getMonth()+1) + "-"+ pad(d.getDate()) + "<br/>" +
  pad(d.getHours())+":" + pad(d.getMinutes())+":" + pad(d.getSeconds()) ;
}

var pad = function(n){

  return ("0"+n).substr(n.toFixed().length-1);

}
