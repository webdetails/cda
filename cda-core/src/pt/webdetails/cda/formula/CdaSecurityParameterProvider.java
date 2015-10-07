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

package pt.webdetails.cda.formula;

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

public class CdaSecurityParameterProvider implements ICdaParameterProvider {

  private ISessionUtils sessionUtils;

  private static String principalName = "principalName";
  private static String principalRoles = "principalRoles";
  private static String systemRoleNames = "systemRoleNames";
  private static String systemUserNames = "systemUserNames";

  public CdaSecurityParameterProvider( ISessionUtils sessionUtils ) {
    this.sessionUtils = sessionUtils;
  }

  @Override
  public Object getParameter( String name ) {
    if ( name != null ) {
      IUserSession session = sessionUtils.getCurrentSession();
      if ( session != null ) {
        if ( principalName.equals( name ) ) {
          return session.getUserName();
        } else if ( principalRoles.equals( name ) ) {
          return convertArray( session.getAuthorities() );
        }
      } else if ( systemUserNames.equals( name ) ) {
        return convertArray( sessionUtils.getSystemPrincipals() );
      } else if ( systemRoleNames.equals( name ) ) {
        return convertArray( sessionUtils.getSystemAuthorities() );
      }
    }
    return null;
  }

  private String convertArray( Object[] array ) {
    if ( array != null && array.length > 0 ) {
      String output = "";
      for ( int i = 0; i < array.length; i++ ) {
        output += ( i > 0 ? "," + array[ i ] : array[ i ] );
      }
      return output;
    }
    return null;
  }
}
