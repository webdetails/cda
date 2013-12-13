var CdaCacheScheduler = {
  
  listEntries: function( callback ) {
    $.get(CacheManagerBackend.CACHE_SCHEDULER + '/list', {}, callback, 'json');
  },
  deleteEntry: function( id, callback ) {
    $.post(CacheManagerBackend.CACHE_SCHEDULER + '/delete', {}, callback, 'json');
  },
  listQueries: function( callback ) {
    
  },
  
  
};