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


package pt.webdetails.cda.formula;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;


public class DefaultSessionFormulaContext extends DefaultFormulaContext {

  private Map<String, ICdaParameterProvider> providers = new HashMap<String, ICdaParameterProvider>();

  public DefaultSessionFormulaContext( Map<String, ICdaParameterProvider> ps ) {
    super();
    if ( ps == null || ps.size() == 0 ) {
      this.providers.put( "system:", new CdaSystemParameterProvider() );
    } else {
      this.providers = ps;
    }
  }

  public void setProviders( Map<String, ICdaParameterProvider> _providers ) {
    if ( _providers != null ) {
      this.providers.putAll( _providers );
    }
  }

  public Map<String, ICdaParameterProvider> getProviders() {
    return providers;
  }

  @Override
  public Object resolveReference( final Object name ) {
    if ( name instanceof String ) {
      String paramName = ( (String) name ).trim();
      for ( String prefix : providers.keySet() ) {
        if ( paramName.startsWith( prefix ) ) {
          paramName = paramName.substring( prefix.length() );
          Object value = providers.get( prefix ).getParameter( paramName );
          return value;
        }
      }
    }
    return super.resolveReference( name );
  }
}
