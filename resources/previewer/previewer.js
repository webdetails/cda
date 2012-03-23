pageParams = function() {
        var url = document.location.href;
        var output = {};

        // The parameters are in the section between '?' and '#' (if any)
        var startIndex = url.indexOf('?');
        var endIndex = url.indexOf('#');
        var params = (endIndex > 0) ? url.slice(startIndex+1,endIndex).split('&'):url.slice(startIndex+1).split('&');
        for (param in params){
            var p = params[param].split('=');
            output[p[0]] = decodeURIComponent(p[1]);
        }
        return output;
}

refreshTable = function(id){
  // Detect whether the change was triggered by a refresh or a change in DataAccessId
  if (id != lastQuery) {
    // When we change query, we must drop the table and parameters, and rebuild both
    lastQuery = id;
    refreshParams(id);
    $.getJSON("doQuery",{path:filename, dataAccessId: id},showTable);
  } else {
    // Same query, we need to get the present parameter values and rebuild the table
    var params = getParams();
    params.path = filename;
    params.dataAccessId = id;
    params.outputIndexId = $('#outputIndexId').val();
    $.getJSON("doQuery",params, showTable);
  }
};

showTable = function(data){
  var tableContents = ignoreNullRows(data.resultset);
  var columnNames = [];
  for (column in data.metadata) {
    columnNames.push({"sTitle": data.metadata[column].colName});
  }
  $('#previewerTable').empty();
  $('#previewerTable').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="contents"></table>' );
  if (oLanguage == 'undefined'){
    tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames});
  }
  else {
    tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames,"oLanguage":oLanguage});
  }
};

ignoreNullRows = function(table){
  var cleanTable = [];
  if(table != null){
    for(var i=0;i<table.length;i++){
      if(table[i] != null){
        cleanTable.push(table[i]);
      }
      else if(console && console.error){
        console.error("row #" + i + " is null");
      }
    }
  }
  return cleanTable;
};

exportFunc = function(id){
  // Detect whether the was triggered by a refresh or a change in DataAccessId
  if (id != lastQuery) {
    // When we change query, we must rebuild parameters
    lastQuery = id;
    refreshParams(id);
    
    var url = document.location.href;
    
    var newWindow = window.open(url.replace('previewQuery', 'doQuery') + '&' + $.param({dataAccessId: id, outputType: "xls"}), 'CDA Export');
  } else {
    // Same query, we need to get the present parameter values and rebuild the table
    var params = getParams();

    params.dataAccessId = id;
    params.outputType = "xls";

    var url = document.location.href;
    
    var newWindow = window.open(url.replace('previewQuery', 'doQuery') + '&' + $.param(params), 'CDA Export');
  }	
};


refreshParams = function(id) {
  $.getJSON("listParameters",{path:filename, dataAccessId: id},function(data){
    var placeholder = $('#parameterHolder');
    placeholder.empty();
    for (param in data.resultset) {
      placeholder.append('<div class="param span-8 last"><div class="span-4">'+data.resultset[param][0]+
        ':&nbsp;</div><div class="cdaInputWrapper span-4 last"><input class="cdaButton cdaButtonShort" id="'+data.resultset[param][0]+
        '" value="'+data.resultset[param][2]+'"' + ((data.resultset[param][4] == 'private')? ' readonly="readonly"' : '')+ '"><div class="helpButton">?</div></div></div>');
    }
    placeholder.find("div.helpButton").click(helpPopup).hide();
    placeholder.find("input").focus(inputFocus).blur(inputBlur);
  });

};

getParams = function() {
  var params = {};
  $('#parameterHolder input').each(function(index,param){
    params['param' +$(param).attr('id')] = $(param).val()
  });
  return params;
}
var filename = function(){
  var params = pageParams();
  return params.solution != undefined?
    (params.solution + '/' +params.path + '/' + params.file).replace(/\+/g," ").replace(/\/\//g,"/")
  : params.path}();

cacheThis = function() {

    var queryDefinition = {};
    var params = [];
    $('#parameterHolder input').each(function(index,param){
        params.push({name: $(param).attr('id'), value: $(param).val()});
    });
    queryDefinition.parameters = params;
    queryDefinition.cdaFile = filename
    queryDefinition.dataAccessId = $('#dataAccessSelector').val();
    queryDefinition.cronString = $('#cron').val() || simpleCron();
    var json = JSON.stringify(queryDefinition);
    var notification = $('#dialog .notification');
    if (!notification.length) {
      notification = $("<span class='notification'></span>").appendTo('.dialogAction');
    }
    $.getJSON("cacheController",{method: "change", "object": json}, function(response){
      if (response.status == 'ok') {
        notification.text('');
        $("#dialog").jqmHide();
      } else {
        notification.text(response.message);
      }
    });
};


periodicity = [
    {name: "every week", granularity: "day of the week (1-7)", cron: "0 0 0 ? * x"},
    {name: "every day", granularity: "hour (0-23)", cron: "0 0 x * * ?"},
    {name: "every hour", granularity: "minute (0-59)", cron: "0 x * * * ?"},
    {name: "every minute", granularity: "second (0-59)",cron: "x * * * * ?"}
];

toggleAdvanced = function(advanced){
    if(advanced==false) {
        var selector = "<select id='periodType'>";
        for (option in periodicity) {
            selector += "<option value='"+option+"'>"+periodicity[option].name+"</option>"
        }
        selector += '</select>';
        contents =$( '<span>'+selector+'<span>on the</span><input id="startAt"><span id="granularity">th '+periodicity[0].granularity +'</span></span>');
        contents.find('select#periodType').change(function(){
            $(this).parent().find('#granularity')
                .text(periodicity[$(this).attr('value')].granularity);
        });
    } else {
        contents ='<span>Cron Expression: <input id="cron" placeholder="0 */10 * * * ? "></input></span>';
    }

    $("#dialogInput").empty().append(contents);
    $("#dialog .notification").text('');
    $("p.dialogTitle .dialogToggle")
      .html(advanced?"(basic)":"(advanced)")
      .attr("href","javascript:;")
      .click(function(){
        toggleAdvanced(!advanced);
      }
    );
};

simpleCron = function() {
    var selector = document.getElementById('periodType');
    if(selector !== null) {
        var val = $(selector).attr("value"),
          startAt = $("#startAt").attr("value"),
          period = periodicity[val],
          cronExpression;

        cronExpression = period.cron.replace('x', startAt);
        return cronExpression;
    }
};

var inputFocus = function(event){
    $(this).attr("style","width:130px"); 
    $(this).parent().find("div.helpButton").show();
};

var inputBlur = function(event){
    // we need to delay this evaluation ever so slightly, so that
    // we don't hide the help button before it registers the click
    var myself = this;
    setTimeout(
        function(){
            $(myself).attr("style","");
            $(myself).parent().find("div.helpButton").hide();
        },
        100);
};
helpPopup = function(){
$("#help").jqmShow();
}

