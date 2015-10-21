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

package pt.webdetails.cda.connections.scripting;

import org.dom4j.Element;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:22:17
 *
 * @author Thomas Morgner.
 */
public class ScriptingConnectionInfo
{
  private String language;
  private String initScript;

  public ScriptingConnectionInfo(final Element connection)
  {
    language = ((String) connection.selectObject("string(./Language)"));
    initScript = ((String) connection.selectObject("string(./InitScript)"));
  }

  public String getLanguage()
  {
    return language;
  }

  public String getInitScript()
  {
    return initScript;
  }
}
