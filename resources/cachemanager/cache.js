
var refreshTable = function(){

  $('#cachedQueries').hide();
  $('#scheduledQueries').show();
  $('#cacheButton').removeClass('selected');
  var thisButton = $('#scheduleButton');
  if(!thisButton.hasClass('selected')){
    thisButton.addClass('selected');
  }
  $.getJSON('cacheController?method=list', populateQueries);

}

var refreshCachedTable = function(){
  
    $('#scheduledQueries').hide();
    $('#cachedQueries').show();
    $('#scheduleButton').removeClass('selected');
    var thisButton = $('#cacheButton');
    if(!thisButton.hasClass('selected')){
      thisButton.addClass('selected');
    }
    $.getJSON('cacheController?method=cached', populateCachedQueries);
    
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
    var deleteFunction = function(id){
      deleteButton.click(function(){
        if(confirm("Want to delete this scheduler?")){
          $.getJSON("cacheController?method=delete&id=" + id ,function(){
            refreshTable();
          })
        }
      })
    };
    deleteFunction(r.id);

    var refreshButton = $("<a  href='javascript:'><img src='cachemanager/refresh-24x24.png' class='button' alt='refresh'></a>");
    var refreshFunction = function(id){
      refreshButton.click(function(){

        var myself = this;
        $(this).find("img").attr("src","cachemanager/processing.png");
        $.getJSON("cacheController?method=execute&id=" + id,function(){
          refreshTable();
        })

      })
    };
    refreshFunction(r.id);

    $("<div class='span-2 last operations'></div>").append(refreshButton).append(deleteButton).appendTo(row);

    ph.append(row);
  
  }
}

var populateCachedQueries = function(resp){
 
  var ph = $("#cachedQueriesLines").empty();

  if(resp.results.length > 0){
    for (var i = 0; i< resp.results.length;i++ ) {
      //<div class='span-13'>Query</div>
      //<div class='span-5'>Parameters</div>
      //<div class='span-2'>Insertion</div>
      //<div class='span-2'>Last Update</div>
      //<div class='span-2'># Hits</div>
      
      var row = $("<div class='span-24 last row'></div>");
      var item = resp.results[i];
      
      row.append($('<div/>').addClass('span-13').text(item.query));
      
      var paramPh = $("<dl></dl>");
      for (var param in item.parameters){
        paramPh.append("<dt>"+param+"</dt><dd>"+item.parameters[param]+"</dd>");
      }
      row.append($('<div/>').addClass('span-5').append(paramPh));
      var insertDate = new Date(item.inserted);
      row.append($('<div/>').addClass('span-2').text(insertDate.toLocaleDateString() + ' ' + insertDate.toLocaleTimeString()));
      var accessDate = new Date(item.accessed);
      row.append($('<div/>').addClass('span-2').text(accessDate.toLocaleDateString() + ' ' + accessDate.toLocaleTimeString()));
      row.append($('<div/>').addClass('span-2 last').text(item.hits));
      
      ph.append(row);
  
      if(item.table && item.table.metadata && item.table.resultset){
        row.click( function() {
          $(this.nextSibling).toggle();
        });
        
        //table
        var tableContentsId = "tableContents" + i;
        ph.append($('<div id="' + tableContentsId + '" />').addClass('span-22 prepend-1 append-1').css('display','none'));
        renderCachedTable(item.table, tableContentsId);
      }
    }
  }
  else {
    var row = $('<div class="span-24 last row"/>').text('Cache is empty.');
    ph.append(row);
  }
}

var renderCachedTable = function(data, containerId){
  
  var tableContents = data.resultset;
  var columnNames = [];
  for (column in data.metadata) {
    columnNames.push({"sTitle": data.metadata[column].colName});
  }
  var table = $('<table class="queryTable"></table>');
  
  var container = $('#' + containerId);
  container.empty();
  container.append(table);
  
  var dTable = table.dataTable({"aaData": tableContents, "aoColumns": columnNames, "bFilter" : false});
  
}


var formatDate = function(date){
  var d = new Date(date);
  return d.getFullYear() + "-" + pad(d.getMonth()+1) + "-"+ pad(d.getDate()) + "<br/>" +
  pad(d.getHours())+":" + pad(d.getMinutes())+":" + pad(d.getSeconds()) ;
}

var pad = function(n){

  return ("0"+n).substr(n.toFixed().length-1);

}
