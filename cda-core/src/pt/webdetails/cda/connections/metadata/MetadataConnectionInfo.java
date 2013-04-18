/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.metadata;

import org.dom4j.Element;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:13:50
 *
 * @author Thomas Morgner.
 */
public class MetadataConnectionInfo
{
  private String domainId;
  private String xmiFile;

  public MetadataConnectionInfo(final Element connection)
  {
    domainId = ((String) connection.selectObject("string(./DomainId)"));
    xmiFile = ((String) connection.selectObject("string(./XmiFile)"));
  }
  
  public MetadataConnectionInfo(String domainId, String xmiFile){
  	this.domainId = domainId;
  	this.xmiFile = xmiFile;
  }

  public String getDomainId()
  {
    return domainId;
  }

  public String getXmiFile()
  {
    return xmiFile;
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final MetadataConnectionInfo that = (MetadataConnectionInfo) o;

    if (!domainId.equals(that.domainId))
    {
      return false;
    }
    if (!xmiFile.equals(that.xmiFile))
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = domainId.hashCode();
    result = 31 * result + xmiFile.hashCode();
    return result;
  }
}
