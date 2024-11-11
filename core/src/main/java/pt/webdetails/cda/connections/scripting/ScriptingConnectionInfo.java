/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.connections.scripting;

import org.dom4j.Element;


public class ScriptingConnectionInfo {
  private String language;
  private String initScript;

  public ScriptingConnectionInfo( final Element connection ) {
    language = ( (String) connection.selectObject( "string(./Language)" ) );
    initScript = ( (String) connection.selectObject( "string(./InitScript)" ) );
  }

  public String getLanguage() {
    return language;
  }

  public String getInitScript() {
    return initScript;
  }
}
