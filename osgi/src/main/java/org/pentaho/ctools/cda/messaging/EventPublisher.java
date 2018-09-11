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
