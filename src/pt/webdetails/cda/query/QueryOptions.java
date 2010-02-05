package pt.webdetails.cda.query;

import java.util.ArrayList;

import pt.webdetails.cda.dataaccess.Parameter;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 5:25:53 PM
 */
public class QueryOptions
{

  private String dataAccessId;
  private boolean paginate;
  private int pageSize;
  private int pageStart;
  private ArrayList<Integer> sortBy;
  private ArrayList<Parameter> parameters;
  private String outputType;

  public QueryOptions()
  {
    paginate = false;
    pageSize = 20;
    pageStart = 0;
    sortBy = new ArrayList<Integer>();
    parameters = new ArrayList<Parameter>();
    outputType = "json";
  }


  public boolean isPaginate()
  {
    return paginate;
  }

  public void setPaginate(final boolean paginate)
  {
    this.paginate = paginate;
  }

  public int getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(final int pageSize)
  {
    this.pageSize = pageSize;
  }

  public int getPageStart()
  {
    return pageStart;
  }

  public void setPageStart(final int pageStart)
  {
    this.pageStart = pageStart;
  }

  public ArrayList<Integer> getSortBy()
  {
    return sortBy;
  }

  public void setSortBy(final ArrayList<Integer> sortBy)
  {
    this.sortBy = sortBy;
  }

  public ArrayList<Parameter> getParameters()
  {
    return parameters;
  }

  public String getDataAccessId()
  {
    return dataAccessId;
  }

  public void setDataAccessId(final String dataAccessId)
  {
    this.dataAccessId = dataAccessId;
  }


  public void addParameter(final String name, final String value)
  {

    final Parameter p = new Parameter(name, value);
    parameters.add(p);

  }

  public Parameter getParameter(final String name)
  {

    for (final Parameter parameter : parameters)
    {
      if (parameter.getName().equals(name))
      {
        return parameter;
      }
    }

    return null;

  }

  public String getOutputType()
  {
    return outputType;
  }

  public void setOutputType(final String outputType)
  {
    this.outputType = outputType;
  }
}
