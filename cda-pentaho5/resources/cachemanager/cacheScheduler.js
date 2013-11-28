var CdaCacheScheduler = {
  listEntries: function( callback ) {
    $.ajax({
      type: 'GET',
      url: CacheManagerBackend.CACHE_SCHEDULER + '/list',
      data: {},
      success: callback,
      dataType: 'json'
    });
    //$.get(CacheManagerBackend.CACHE_SCHEDULER + '/list', {}, callback, 'json');
  },
  deleteEntry: function( id, callback ) {
    $.ajax({
      type: 'DELETE',
      url: CacheManagerBackend.CACHE_SCHEDULER + '/delete',
      data: {id: id},
      success: callback,
      dataType: 'json'
    });
  },
//   createEntry: function( payload, callback ) {
//     $.ajax({
//       type: 'POST',
//       url: CacheManagerBackend.CACHE_SCHEDULER + '/change',
//       data: payload,
//       success: callback,
//       dataType: 'json'
//     });
//   },
  executeEntry: function( id, callback ) {
    $.ajax({
      type: 'POST',
      url: CacheManagerBackend.CACHE_SCHEDULER + '/execute',
      data: {id: id},
      success: callback,
      dataType: 'json'
    });
  }
};