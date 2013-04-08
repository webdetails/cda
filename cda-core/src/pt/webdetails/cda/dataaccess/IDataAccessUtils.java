/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;

import org.pentaho.reporting.engine.classic.core.ReportEnvironmentDataRow;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import pt.webdetails.cda.connections.mondrian.MondrianConnection;

public interface IDataAccessUtils {

  public void setMdxDataFactoryBaseConnectionProperties(MondrianConnection connection, AbstractNamedMDXDataFactory mdxDataFactory);

  public void setConnectionProvider(PmdDataFactory returnDataFactory);

  public ReportEnvironmentDataRow createEnvironmentDataRow(Configuration configuration);
  
}
