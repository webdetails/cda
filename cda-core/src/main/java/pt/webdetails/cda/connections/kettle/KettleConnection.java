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

package pt.webdetails.cda.connections.kettle;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * There should be two implementations for Kettle connections. One for running a *.ktr file and one
 * for running the transformation from the database repository. The database access method is not implemented
 * yet, but scheduled for 3.7-PRD. So lets prepare the field here as well, so that we have to worry less when
 * it finally happens.
 *
 * @author Thomas Morgner.
 */
public interface KettleConnection extends Connection {
  /**
   *
   * @param query the name of the transformation step that should be polled.
   * @return the initialized transformation producer.
   */
  public KettleTransformationProducer createTransformationProducer( final String query, CdaSettings cdaSettings );
}
