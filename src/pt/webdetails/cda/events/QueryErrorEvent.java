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
