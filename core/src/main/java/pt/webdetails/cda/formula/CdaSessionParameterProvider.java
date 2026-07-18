/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cda.formula;

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

public class CdaSessionParameterProvider implements ICdaParameterProvider {

  private ISessionUtils sessionUtils;

  public CdaSessionParameterProvider( ISessionUtils sessionUtils ) {
    this.sessionUtils = sessionUtils;
  }

  @Override
  public Object getParameter( String name ) {
    IUserSession session = sessionUtils.getCurrentSession();
    if ( session != null ) {
      return session.getParameter( name );
    }
    return null;
  }
}
