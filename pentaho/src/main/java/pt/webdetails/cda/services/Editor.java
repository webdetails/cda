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
import java.io.InputStream;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class Editor extends BaseService {

  private static final String EDITOR_PATH = "editor";
  private static final String EDITOR_SOURCE = "editor.html";
  private static final String EXT_EDITOR_SOURCE = "editor-cde.html";
  private static final String CDE = "pentaho-cdf-dd";
  private static Boolean hasCde = null;
  private IContentAccessFactory contentAccess;

  public Editor() {
    this( CdaEngine.getRepo() );
  }

  public Editor( IContentAccessFactory contentAccess ) {
    this.contentAccess = contentAccess;
  }

  public InputStream getEditor( String path ) throws AccessDeniedException, IOException {
    IUserContentAccess repository = getRepository();
    if ( !repository.hasAccess( path, FileAccess.WRITE ) ) {
      throw new AccessDeniedException( "No write access for " + path, null );
    }
    final String editorPath = ( hasCde() ? EXT_EDITOR_SOURCE : EDITOR_SOURCE );
    IReadAccess sysDir = CdaEngine.getRepo().getPluginSystemReader( EDITOR_PATH );
    return sysDir.getFileInputStream( editorPath );
  }

  private static synchronized boolean hasCde() {
    if ( hasCde == null ) {
      IReadAccess cdeDir = CdaEngine.getRepo().getOtherPluginSystemReader( CDE, "" );
      hasCde = cdeDir.fileExists( "." );
    }
    return hasCde;
  }

  /**
   *
   */
  public String getFile( String filePath ) throws AccessDeniedException, IOException {
    return getResourceAsString( filePath );
  }

  public boolean canEdit( String filePath ) {
    return getRepository().hasAccess( filePath, FileAccess.WRITE );
  }

  /**
   * @param repoPath
   * @param fileContents
   * @return
   * @throws AccessDeniedException
   */
  public boolean writeFile( String repoPath, InputStream fileContents ) throws AccessDeniedException {
    IUserContentAccess writer = getRepository();
    if ( !writer.hasAccess( repoPath, FileAccess.WRITE ) ) {
      throw new AccessDeniedException( repoPath, null );
    }
    return writer.saveFile( repoPath, fileContents );
  }

  public boolean writeFile( String repoPath, String fileContents ) throws AccessDeniedException {
    return writeFile( repoPath, Util.toInputStream( fileContents ) );
  }

  /**
   * @param repoPath
   * @return
   * @throws AccessDeniedException
   */
  public boolean deleteFile( String repoPath ) throws AccessDeniedException {
    IUserContentAccess writer = getRepository();
    if ( !writer.hasAccess( repoPath, FileAccess.WRITE ) ) {
      throw new AccessDeniedException( repoPath, null );
    }
    return writer.deleteFile( repoPath );
  }

  private IUserContentAccess getRepository() {
    return contentAccess.getUserContentAccess( "/" );
  }

}
