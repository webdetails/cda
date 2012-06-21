/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.events;

import org.json.JSONException;

public class QueryErrorEvent extends CdaEvent {

  public QueryErrorEvent(QueryInfo queryInfo, Exception e) throws JSONException {
    super(CdaEventType.QueryError, queryInfo);
    getEvent().put("exceptionType", e.getClass().getName());
    getEvent().put("message", e.getMessage());
    getEvent().put("stackTrace", e.getStackTrace());
  }

}
