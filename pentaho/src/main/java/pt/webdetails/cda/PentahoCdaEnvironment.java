/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda;

import java.util.Locale;
import java.util.TimeZone;

import org.pentaho.platform.util.messages.LocaleHelper;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactoryContext;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class PentahoCdaEnvironment extends PentahoBaseCdaEnvironment implements ICdaEnvironment {

  public PentahoCdaEnvironment() throws InitializationException {
    super();
  }

  public void initializeDataFactory(
    final DataFactory dataFactory,
    final Configuration configuration,
    final ResourceKey contextKey,
    final ResourceManager resourceManager )
    throws ReportDataFactoryException {

    dataFactory.initialize( new DataFactoryContext() {
      public Configuration getConfiguration() {
        return configuration;
      }

      public ResourceManager getResourceManager() {
        return resourceManager;
      }

      public ResourceKey getContextKey() {
        return contextKey;
      }

      public ResourceBundleFactory getResourceBundleFactory() {
        return new LibLoaderResourceBundleFactory( resourceManager, contextKey,
          getLocale(), TimeZone.getDefault() );
      }

      public DataFactory getContextDataFactory() {
        return dataFactory;
      }

      @Override
      public FormulaContext getFormulaContext() {
        return null;
      }
    } );
  }

  @Override
  public Locale getLocale() {
    return LocaleHelper.getLocale();
  }
}
