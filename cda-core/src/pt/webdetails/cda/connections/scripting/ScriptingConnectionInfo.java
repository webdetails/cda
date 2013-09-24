/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
