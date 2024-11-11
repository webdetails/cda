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


package pt.webdetails.cda.connections.kettle;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * There should be two implementations for Kettle connections. One for running a *.ktr file and one for running the
 * transformation from the database repository. The database access method is not implemented yet, but scheduled for
 * 3.7-PRD. So lets prepare the field here as well, so that we have to worry less when it finally happens.
 *
 * @author Thomas Morgner.
 */
public interface KettleConnection extends Connection {
  /**
   * @param query the name of the transformation step that should be polled.
   * @return the initialized transformation producer.
   */
  public KettleTransformationProducer createTransformationProducer( final String query, CdaSettings cdaSettings );
}
