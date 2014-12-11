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