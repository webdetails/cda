package pt.webdetails.cda.discovery;

import java.util.ArrayList;

import pt.webdetails.cda.dataaccess.Parameter;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 5:25:53 PM
 */
public class DiscoveryOptions
{

  private String dataAccessId;
  private String outputType;

  public DiscoveryOptions()
  {
    outputType = "json";
  }



  public String getDataAccessId()
  {
    return dataAccessId;
  }

  public void setDataAccessId(final String dataAccessId)
  {
    this.dataAccessId = dataAccessId;
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