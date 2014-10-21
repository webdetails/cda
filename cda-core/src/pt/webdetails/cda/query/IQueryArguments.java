package pt.webdetails.cda.query;

import pt.webdetails.cda.dataaccess.Parameter;

import java.util.List;

/**
 * Arguments passed to a data access to execute query
 */
public interface IQueryArguments {

  /**
   * If {@link #getPageSize()} and {@link #getPageStart()} should be taken into account
   */
  public boolean isPaginate();

  public int getPageSize();

  public int getPageStart();

  /**
   * get list of sort by directives in the form &lt;column-index&gt;&lt;A|D&gt;
   */
  public List<String> getSortBy();

  /**
   * @return mutable parameters list
   */
  public List<Parameter> getParameters();

  /**
   * get a specific parameter
   * @param name
   * @return null if not found
   */
  public Parameter getParameter( String name );

  /**
   * @return if shouldn't try to fetch from cache
   */
  public boolean isCacheBypass();

}
