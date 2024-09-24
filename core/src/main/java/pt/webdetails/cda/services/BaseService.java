/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.services;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

// TODO just impl aid, to be changed
public abstract class BaseService {

  protected Log getLog() {
    return LogFactory.getLog( getClass() );
  }

  protected String getPluginId() {
    return "cda";
  }

  protected IReadAccess getSystemPath( String path ) {
    return PluginEnvironment.env().getContentAccessFactory().getPluginSystemReader( path );
  }

  protected String getResourceAsString( final String path, FileAccess access ) throws IOException,
    AccessDeniedException {
    IUserContentAccess repo = CdaEngine.getRepo().getUserContentAccess( "/" );
    if ( repo.hasAccess( path, access ) ) {
      return Util.toString( repo.getFileInputStream( path ) );
    } else {
      throw new AccessDeniedException( path, null );
    }
  }

  protected String getResourceAsString( final String path ) throws IOException, AccessDeniedException {
    return getResourceAsString( path, FileAccess.READ );
  }

}
