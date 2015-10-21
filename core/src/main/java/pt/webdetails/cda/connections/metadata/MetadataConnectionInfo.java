/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
