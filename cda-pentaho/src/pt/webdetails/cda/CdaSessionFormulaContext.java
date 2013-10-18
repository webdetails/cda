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

package pt.webdetails.cda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;

/**
 * Provides access to Pentaho parameter providers for formula reference resolution,
 *  accessible as [&lt;prefix&gt:&lt;parameter_name&gt;]<br><br>
 * Available prefixes are:
 * <ul>
 * <li> security: {@link SecurityParameterProvider} </li>
 * <li> session: {@link PentahoSessionParameterProvider}</li>
 * <li> system: {@link SystemSettingsParameterProvider} </li>
 * </ul>
 * 
 */
public class CdaSessionFormulaContext extends DefaultFormulaContext {

    Map<String, IParameterProvider> providers;
    private static final String SECURITY_PREFIX = "security:";
    private static final String SESSION_PREFIX = "session:";
    private static final String SYSTEM_PREFIX = "system:";


    public CdaSessionFormulaContext(IPentahoSession session)
    {
      providers = new HashMap<String, IParameterProvider>();
      if (session != null)
      {
        providers.put(SECURITY_PREFIX, new SecurityParameterProvider(session));
        providers.put(SESSION_PREFIX, new PentahoSessionParameterProvider(session));
      }
      providers.put(SYSTEM_PREFIX, new SystemSettingsParameterProvider());
    }
    
    public CdaSessionFormulaContext()
    {
      this(PentahoSessionHolder.getSession());
    }


    @Override
    public Object resolveReference(final Object name)
    {
      if (name instanceof String)
      {
        String paramName = ((String) name).trim();
        for (String prefix : providers.keySet())
        {
          if (paramName.startsWith(prefix))
          {
            paramName = paramName.substring(prefix.length());
            Object value = providers.get(prefix).getParameter(paramName);
            if (value instanceof JavaScriptResultSet)
            {//needs special treatment, convert to array
              JavaScriptResultSet resultSet = (JavaScriptResultSet) value;
              return convertToArray(resultSet);
            }
            return value;
          }
        }
      }
      return super.resolveReference(name);
    }


    private Object[] convertToArray(final JavaScriptResultSet resultSet)
    {
      List<Object> result = new ArrayList<Object>();
      for (int i = 0; i < resultSet.getRowCount(); i++)
      {
        for (int j = 0; j < resultSet.getColumnCount(); j++)
        {
          result.add(resultSet.getValueAt(i, j));
        }
      }
      return result.toArray();
    }
}

