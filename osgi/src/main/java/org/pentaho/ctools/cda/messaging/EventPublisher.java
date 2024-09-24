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
package org.pentaho.ctools.cda.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.messaging.IEventPublisher;
import pt.webdetails.cpf.messaging.PluginEvent;

/**
 * Dummy class for support
 */
public class EventPublisher implements IEventPublisher {

  protected static Log logger = LogFactory.getLog( EventPublisher.class );


  @Override
  public void publish( PluginEvent event ) {
    logger.debug( "Event: " + event.getKey() + " : " + event.getName() + "\n" + event.toString() );
  }
}
