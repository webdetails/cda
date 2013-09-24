/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.discovery;

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
