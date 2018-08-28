/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2018 Object Refinery Ltd, Hitachi Vantara and Contributors..  All rights reserved.
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
