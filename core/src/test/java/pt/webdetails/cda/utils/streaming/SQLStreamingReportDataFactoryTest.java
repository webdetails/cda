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


package pt.webdetails.cda.utils.streaming;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinPreparedStatement;
import org.pentaho.di.trans.dataservice.jdbc.api.IThinStatement;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;

import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SQLStreamingReportDataFactoryTest {
  private SQLStreamingReportDataFactory dataFactory;
  private ConnectionProvider connectionProvider;
  private Statement statement;
  private final IDataServiceClientService.StreamingMode windowMode = IDataServiceClientService.StreamingMode.ROW_BASED;
  private final long windowSize = 100;
  private final long windowEvery = 0;
  private final long windowLimit = 1000;
  private String query = "SELECT * FROM \"sales4\"";
  private String[] preparedParameterNames = new String[0];

  @Before
  public void setUp() {
    connectionProvider = mock( ConnectionProvider.class );
    dataFactory = new SQLStreamingReportDataFactory( connectionProvider, windowMode, windowSize, windowEvery,
            windowLimit );
    statement = mock( IThinStatement.class );
  }

  @Test
  public void testQueryExecution() throws SQLException {
    dataFactory.performQuery( statement, query, preparedParameterNames );
    verify( (IThinStatement) statement ).executeQuery( query, windowMode, windowSize, windowEvery,
            windowLimit );
  }

  @Test
  public void testPreparedStatementIsExecuted() throws SQLException {
    preparedParameterNames = new String[]{"some prepared parameter"};
    statement = mock( IThinPreparedStatement.class );
    dataFactory.performQuery( statement, query, preparedParameterNames );
    verify( (IThinPreparedStatement) statement ).executeQuery( windowMode, windowSize, windowEvery, windowLimit );
  }
}
