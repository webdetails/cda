

var CacheManagerBackend = {
  
  CONTENT_HANDLER: 'cacheController',
  STATUS_OK: 'ok',
  STATUS_ERROR: 'error',
  
  defaultErrorHandle: function(status, errorMsg){
    alert('Error :' + errorMsg);
  },
  
  handleResponse: function(response, callback){
        if(response.status == this.STATUS_OK){
          if(typeof(callback) == 'function'){
            callback(response.result);
          }
        }
        else {//TODO: if, etc
          if(typeof(errorCallback) == 'function'){
            errorCallback(reponse.status, response.errorMsg);
          }
        }
  },
  
  postJson: function(args, callback, errorCallback){
    var self = this;
    $.post(this.CONTENT_HANDLER, args,
    function(response)
    {
      self.handleResponse(response, callback, errorCallback);
    }, 'json');
  },
  
  getJson: function(args, callback, errorCallback){
    var self = this;
    $.getJSON(this.CONTENT_HANDLER, args,
      function(response)
      {
        self.handleResponse(response, callback, errorCallback);
      });
      
  }
  
}

/** SCHEDULER **/

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

/** CACHE **/

var refreshCachedOverviewTable = function(){
  $('#scheduledQueries').hide();
  $('#cachedQueriesDetail').hide();
  $('#cachedQueries').show();
  $('#cachedQueriesOverview').show();
  $('#cachedQueriesDetail').hide();
  $('#scheduleButton').removeClass('selected');
  
  var thisButton = $('#cacheButton');
  if(!thisButton.hasClass('selected')){
    thisButton.addClass('selected');
  }
  
  CacheManagerBackend.getJson({method : 'cacheOverview'},populateCachedQueriesOverview, CacheManagerBackend.defaultErrorHandle);
}

var refreshCachedTable = function(cdaSettingsId, dataAccessId){
  
  $('#cachedQueriesOverview').hide('fast');
  $('#cachedQueriesDetail').show('fast');
  
  CacheManagerBackend.getJson(
            {
              method :'cached',
              cdaSettingsId: cdaSettingsId,
              dataAccessId: dataAccessId
            },
            populateCachedQueries,
            function(status, errorMsg){
              var row = $('<div class="span-24 last"/>').text('Problem accessing cache. Check log for errors or reload to try again.');
              $("#cachedQueriesOverviewLines").empty().append(row);
            });
}


var populateCachedQueriesOverview = function(results){
    var ph = $("#cachedQueriesOverviewLines").empty();

  if(results.length > 0){
    for (var i = 0; i< results.length;i++ )
    {
      //<div class='span-16'>CDA Settings</div>
      //<div class='span-6'>Data Access ID</div>
      //<div class='span-2 last'># Queries</div>
      var row = $("<div class='span-24 last row'></div>");
      var item = results[i];
      row.append($('<div/>').addClass('span-16').text(item.cdaSettingsId));
      row.append($('<div/>').addClass('span-6').text(item.dataAccessId));
      row.append($('<div/>').addClass('span-2 last').text(item.count));
      
      var drillDownFunction = function(cdaSettingsId, dataAccessId){
        row.click(function(){
          refreshCachedTable(cdaSettingsId, dataAccessId);
        })
      };
      drillDownFunction(item.cdaSettingsId, item.dataAccessId);
      
      row.addClass('button');
      ph.append(row);
    }
  }
  else {
    var row = $('<div class="span-24 last"/>').text('Cache is empty.');
    ph.append(row);
  }
  
}

var removeCachedQuery =  function(key, row, cdaSettingsId, dataAccessId){
  row.addClass('toDelete');
  if(confirm('Are you sure you want to remove this query from cache?'))
  {
    CacheManagerBackend.postJson({method: 'removeCache', key: key},
      function(result){
        refreshCachedTable(cdaSettingsId, dataAccessId);
      }, CacheManagerBackend.defaultErrorHandle);
  }
  else
  {
    row.removeClass('toDelete');
  }
}

var populateCachedQueries = function(resp){
 
  var ph = $("#cachedQueriesLines").empty();

  if(resp.items.length > 0){
    for (var i = 0; i< resp.items.length;i++ ) {
      //<div class='span-11'>Query</div>
      //<div class='span-5'>Parameters</div>
      //<div class='span-1'># Rows</div>
      //<div class='span-2'>Insertion</div>
      //<div class='span-2'>Last Update</div>
      //<div class='span-1'># Hits</div>
      //<div class='span-2 last'>Operations</div>
      
      var row = $("<div class='span-24 last row'></div>");
      var item = resp.items[i];
      
      row.append($('<div/>').addClass('span-11').text(item.query));
      
      var paramPh = $("<dl></dl>");
      for (var param in item.parameters){
        paramPh.append("<dt>"+param+"</dt><dd>"+item.parameters[param]+"</dd>");
      }
      row.append($('<div/>').addClass('span-5').append(paramPh));
      row.append($('<div/>').addClass('span-1').text(item.rows != null ? item.rows : '?'));
      var insertDate = new Date(item.inserted);
      row.append($('<div/>').addClass('span-2').text(insertDate.toLocaleDateString() + ' ' + insertDate.toLocaleTimeString()));
      var accessDate = new Date(item.accessed);
      row.append($('<div/>').addClass('span-2').text(accessDate.toLocaleDateString() + ' ' + accessDate.toLocaleTimeString()));

      row.append($('<div/>').addClass('span-1').text(item.hits));
      
      //remove from cache
      var removeButton = $("<a  href='javascript:'><img src='cachemanager/delete-24x24.png' class='button' alt='remove from cache'></a>");
      var setRemoveAction = function(key, row, cdaSettingsId, dataAccessId){
        removeButton.click(function(){
          removeCachedQuery(key,row, cdaSettingsId, dataAccessId);
        })
      }
      setRemoveAction(item.key, row, resp.cdaSettingsId, resp.dataAccessId);
      
      //view results
      var tableButton = $("<a  href='javascript:'><img src='cachemanager/table.png' class='button' alt='view results'></a>");
      var setQueryDetailsAction = function(tableContents, key){
        tableButton.click(function(){
            tableContents.toggle();
            if(tableContents.hasClass('empty')){
              tableContents.removeClass('empty');
              if(key)
              {
                tableContents.append( $('<img src="cachemanager/loading.gif" >' ));
                CacheManagerBackend.postJson(
                  {
                    method: 'getDetails',
                    key: key
                  },
                  function(result) {
                     renderCachedTable(result, tableContents);
                  },
                  function(status, errorMsg){
                    tableContents.text('Item could not be retrieved from cache: ' + errorMsg);
                  }
                )
              }
              else{
                alert('this cache element is invalid');//TODO: better msg
              }
            }
        });
      };
      
      $("<div class='span-2 last operations'></div>").append(tableButton).append(removeButton).appendTo(row);
      
      row.append($('<span/>').css('display','none').addClass('keyHolder').text( escape(item.key)));
      
      ph.append(row);
      
        //table
      var tableContentsId = "tableContents" + i;
      ph.append($('<div id="' + tableContentsId + '" />').addClass('span-22 prepend-1 append-1 empty').css('display','none'));
      
      setQueryDetailsAction($('#' + tableContentsId), item.key);
    }
  }
  else {
    var row = $('<div class="span-24 last"/>').text('Cache is empty.');
    ph.append(row);
  }
}

var renderCachedTable = function(data, container){
  
  var tableContents = data.resultset;
  var columnNames = [];
  for (column in data.metadata) {
    columnNames.push({"sTitle": data.metadata[column].colName});
  }
  var table = $('<table class="queryTable"></table>');
  
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
