/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.kettle;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import pt.webdetails.cda.connections.Connection;

/**
 * There should be two implementations for Kettle connections. One for running a *.ktr file and one
 * for running the transformation from the database repository. The database access method is not implemented
 * yet, but scheduled for 3.7-PRD. So lets prepare the field here as well, so that we have to worry less when
 * it finally happens.
 *
 * @author Thomas Morgner.
 */
public interface KettleConnection extends Connection
{
  /**
   *
   * @param query the name of the transformation step that should be polled.
   * @return the initialized transformation producer.
   */
  public KettleTransformationProducer createTransformationProducer(final String query);
}
