/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
  return PreviewerBackend.PATH_unwrapQuery + "?" + $.param(parameters, true);
};

getFullQueryUrl = function(dataAccessId, extraParams) {
  updateLastQuery(dataAccessId);

  var params = getParams();
  return window.location.protocol + '//' + window.location.host +
      PreviewerBackend.PATH_doQuery + '?path=' + getFileName() +
      '&' + $.param($.extend({dataAccessId: dataAccessId}, params, extraParams), true);
};

updateLastQuery = function(dataAccessId) {
  if(dataAccessId !== lastQuery) {
    lastQuery = dataAccessId;
    refreshParams(dataAccessId);
  }
};

refreshParams = function(id) {
  var myself = this;
  PreviewerBackend.listParameters({path: getFileName(), dataAccessId: id}, function(data) {
    var placeholder = $('#parameterHolder');
    clearParameters();
    for(var param in data.resultset) {
      if (data.resultset.hasOwnProperty(param)) {
        var paramData = data.resultset[param];
        var id="param_" + param;
        var name = paramData[0];
        var value = paramData[2];
        var readonly = paramData[4] === "private" ? "readonly='readonly'" : "";
        var paramCss = "param span-5 last " + (validateInput(value, paramData) ? "" : "invalid-input");
        var errorMessage = "Must be " + paramData[1] + "*";


        var paramInput = $(
            '<div id="' + id + '" class="' + paramCss + '">' +
            '  <div class="span-5" id="parameterDimension">' + name + ':&nbsp;' +
            '    <span class="error-invalid-input">' + errorMessage + '</span>' +
            '  </div>' +
            '  <div class="cdaInputWrapper span-5 last">' +
            '    <input id="' + name + '" class="cdaButton" value="' + value + '" ' + readonly + '>' +
            '    <div class="helpButton">?</div><div class="errorButton">!</div>' +
            '  </div>' +
            '</div>'
        );

        paramInput.change(function(event) {
          var wrapper = $(this);
          var index = wrapper.attr("id").replace("param_", "");
          var paramData = data.resultset[index];

          var helpB = wrapper.find(".helpButton");
          var errorB = wrapper.find(".errorButton");

          var inputValue = wrapper.find('.cdaButton').val();

          if(!myself.validateInput(inputValue, paramData)) {
            wrapper.addClass("invalid-input");
            helpB.hide();
            errorB.show();

          } else {
            wrapper.removeClass("invalid-input");
            errorB.hide();
            helpB.show();
          }
        });

        placeholder.append(paramInput);
      }
    }
    placeholder.find("div.helpButton").click(helpPopup).hide();
    placeholder.find("div.errorButton").click(errorButton).hide();
    placeholder.find("input")
        .focus(function(event) {
          var valid = !$(this).parents('.param').hasClass('invalid-input');
          inputFocus(event, $(this), valid);
        }).blur(inputBlur);
  });

};

validateInput = function(inputValue, data) {
  var type = data[1].replace("Array", "") || "";
  var values = inputValue.split(";");

  for(var i = 0, L = values.length; i < L; i++) {
    var value = values[i];

    if(value === "" || validateFormula(value)) {
      return true;
    }

    switch(type) {
      case "String":
        if(!validateString(value)) {
          return false;
        }
        break;

      case "Integer":
        if(!validateInteger(value)) {
          return false;
        }
        break;

      case "Numeric":
        if(!validateNumeric(value)) {
          return false;
        }
        break;

      case "Date":
        var pattern = data[3] || "";
        if(!validateDate(value, pattern)) {
          return false;
        }
        break;
    }
  }

  return true;
};

validateFormula = function(value) {
  return value.search(/^(\$|=|[^:=]+:)(.+)$/) !== -1;
};

validateString = function(value) {
  return true;
};

validateInteger = function(value) {
  return value.search(/^[+-]?\d+$/) !== -1;
};

validateNumeric = function(value) {
  return value.search(/^[+-]?((\d*\.)?\d+([eE][+-]?\d+)?)$/) !== -1;
};

validateDate = function(value, pattern) {
  var date;
  pattern = translatePattern(pattern);

  if(pattern === null) {
    return true; //couldn't convert to javascript pattern. will not verify date
  }

  try {
    date = $.datepicker.parseDate(pattern, value);
    return !isNaN(date.getTime());
  } catch(error) {
    return false;
  }


};

translatePattern = function( pattern ) {

  if(stringContains(pattern, "w") || stringContains(pattern, "W") || stringContains(pattern, "F")) {
    return null;
  }

  //Day of year
  if(stringContains(pattern, "D")) {
    pattern = pattern.replace("D", "o");
  }

  //Day Name
  if(stringContains(pattern, "EEEE")) {
    pattern = pattern.replace("EEEE", "DD");
  } else if(stringContains(pattern, "EEE")) {
    pattern = pattern.replace("EEE", "D");
  }

  //Day of month - do nothing
  if(stringContains(pattern, "MMMM")) {
    pattern = pattern.replace("MMMM", "MM");
  } else if(stringContains(pattern, "MMM")) {
    pattern = pattern.replace("MMM", "M");
  } else if(stringContains(pattern, "MM")) {
    pattern = pattern.replace("MM", "mm");
  }

  //Year
  if(stringContains(pattern, "yyyy") || stringContains(pattern, "YYYY")) {
    pattern = pattern.replace("yyyy", "yy").replace("YYYY", "yy");
  } else if(stringContains(pattern, "yyy") || stringContains(pattern, "YYY")) {
    pattern = pattern.replace("yyy", "yy").replace("YYY", "yy");
  } else if(stringContains(pattern, "y") || stringContains(pattern, "Y")) {
    pattern = pattern.replace("y", "yy").replace("Y", "yy");
  } else if(stringContains(pattern, "yy") || stringContains(pattern, "YY")) {
    pattern = pattern.replace("yy", "y").replace("YY", "y");
  }

  return pattern;



};

stringContains = function(string, subString) {
  return string.indexOf(subString) > -1;
};

getParams = function() {
  var params = {};
  $('#parameterHolder input').each(function(index, param) {
    params['param' + $(param).attr('id')] = $(param).val();
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

var inputFocus = function(event, input, isValid) {
  var $parent = input.parent();

  input.addClass("cdaButtonSelected");

  if(isValid) {
    $parent.find("div.helpButton").show();
  } else {
    $parent.find("div.errorButton").show();
  }
};

var inputBlur = function(event) {
  // we need to delay this evaluation ever so slightly, so that
  // we don't hide the help button before it registers the click
  var $myself = $(this);
  setTimeout(
      function() {
        $myself.removeClass("cdaButtonSelected");
        $myself.parent().find("div.helpButton").hide();
        $myself.parent().find("div.errorButton").hide();
      },
      125);
};

helpPopup = function() {
  $("#help").jqmShow();
};

errorButton = function() {
  $("#invalidInput").jqmShow();
};
