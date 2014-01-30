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
package pt.webdetails.cda.dataaccess;

import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.platform.plugin.connection.PentahoKettleTransFromFileProducer;
import org.pentaho.reporting.platform.plugin.RepositoryResourceLoader;

public class CdaPentahoKettleTransFromFileProducer extends PentahoKettleTransFromFileProducer {
  public CdaPentahoKettleTransFromFileProducer( final String repositoryName,
                                                final String transformationFile,
                                                final String stepName,
                                                final String username,
                                                final String password,
                                                final String[] definedArgumentNames,
                                                final ParameterMapping[] definedVariableNames)
  {
    super( repositoryName, transformationFile, stepName, username, password, definedArgumentNames,
      definedVariableNames );
  }

  @Override
  protected String computeFullFilename( ResourceKey key )
  {
    while ( key != null )
    {
      final Object schema = key.getSchema();
      if ( RepositoryResourceLoader.SOLUTION_SCHEMA_NAME.equals( schema ) == false
        && "pt.webdetails.cda.settings.CdaRepositoryResourceLoader:".equals( schema ) == false )
      {
        // these are not the droids you are looking for ..
        key = key.getParent();
        continue;
      }

      final Object identifier = key.getIdentifier();
      if ( identifier instanceof String )
      {
        // get a local file reference ...
        final String file = (String) identifier;
        // pedro alves - Getting the file through normal apis
        final String fileName = PentahoSystem.getApplicationContext().getSolutionPath( file );
        if ( fileName != null )
        {
          return fileName;
        }
      }
      key = key.getParent();
    }

    return super.computeFullFilename( key );
  }
}

