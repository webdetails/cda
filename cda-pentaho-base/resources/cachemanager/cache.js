/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

/**
 * Backend interaction, constants will be overridden by the server
 **/
var CacheManagerBackend = {
  
  CACHE_MONITOR: null,
  CACHE_SCHEDULER: null,

  STATUS_OK: 'ok',
  STATUS_ERROR: 'error',

  defaultErrorHandle: function(status, errorMsg) {
    alert('Error :' + errorMsg);
  },

  handleResponse: function(response, callback) {
    if(response.status == this.STATUS_OK) {
      if(typeof(callback) == 'function') {
        callback(response.result);
      }
    } else {
      if(typeof(errorCallback) == 'function') {
        errorCallback(reponse.status, response.errorMsg);
      } else {
        this.defaultErrorHandle(response.status, response.errorMsg);
      }
    }
  },

  postJson: function(args, callback, errorCallback) {
    var self = this;
    UI.loadingImg.show();
    $.post(this.CACHE_MONITOR + "/" + args.method, args,
    function(response) {
      UI.loadingImg.hide();
      self.handleResponse(response, callback, errorCallback);
    }, 'json');
  },
  
  getJson: function(args, callback, errorCallback) {
    var self = this;
    UI.loadingImg.show();
    $.getJSON(this.CACHE_MONITOR + "/" + args.method, args,
      function(response) {
        UI.loadingImg.hide();
        self.handleResponse(response, callback, errorCallback);
      });

  }

};

/**
 * Holds references to the html objects.
 **/
var UI = {

  setButtonSelected: function(button, selected) {
    if(!button.hasClass('selected')) {
      if(selected) button.addClass('selected');
    }
    else if(!selected) button.removeClass('selected');
  },

  //define UI elements
  init: function() {
    //main panes
    this.cachedQueries = $('#cachedQueries');
    this.scheduledQueries = $('#scheduledQueries');
    //main pane switch buttons
    this.cacheButton = $('#cacheButton');
    this.scheduleButton = $('#scheduleButton');
    //cache area
    this.cachedQueriesDetail = $('#cachedQueriesDetail');
    this.cachedQueriesOverview = $('#cachedQueriesOverview');
    this.cachedQueriesOverviewLines = $("#cachedQueriesOverviewLines");
    this.clearCacheButton = $("#clearCacheButton");
    //loading imgs
    this.loadingImg = $('#loading');
    this.icons = $('#icons');
  },

  getIcon: function(iconClass) {
    return this.icons.children(iconClass).clone();
  },
  createButton: function (iconClass) {
    var button = this.getIcon(iconClass).addClass('button');
    return $('<a href="javascript:"></a>').append(button);
  }
};

/** SCHEDULER **/

var refreshTable = function() {
  UI.cachedQueries.hide();
  UI.scheduledQueries.show();
  UI.setButtonSelected(UI.cacheButton, false);
  UI.setButtonSelected(UI.scheduleButton, true);
  CdaCacheScheduler.listEntries( populateQueries );
};


var populateQueries = function(data) {
  var ph = $("#lines").empty();

  for (row in data.queries) {

    var r = data.queries[row];

    var row = $("<div class='span-23 last row "+ (r.success?"":"error") +"' id='query_" + r.id + "'></div>");

    // Name
    var name = r.cdaFile + " (" + r.dataAccessId + ")";
    row.append("<div class='span-7 left'>" + name + "</div>");

    var paramPh = $("<dl></dl></div>");
    for (var param in r.parameters) {
      paramPh.append("<dt>"+param+"</dt><dd>"+r.parameters[param]+"</dd>");
    }
    $("<div class='span-4 left'></div>").append(paramPh).appendTo(row);


    row.append("<div class='span-2'>" + formatDate(r.lastExecuted) + " </div>");
    row.append("<div class='span-2'>" + formatDate(r.nextExecution) + " </div>");
    row.append("<div class='span-2'>" + r.cronString + " </div>");
    row.append("<div class='span-2'>" + r.timeElapsed + " </div>");
    row.append("<div class='span-2'>" + (r.success?"Success":"Failed") + " </div>");

    var deleteButton =  UI.createButton('.deleteIcon');
    var deleteFunction = function(id) {
      deleteButton.click(function() {
        if(confirm("Want to delete this scheduler entry?")) {
	        CdaCacheScheduler.deleteEntry(id, refreshTable);
        }
      })
    };
    deleteFunction(r.id);

    var refreshButton = UI.createButton('.refreshIcon');
    var refreshFunction = function(id) {
      refreshButton.click(function() {
        $(this).find('.refreshIcon').removeClass('refreshIcon').addClass('loadingIcon');
        CdaCacheScheduler.executeEntry(id, refreshTable);
      })
    };
    refreshFunction(r.id);

    $("<div class='span-2 last operations'></div>").append(refreshButton).append(deleteButton).appendTo(row);

    ph.append(row);
  
  }
};

/** CACHE **/

//Main cache view
var refreshCachedOverviewTable = function() {
  UI.scheduledQueries.hide();
  UI.cachedQueriesDetail.hide();
  
  UI.cachedQueries.show();
  UI.cachedQueriesDetail.hide();
  UI.cachedQueriesOverview.show();
  
  UI.setButtonSelected(UI.scheduleButton, false);
  UI.setButtonSelected(UI.cacheButton, true);
  
  UI.clearCacheButton.text('Clear Cache');
  UI.clearCacheButton.unbind('click');
  UI.clearCacheButton.click(function() {
    if(confirm('This will remove ALL items from cache. Are you sure?')) {
      CacheManagerBackend.postJson(
        {method:'removeAll'},
        function(itemsDeleted) {
          alert(itemsDeleted + ' items have been removed from cache');
          CacheManagerBackend.getJson({method : 'cacheOverview'},populateCachedQueriesOverview);
        }
      );
    }
  });

  //getGeneralInfo();
  
  CacheManagerBackend.getJson({method : 'cacheOverview'},populateCachedQueriesOverview, CacheManagerBackend.defaultErrorHandle);
}

var refreshCachedTable = function(cdaSettingsId, dataAccessId) {
  
  UI.cachedQueriesOverview.hide();
  UI.cachedQueriesDetail.show();
   
  var clearBtnText = "Clear all items";
  if(cdaSettingsId != null) {
    clearBtnText += " for " + cdaSettingsId;
    if(dataAccessId != null) {
      clearBtnText += " (" + dataAccessId + ")";
    }
  }
  UI.clearCacheButton.text(clearBtnText);
  UI.clearCacheButton.unbind('click');
  UI.clearCacheButton.click(function() {
    var confText = "This will clear All items" +
      (cdaSettingsId != null?
       (" where cdaSettingsId='" + cdaSettingsId + "'" +
         (dataAccessId!=null? "and dataAccessId='"+ dataAccessId+"'" : "")):
       "");
    confText += ". Are you sure?";
    if(confirm(confText)) {
      var callArgs = {method:'removeAll'};
      if(cdaSettingsId!= null) {
        callArgs.cdaSettingsId = cdaSettingsId;
        if(dataAccessId!=null) callArgs.dataAccessId = dataAccessId;
      }
      CacheManagerBackend.postJson(
        callArgs,
        function(itemsDeleted) {
          alert(itemsDeleted + ' items have been removed from cache');
          refreshCachedTable(cdaSettingsId, dataAccessId);
        }
      );
    }
  });
  
  var callArgs = {method:'cached'};
  if(cdaSettingsId!= null) {
    callArgs.cdaSettingsId = cdaSettingsId;
    if(dataAccessId!=null) callArgs.dataAccessId = dataAccessId;
  }
  CacheManagerBackend.getJson(
            callArgs,
            populateCachedQueries,
            function(status, errorMsg){
              var row = $('<div class="span-23 last"/>').text('Problem accessing cache. Check log for errors or reload to try again.');
              $("#cachedQueriesOverviewLines").empty().append(row);
            });
};

//should be improved
var getHumanReadableSize = function(byteSize) {
  var units = ['B', 'kB', 'MB', 'GB'];
  var convRate = 1024;
  var okVal = 2047;
  var size = byteSize;

  for(i=0; i<units.length; i++) {
    if(size < okVal || i== units.length -1) {
      return Math.round(size) + ' ' + units[i];
    }

    size /= convRate;
  }
  return '?';
}

//NOT IMPLEMENTED:
var getGeneralInfo = function() {
  CacheManagerBackend.getJson({method: 'clusterInfo'}, populateClusterInfo, CacheManagerBackend.defaultErrorHandle);
  CacheManagerBackend.getJson({method: 'mapInfo'}, populateMapInfo, CacheManagerBackend.defaultErrorHandle);
}

var populateClusterInfo = function(result) {
  var LENGTH = 12;
  var members = result.otherMembers;
  members.splice(0,0,result.localMember);
  
  var holder = $('#membersInfo');
  
  for(var i=0; i<members.length; i++) {
    $('<div/>').class('span-' + LENGTH + ' row last').text(members[i].address).appendTo(holder);
  }
}

var populateMapInfo = function(result) {
  var holder = $('#mapInfo');
  
  var str = result.entryCount + ' entries, (' + result.ownedCount + ' owned), totalling ' + getHumanReadableSize(result.mapMemSize);
  holder.text(str);
}

var populateCachedQueriesOverview = function(results) {
  var ph = $("#cachedQueriesOverviewLines").empty();
  if(results.length > 0) {
    for (var i = 0; i< results.length;i++ ) {

      var row = $("<div class='span-23 last row'></div>");
      var item = results[i];
      var settingsLink = $('<span/>').text(item.cdaSettingsId);
      row.append($('<div/>').addClass('span-15').append(settingsLink));

      row.append($('<div/>').addClass('span-6').text(item.dataAccessId));
      row.append($('<div/>').addClass('span-2 last').text(item.count));

      var drillDownFunction = function(cdaSettingsId, dataAccessId) {
        row.click(function() {
          refreshCachedTable(cdaSettingsId, dataAccessId);
        });
        row.addClass('button');
      };
      drillDownFunction(item.cdaSettingsId, item.dataAccessId);

      var drillOnSettingsFunc = function(cdaSettingsId) {
        settingsLink.click(function(event) {
          //alert(cdaSettingsId);
          event.stopPropagation();
          refreshCachedTable(cdaSettingsId, null);
        });
      };
      drillOnSettingsFunc(item.cdaSettingsId);
      settingsLink.addClass('link');

      ph.append(row);
    }
  } else {
    var row = $('<div class="span-24 last"/>').text('Cache is empty.');
    ph.append(row);
  }
  
};

var removeCachedQuery =  function(key, row, cdaSettingsId, dataAccessId) {
  row.addClass('toDelete');
  if(confirm('Are you sure you want to remove this query from cache?')) {
    CacheManagerBackend.postJson({method: 'removeCache', key: key},
      function(result) {
        refreshCachedTable(cdaSettingsId, dataAccessId);
      }, CacheManagerBackend.defaultErrorHandle);
  } else {
    row.removeClass('toDelete');
  }
};

var populateCachedQueries = function(resp) {
 
  var ph = $("#cachedQueriesLines").empty();

  if(resp.items.length > 0) {
    for (var i = 0; i< resp.items.length;i++ ) {

      var row = $("<div class='span-23 last row'></div>");
      var item = resp.items[i];

      //query
      var queryCol = $('<div/>').addClass('span-8').addClass('queryCol').text(item.query);
      queryCol.click(
        function() {
          alert($(this).text());
        }
      );
      row.append(queryCol);

      //parameters
      var paramPh = $("<dl></dl>");
      for (var param in item.parameters) {
        paramPh.append("<dt>"+param+"</dt><dd>"+item.parameters[param]+"</dd>");
      }
      row.append($('<div/>').addClass('span-5').append(paramPh));
      //rows
      row.append($('<div/>').addClass('span-1').text(item.rows));
      //size
      row.append($('<div/>').addClass('span-2').text(item.size != null ? getHumanReadableSize(item.size) : '?'));
      //insert date
      var insertDate = new Date(item.inserted);
      row.append($('<div/>').addClass('span-2').text(insertDate.toLocaleDateString() + ' ' + insertDate.toLocaleTimeString()));
      //access date
      var accessDate = new Date(item.accessed);
      row.append($('<div/>').addClass('span-2').text(accessDate.toLocaleDateString() + ' ' + accessDate.toLocaleTimeString()));
      //#hits
      row.append($('<div/>').addClass('span-1').text(item.hits));

      //remove from cache
      var removeButton = UI.createButton('.deleteIcon');
      var setRemoveAction = function(key, row, cdaSettingsId, dataAccessId) {
        removeButton.click(function() {
          removeCachedQuery(key,row, cdaSettingsId, dataAccessId);
        })
      };
      setRemoveAction(item.key, row, resp.cdaSettingsId, resp.dataAccessId);

      //view results
      var tableButton = UI.createButton('.tableIcon');
      var setQueryDetailsAction = function(tableContents, key) {
        tableButton.click(function() {
            tableContents.toggle();
            if(tableContents.hasClass('empty')) {
              tableContents.removeClass('empty');
              if(key) {
                tableContents.append( UI.getIcon('.loadingIcon') );
                CacheManagerBackend.postJson(
                  {
                    method: 'getDetails',
                    key: key
                  },
                  function(result) {
                     renderCachedTable(result, tableContents);
                  },
                  function(status, errorMsg) {
                    tableContents.text('Item could not be retrieved from cache: ' + errorMsg);
                  }
                )
              } else {
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
      ph.append($('<div id="' + tableContentsId + '" />').addClass('span-22 prepend-1 append-1 empty last queryTable').css('display','none'));

      setQueryDetailsAction($('#' + tableContentsId), item.key);
    }
  } else { //no items
    var row = $('<div class="span-24 last"/>').text('No queries in cache for ' + resp.cdaSettingsId +
                                                    (resp.dataAccessId!=null?' (' + resp.dataAccessId + ')' : ''));
    ph.append(row);
  }
};

var renderCachedTable = function(data, container) {
  var tableContents = data.resultset;
  var columnNames = [];
  for (column in data.metadata) {
    columnNames.push({"sTitle": data.metadata[column].colName});
  }
  var table = $('<table class="queryTable"></table>');
  
  container.empty();
  container.append(table);
  
  var dTable = table.dataTable({"aaData": tableContents, "aoColumns": columnNames, "bFilter" : false});
  
};


var formatDate = function(date) {
  var d = new Date(date);
  return d.getFullYear() + "-" + pad(d.getMonth()+1) + "-"+ pad(d.getDate()) + "<br/>" +
  pad(d.getHours())+":" + pad(d.getMinutes())+":" + pad(d.getSeconds()) ;
};

var pad = function(n) {

  return ("0"+n).substr(n.toFixed().length-1);

};
