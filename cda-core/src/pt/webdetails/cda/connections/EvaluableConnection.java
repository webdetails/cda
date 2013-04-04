package pt.webdetails.cda.connections;

/**
 * A connection with fields that need to be evaluated in some form prior to execution.<br> 
 * Needs to be stored in pre-evaluated form in settings cache but evaluated before being used in a table cache key or query execution.  
 */
public interface EvaluableConnection {

  /**
   * @return Clone of this connection with all evaluable fields evaluated.
   */
  public Connection evaluate();
  
}
