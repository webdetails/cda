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

package pt.webdetails.cda.settings;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

/**
 *
 * @author pedrovale
 */
public class DefaultResourceKeyGetter implements IResourceKeyGetter {

  private static Log logger = LogFactory.getLog(DefaultResourceKeyGetter.class);

  @Override
  public ResourceKey getResourceKey(String id, ResourceManager resourceManager) throws ResourceKeyCreationException {
    IUserContentAccess reader = CdaEngine.getRepo().getUserContentAccess("/");
    // XXX why are we creating the key from the file contents? why not go straight for the path?
    // XXX this doesn't work in pentaho, does it work at all? 
    try {
      if (reader.fileExists(id) && reader.hasAccess(id, FileAccess.READ)) {
        return resourceManager.createKey( (Util.toString(reader.getFileInputStream(id))) );
      }
    }
    catch (IOException ioe) {
      logger.error( "Couldn't get file contents for resource key " + id, ioe);
    }
    // TODO: we need a resourceloader that can deal with repository relative
    // FIXME this doesn't work
    return resourceManager.createKey(id);
  }

}
