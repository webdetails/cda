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

package pt.webdetails.cda.connections.xpath;

import org.dom4j.Element;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 13:49:22
 *
 * @author Thomas Morgner.
 */
public class XPathConnectionInfo
{
  private String xqueryDataFile;
  
  public XPathConnectionInfo(final Element connection)
  {
    xqueryDataFile = ((String) connection.selectObject("string(./DataFile)"));
  }

  public String getXqueryDataFile()
  {
    return xqueryDataFile;
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

    final XPathConnectionInfo that = (XPathConnectionInfo) o;

    if (xqueryDataFile != null ? !xqueryDataFile.equals(that.xqueryDataFile) : that.xqueryDataFile != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return xqueryDataFile != null ? xqueryDataFile.hashCode() : 0;
  }
}
