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


package pt.webdetails.cda.events;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryTooLongEvent extends CdaEvent {

  private long duration;

  public QueryTooLongEvent( QueryInfo queryInfo, long duration ) throws JSONException {
    super( CdaEventType.QueryTooLong, queryInfo );
    this.duration = duration;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject obj = super.toJSON();
    obj.put( "duration", duration );
    return obj;
  }

}
