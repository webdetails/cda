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
    $.getJSON("doQuery",{path:filename, dataAccessId: id},function(data){
      var tableContents = data.resultset;
      var columnNames = [];
      for (column in data.metadata) {
        columnNames.push({"sTitle": data.metadata[column].colName});
      }
      $('#previewerTable').empty();
      $('#previewerTable').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="contents"></table>' );
      if (oLanguage == 'undefined')
        tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames});
      else
        tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames,"oLanguage":oLanguage});
    });
  } else {
    // Same query, we need to get the present parameter values and rebuild the table
    var params = getParams();
    params.path = filename;
    params.dataAccessId = id;
    $.getJSON("doQuery",params,function(data){
      var tableContents = data.resultset;
      var columnNames = [];
      for (column in data.metadata) {
        columnNames.push({"sTitle": data.metadata[column].colName});
      }
      $('#previewerTable').empty();
      $('#previewerTable').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="contents"></table>' );
      if (oLanguage == 'undefined')
        tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames});
      else
        tableController = $('#contents').dataTable({"aaData": tableContents, "aoColumns": columnNames,"oLanguage":oLanguage});

    });}
};


refreshParams = function(id) {
  $.getJSON("listParameters",{path:filename, dataAccessId: id},function(data){
    var placeholder = $('#parameterHolder');
    placeholder.empty();
    for (param in data.resultset) {
      placeholder.append('<div class="param">'+data.resultset[param][0]+
        ':&nbsp;<input class="cdaButton" id="'+data.resultset[param][0]+
        '" value="'+data.resultset[param][2]+'"></div>');


    }
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
    (params.solution + '/' +params.path + '/' + params.file).replace(/\/\//g,"/")
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
    queryDefinition.cronString = $('#cron').val();
    var json = JSON.stringify(queryDefinition);
    $.getJSON("cacheController",{method: "change", "object": json},function(){$("#dialog").jqmHide();})
}

periodicity = [
    {name: "every week", granularity: "day of the week (1-7)"},
    {name: "every day", granularity: "hour (0-23)"},
    {name: "every hour", granularity: "minute (0-59)"},
    {name: "every minute", granularity: "second (0-59)"}
]

toggleAdvanced = function(advanced){
    var contents = '';
    if(advanced==false) {
        var selector = "<select id='periodType'>";
        for (option in periodicity) {
            selector += "<option value='"+option+">"+periodicity[option].name+"</option>"
        }
        selector += '</select>';
        contents += '<span>'+selector+'</span>';
    } else {
        contents +='<span>: <input></input></span>';
    }

    $("#dialogInput").empty().append(contents);
    $("p.dialogTitle .dialogToggle").html(advanced?"(basic)":"(advanced)").attr("href","javascript:toggleAdvanced("+!advanced+")");
}

updateSelector = function() {
}
