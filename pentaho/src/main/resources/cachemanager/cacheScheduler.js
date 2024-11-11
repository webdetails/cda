/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


var CdaCacheScheduler = {
  listEntries: function( callback ) {
    $.ajax({
      type: 'GET',
      url: CacheManagerBackend.CACHE_SCHEDULER + '/list',
      data: {},
      success: callback,
      dataType: 'json'
    });
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