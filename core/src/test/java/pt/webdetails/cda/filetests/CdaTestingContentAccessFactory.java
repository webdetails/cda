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

package pt.webdetails.cda.filetests;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.repository.impl.FileBasedResourceAccess;

/**
 * Content access factory used in test cases.
 */
public class CdaTestingContentAccessFactory implements IContentAccessFactory {

  public static final String DEFAULT_REPOSITORY = "repository";

  File baseSystem;
  File baseRepository;

  public CdaTestingContentAccessFactory() {
    // assumes test resources were copied to classpath
    File base = new File(
      CdaTestingContentAccessFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
    baseSystem = base;
    baseRepository = new File( base, DEFAULT_REPOSITORY );
  }

  public IUserContentAccess getUserContentAccess( String basePath ) {
    return new TestRepositoryAccess( new File( baseRepository, basePath ) );
  }

  public IReadAccess getPluginRepositoryReader( String basePath ) {
    return new TestRepositoryAccess( new File( baseRepository, basePath ) );
  }

  public IRWAccess getPluginRepositoryWriter( String basePath ) {
    return new TestRepositoryAccess( new File( baseRepository, basePath ) );
  }

  public IReadAccess getPluginSystemReader( String basePath ) {
    return new TestRepositoryAccess( new File( baseSystem, basePath ) );
  }

  public IRWAccess getPluginSystemWriter( String basePath ) {
    return new TestRepositoryAccess( new File( baseSystem, basePath ) );
  }

  public IReadAccess getOtherPluginSystemReader( String pluginId, String basePath ) {
    throw new UnsupportedOperationException();
  }

  public IRWAccess getOtherPluginSystemWriter( String pluginId, String basePath ) {
    throw new UnsupportedOperationException();
  }

  public String toString() {
    return getClass().getName() + ": " + "system= '" + baseSystem + "', repo='" + baseRepository + "'";
  }

  public static class TestRepositoryAccess extends FileBasedResourceAccess implements IUserContentAccess {

    private final File baseDir;

    public TestRepositoryAccess( File baseDir ) {
      this.baseDir = baseDir;
    }

    public TestRepositoryAccess( String path ) {
      this.baseDir = new File( FilenameUtils.separatorsToUnix( path ) );
    }

    protected File getFile( String path ) {
      return new File( Util.joinPath( FilenameUtils.separatorsToUnix( baseDir.getPath() ), path ) );
    }

    public boolean hasAccess( String filePath, FileAccess access ) {
      return true;
    }

  }

}
