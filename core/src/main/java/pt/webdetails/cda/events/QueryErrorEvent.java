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

public class QueryErrorEvent extends CdaEvent {

  private Throwable e;

  public QueryErrorEvent( QueryInfo queryInfo, Throwable e ) throws JSONException {
    super( CdaEventType.QueryError, queryInfo );
    this.e = e;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject obj = super.toJSON();
    obj.put( "exceptionType", e.getClass().getName() );
    obj.put( "message", e.getMessage() );
    obj.put( "stackTrace", toStringArray( e.getStackTrace() ) );
    return obj;
  }

  private static String[] toStringArray( final StackTraceElement[] stackTrace ) {
    if ( stackTrace == null ) {
      return null;
    }
    String[] result = new String[ stackTrace.length ];
    for ( int i = 0; i < stackTrace.length; i++ ) {
      result[ i ] = stackTrace[ i ].toString();
    }
    return result;
  }

}
