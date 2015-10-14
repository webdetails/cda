/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.connections;

import org.dom4j.Element;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import pt.webdetails.cda.utils.FormulaEvaluator;

public class JndiConnectionInfo {
  private String jndi = "";
  private String user;
  private String pass;
  private String userField;
  private String passwordField;

  public JndiConnectionInfo( final Element connection ) {
    this( (String) connection.selectObject( "string(./Jndi)" ), (String) connection.selectObject( "string(./User)" ),
      (String) connection.selectObject( "string(./Pass)" ), (String) connection.selectObject( "string(./UserField)" ),
      (String) connection.selectObject( "string(./PassField)" ) );
  }

  public JndiConnectionInfo( String jndi, String userName, String password, String userFormula, String passFormula ) {
    this.jndi = jndi;
    if ( !StringUtils.isEmpty( userName ) ) {
      setUser( userName );
    }
    if ( !StringUtils.isEmpty( password ) ) {
      setPass( password );
    }
    if ( !StringUtils.isEmpty( userFormula ) ) {
      setUserField( userFormula );
    }
    if ( !StringUtils.isEmpty( passFormula ) ) {
      setPasswordField( passFormula );
    }
  }

  public String getUser() {
    return FormulaEvaluator.replaceFormula( user );
  }

  public void setUser( final String user ) {
    this.user = user;
  }

  public String getPass() {
    return pass;
  }

  public void setPass( final String pass ) {
    this.pass = pass;
  }

  public String getUserField() {
    return userField;
  }

  public void setUserField( final String userField ) {
    this.userField = userField;
  }

  public String getPasswordField() {
    return passwordField;
  }

  public void setPasswordField( final String passwordField ) {
    this.passwordField = passwordField;
  }

  public String getJndi() {
    return jndi == null ? "" : jndi;
  }

  public void setJndi( String jndi ) {
    this.jndi = jndi == null ? "" : jndi;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final JndiConnectionInfo that = (JndiConnectionInfo) o;

    if ( !StringUtils.equals( jndi, that.jndi ) ) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return getJndi().hashCode();
  }
}
