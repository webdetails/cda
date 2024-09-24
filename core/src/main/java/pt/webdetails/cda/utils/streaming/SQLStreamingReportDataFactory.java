/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.utils.streaming;

import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinPreparedStatement;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinStatement;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.SQLReportDataFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLStreamingReportDataFactory extends SQLReportDataFactory {

  private static final long serialVersionUID = 1L;

  private final IDataServiceClientService.StreamingMode windowMode;
  private final long windowSize;
  private final long windowEvery;
  private final long windowLimit;

  protected IDataServiceClientService.StreamingMode getWindowMode() {
    return windowMode;
  }

  protected long getWindowSize() {
    return windowSize;
  }

  protected long getWindowEvery() {
    return windowEvery;
  }

  protected long getWindowLimit() {
    return windowLimit;
  }

  public SQLStreamingReportDataFactory( final ConnectionProvider connectionProvider,
                                        IDataServiceClientService.StreamingMode windowMode,
                                        long windowSize, long windowEvery,
                                        long windowLimit ) {
    super( connectionProvider );
    this.windowMode = windowMode;
    this.windowSize = windowSize;
    this.windowEvery = windowEvery;
    this.windowLimit = windowLimit;
  }

  @Override
  public ResultSet performQuery( Statement statement, final String translatedQuery, final String[] preparedParameterNames )
    throws SQLException {
    final ResultSet res;
    if ( preparedParameterNames.length == 0 ) {
      res = ( (IThinStatement) statement ).executeQuery( translatedQuery, windowMode, windowSize, windowEvery,
              windowLimit );
    } else {
      final IThinPreparedStatement pstmt = (IThinPreparedStatement) statement;
      res = pstmt.executeQuery( windowMode, windowSize, windowEvery, windowLimit );
    }
    return res;
  }

}
