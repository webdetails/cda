/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

var PreviewerBackend = {
  /* overridden by backend */
  PATH_page: null,
  PATH_doQuery: null,
  PATH_unwrapQuery: null,
  PATH_listParameters: null,
  PATH_listQueries: null,
  PATH_cacheController: null,
  PATH_about: null,
  LOCALE_dataTablesObj: null,
  LOCALE_locale: 'browser',
  PATH_locales: null,
  LOCALE_dataTables: null,
  Path: null,
  /**/
  listQueries: function(params, callback) {
    $.ajax({
      type: "GET",
      dataType: "json",
      url: this.PATH_listQueries,
      data: params,
      success: callback,
      error: function(xhr, status, error) {
        showErrorMessage("Error Listing Queries");
      }
    });
  },

  listParameters: function(params, callback) {
    $.ajax({
      type: "GET",
      dataType: "json",
      url: this.PATH_listParameters,
      data: params,
      success: callback,
      error: function(xhr, status, error) {
        showErrorMessage("Error Listing Parameters");
      }
    });
  },

  doQuery: function(params, callback) {
    $.ajax({
      type: "GET",
      dataType: "json",
      url: this.PATH_doQuery,
      data: params,
      success: callback,
      error: function(xhr, status, error) {
        hideButtons(false);
        showErrorMessage("Error Executing Query");
      }
    });
  },

  scheduleQuery: function(params, callback) {
    $.ajax({
      type: "POST",
      dataType: "json",
      url: this.PATH_cacheController + '/change',
      data: params,
      success: callback,
      error: function(xhr, status, error) {
        $("#dialog").jqmHide();
        showErrorMessage("Error Scheduling Query");
      }
    });
  }
};

if($.blockUI) {

  $.blockUI.defaults.fadeIn = 0;
  $.blockUI.defaults.message = '';
  $.blockUI.defaults.css.left = '50%';
  $.blockUI.defaults.css.top = '40%';
  $.blockUI.defaults.css.marginLeft = '-16px';
  $.blockUI.defaults.css.width = '32px';
  $.blockUI.defaults.css.background = 'none';
  $.blockUI.defaults.overlayCSS = {backgroundColor: "#FFFFFF", opacity: 0.8, cursor: "wait"};
  $.blockUI.defaults.css.border = "none";
}

showErrorMessage = function(message) {
  $('#previewerTable')
      .empty()
      .html('<span class="error-status">' + message + '</span>');
  $.unblockUI();
};

hideButtons = function(hideRefresh) {
  $('#exportButton').hide();
  $('#queryUrl').hide();
  $('#cachethis').hide();
  if(hideRefresh) {
    $('#button').hide();
  } else {
    $('#button').show();
  }

};

showButtons = function() {
  $('#exportButton').show();
  $('#queryUrl').show();
  $('#cachethis').show();
  $('#button').show();
};

getFileName = function() {
  return PreviewerBackend.Path;
};

pageParams = function() {
  var url = document.location.href;
  var output = {};

  // The parameters are in the section between '?' and '#' (if any)
  var startIndex = url.indexOf('?');
  var endIndex = url.indexOf('#');
  var params = (endIndex > 0) ? url.slice(startIndex + 1, endIndex).split('&') : url.slice(startIndex + 1).split('&');
  for(var param in params) {
    if(params.hasOwnProperty(param)) {
      var p = params[param].split('=');
      output[p[0]] = decodeURIComponent(p[1]);
    }
  }
  return output;
};

resetPreview = function() {
  $('#notifications').hide();
  hideButtons(true);

  clearParameters();
  lastQuery = undefined;

  $('#previewerTable')
      .empty()
      .append($("<span id=\"pleaseselect\"></span>")
          .text("Please select a Data Access ID"));

};

refreshTable = function(id) {
  // Detect whether the change was triggered by a refresh or a change in DataAccessId
  var params;
  if(id !== lastQuery) {
    // When we change query, we must drop the table and parameters, and rebuild both
    lastQuery = id;
    refreshParams(id);
    params = {path: getFileName(), dataAccessId: id};
  } else {
    // Same query, we need to get the present parameter values and rebuild the table
    params = getParams();
    params.path = getFileName();
    params.dataAccessId = id;
    params.outputIndexId = $('#outputIndexId').val();
  }
  if($.blockUI.defaults.message == "") {
    $.blockUI.defaults.message = '<div style="padding: 0;"><img src="' + PreviewerBackend.PATH_page + '/img/processing_transparent.gif" />';
  }
  $.blockUI();
  PreviewerBackend.doQuery(params, showTable);
};

showTable = function(data) {
  $.unblockUI();
  var tableContents = ignoreNullRows(data.resultset);
  var columnNames = [];
  for(var column in data.metadata) {
    if(data.metadata.hasOwnProperty(column)) {
      columnNames.push({"sTitle": data.metadata[column].colName});
    }
  }
  $('#previewerTable')
      .empty()
      .html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="contents"></table>');
  showButtons();
  if(oLanguage == null) {
    tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames});
  } else {
    tableController = $('#contents').dataTable({
      "aaData": tableContents,
      "aoColumns": columnNames,
      "oLanguage": oLanguage
    });
  }
};

ignoreNullRows = function(table) {
  var cleanTable = [];
  if(table != null) {
    for(var i = 0, R = table.length; i < R; i++) {
      var row = table[i];
      if(row != null) {
        for(var j = 0, C = row.length; j < C; j++) {
          //Replacing null value for a string with null, so that it is displayed in the preview table
          if(row[j] == null) {
            row[j] = "null";
          }
        }
        cleanTable.push(row);
      } else if(console && console.error) {
        console.error("row #" + i + " is null");
      }
    }
  }
  return cleanTable;
};

showQueryUrl = function(dataAccessId) {
  var queryUrlDialogInput = $('#queryUrlDialog input');
  queryUrlDialogInput.val(getFullQueryUrl(dataAccessId));
  $('#queryUrlDialog').jqmShow();
  queryUrlDialogInput.select();
};

exportFunc = function(dataAccessId) {
  updateLastQuery(dataAccessId);

  var params = getParams();
  var queryDefinition = $.extend({
    dataAccessId: dataAccessId,
    path: getFileName(),
    outputType: 'xls',
    wrapItUp: true
  }, params);

  $.ajax({
    type: 'POST',
    dataType: 'text',
    async: true,
    data: queryDefinition,
    url: PreviewerBackend.PATH_doQuery,
    xhrFields: {
      withCredentials: true
    },
    success: function(uuid) {
      var _exportIframe = $('#cdaExportIframe');
      if(!_exportIframe.length) {
        _exportIframe = $('<iframe id="cdaExportIframe" style="display:none">');
      }
      _exportIframe.attr('src', getUnwrapQueryUrl({path: queryDefinition.path, uuid: uuid}));
      _exportIframe.appendTo($('body'));
    },
    error: function(jqXHR, status, error) {
      console && console.error("Request failed: " + jqXHR.responseText + " :: " + status + " ::: " + error);
    }
  });
};

getUnwrapQueryUrl = function(parameters) {
  return PreviewerBackend.PATH_unwrapQuery + "?" + $.param(parameters)
};

getFullQueryUrl = function(dataAccessId, extraParams) {
  updateLastQuery(dataAccessId);

  var params = getParams();
  return window.location.protocol + '//' + window.location.host +
      PreviewerBackend.PATH_doQuery
      + '?path=' + getFileName()
      + '&' + $.param($.extend({dataAccessId: dataAccessId}, params, extraParams));
};

updateLastQuery = function(dataAccessId) {
  if(dataAccessId !== lastQuery) {
    lastQuery = dataAccessId;
    refreshParams(dataAccessId);
  }
};

refreshParams = function(id) {
  PreviewerBackend.listParameters({path: getFileName(), dataAccessId: id}, function(data) {
    var placeholder = $('#parameterHolder');
    clearParameters();
    for(var param in data.resultset) {
      if (data.resultset.hasOwnProperty(param)) {
        placeholder.append('<div class="param span-5 last"><div class="span-5" id="parameterDimension">' + data.resultset[param][0] +
        ':&nbsp;</div><div class="cdaInputWrapper span-5 last"><input class="cdaButton" id="' + data.resultset[param][0] +
        '" value="' + data.resultset[param][2] + '"' + ((data.resultset[param][4] == 'private') ? ' readonly="readonly"' : '') + '"><div class="helpButton">?</div></div></div>');
      }
    }
    placeholder.find("div.helpButton").click(helpPopup).hide();
    placeholder.find("input").focus(inputFocus).blur(inputBlur);
  });

};

getParams = function() {
  var params = {};
  $('#parameterHolder input').each(function(index, param) {
    params['param' + $(param).attr('id')] = $(param).val()
  });
  return params;
};

clearParameters = function() {
  $('#parameterHolder').empty();
};

cacheThis = function() {

  var queryDefinition = {};
  var params = [];
  $('#parameterHolder input').each(function(index, param) {
    params.push({name: $(param).attr('id'), value: $(param).val()});
  });
  queryDefinition.parameters = params;
  queryDefinition.cdaFile = PreviewerBackend.Path;
  queryDefinition.dataAccessId = $('#dataAccessSelector').val();
  queryDefinition.cronString = $('#cron').val() || simpleCron();
  var json = JSON.stringify(queryDefinition);
  var notification = $('#dialog .notification');
  if(!notification.length) {
    notification = $("<span class='notification'></span>").appendTo('.dialogAction');
  }

  PreviewerBackend.scheduleQuery({"object": json}, function(response) {
    if(response.status == 'ok') {
      notification.text('');
      $("#dialog").jqmHide();
      window.open('manageCache');
    } else {
      notification.text(response.errorMsg);
    }
  });
};


periodicity = [
  {name: "every week", granularity: "day of the week (1-7)", cron: "0 0 0 ? * x"},
  {name: "every day", granularity: "hour (0-23)", cron: "0 0 x * * ?"},
  {name: "every hour", granularity: "minute (0-59)", cron: "0 x * * * ?"},
  {name: "every minute", granularity: "second (0-59)", cron: "x * * * * ?"}
];

toggleAdvanced = function(advanced) {
  if(advanced == false) {
    var selector = '<select id="periodType">';
    for(var option in periodicity) {
      if(periodicity.hasOwnProperty(option)) {
        selector += '<option value="' + option + '">' + periodicity[option].name + '</option>';
      }
    }
    selector += '</select>';
    var contents = $('<span>' + selector + '<span>on the</span><input id="startAt" style="width: 40px; text-align: center;">' +
    '<span id="granularity">th ' + periodicity[0].granularity + '</span></span>');

    contents.find('select#periodType').change(function() {
      $(this).parent().find('#granularity')
          .text(periodicity[$(this).val()].granularity);
    });
  } else {
    contents = '<span>Cron Expression: <input id="cron" placeholder="0 */10 * * * ? "></span>';
  }

  $("#dialogInput").empty().append(contents);
  $("#dialog .notification").text('');
  $("p.dialogTitle .dialogToggle")
      .html(advanced ? "(basic)" : "(advanced)")
      .attr("href", "javascript:;")
      .click(function() {
        toggleAdvanced(!advanced);
      }
  );
};

simpleCron = function() {
  var $selector = $('#periodType');
  if($selector.length) {
    var val = $selector.val(),
        startAt = $("#startAt").val(),
        period = periodicity[val],
        cronExpression;

    cronExpression = period.cron.replace('x', startAt);
    return cronExpression;
  }
};

var inputFocus = function(event) {
  $(this).addClass("cdaButtonSelected");
  $(this).parent().find("div.helpButton").show();
};

var inputBlur = function(event) {
  // we need to delay this evaluation ever so slightly, so that
  // we don't hide the help button before it registers the click
  var myself = this;
  setTimeout(
      function() {
        $(myself).removeClass("cdaButtonSelected");
        $(myself).parent().find("div.helpButton").hide();
      },
      125);
};

helpPopup = function() {
  $("#help").jqmShow();
};
