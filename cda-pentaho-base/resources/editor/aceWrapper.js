//URLs filled by backend
var ExternalEditor = {
  EXT_EDITOR: null,
  CAN_EDIT_URL: null,
  GET_FILE_URL: null,
  SAVE_FILE_URL: null,
  STATUS: {
   OK: "ok",
   ERROR: "error"
  },
  EXT_EDITOR: null,
};

//ACE wrapper
var CodeEditor = function() {
 return {
  MODES : {
    JAVASCRIPT: 'javascript',
    CSS: 'css',
    XML: 'xml'
  },
  MODE_BASE : 'ace/mode/',
  DEFAULT_MODE: 'text',

  modeMap :
  { //highlight modes
    'css' : 'css',
    'javascript' : 'javascript',
    'js' : 'javascript',
    'xml' : 'xml',
    'cda' : 'xml',
    'cdv' : 'javascript',
    'html': 'html',
    'sql' : 'text',
    'mdx' : 'text'
  },

  mode: 'javascript',
  theme: 'ace/theme/textmate',
  editor: null,
  editorId: null,

  initEditor: function(editorId){
	this.editor = ace.edit(editorId); 
	this.editorId = editorId;
	this.setMode(null);
	this.setTheme(null);

  },
	
  loadFile: function(fileName){
      var myself = this;
      //check edit permission
      $.getJSON(ExternalEditor.CAN_EDIT_URL, {path: fileName},
	function(result){
	    var readonly = !result.result;
	    myself.setReadOnly(readonly);
	    //TODO: can read?..get permissions?...

	    //load file contents
	    $.get(ExternalEditor.GET_FILE_URL,{path:fileName},
	      function(response) {
		myself.setContents(response.result); //TODO response.result if response.status==ok
	      }
	    );
	}
      );
  },

  setContents: function(contents){
      this.editor.getSession().setValue(contents);
      $('#' + this.editorId).css("font-size","12px");
      //this.editor.gotoLine(2);
      //document.getElementById('codeArea').style.fontSize='12px';

      //this.editor.navigateFileStart();
  },

  saveFile: function(fileName, contents, callback, errorCallback){
    $.ajax({
	url: ExternalEditor.SAVE_FILE_URL,
	type: "POST",
//	contentType: "application/json",
	dataType: "json",
	data: { path: fileName, data: contents },
	success: function(data){
	    if(typeof callback == 'function'){
		callback(data);
	    }
	},
	error: function(data){
	    if(typeof errorCallback == 'function'){
		errorCallback(data);
	    }
	}
    });
  },
  
  getContents: function(){
	  return this.editor.getSession().getValue();
  },
  
  setMode: function(mode)
  {
    this.mode = this.modeMap[mode];

    if(this.mode == null){
      this.mode = this.DEFAULT_MODE;
    }

    if(this.editor != null)
    {
	    if(this.mode != null){
		    var HLMode = ace.require(this.MODE_BASE + this.mode).Mode;
		    this.editor.getSession().setMode(new HLMode());
	    }
    }
  },
  
  setTheme: function(themePath){
	  this.editor.setTheme((themePath == null || themePath == undefined) ? this.theme : themePath);
	  
  },
  
  setReadOnly: function(readOnly){
	  if(readOnly == this.editor.getReadOnly()){ return; }
	  else{ this.editor.setReadOnly(readOnly); }
  },
  
  isReadOnly: function(){
	  return this.editor.getReadOnly();
  },
  
  insert: function(text){
	  this.editor.insert(text);
  },
  
  getEditor: function(){
	  return this.editor;
  },
  
  onChange: function(callback){
	  this.editor.getSession().on('change', callback);
  }
 }//return new
};
