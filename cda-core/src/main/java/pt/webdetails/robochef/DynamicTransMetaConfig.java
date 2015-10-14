/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.robochef;

import static org.pentaho.di.core.Const.isEmpty;
import static org.pentaho.di.core.Const.trim;

import java.io.File;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.TransMeta;

/**
 * Configuration parameters for constructing the TransMeta object that drives the Kettle Transformation. You can
 * construct a TransMeta in three ways: <ul> <li>Type.XML_FILE, <path to filename> -- Kettle will read the .ktr file and
 * configure the transformation <li>Type.XML_STRING, <xml data> -- Kettle will parse the XML string and configure the
 * transformation <li>Type.REPOSITORY, <repository path>, <Repository object> -- Kettle will connect to the repository
 * and configure the transformation at the specified path
 *
 * @author Daniel Einspanjer
 */
public class DynamicTransMetaConfig {
  public enum Type {
    EMPTY, XML_FILE, XML_STRING, REPOSITORY
  }

  private final TransMeta transMeta;

  public DynamicTransMetaConfig( final Type type, final String name, final String configDataSource,
                                 final RepositoryConfig rc ) throws KettleException {
    if ( type == null ) {
      throw new IllegalArgumentException( "Type is null" );
    }
    if ( isEmpty( trim( name ) ) ) {
      throw new IllegalArgumentException( "Name is null" );
    }

    switch( type ) {
      case EMPTY:
        transMeta = new TransMeta();
        transMeta.setRepository( connectToRepository( rc ) );
        break;
      case XML_FILE:
        transMeta = new TransMeta( configDataSource, connectToRepository( rc ) );
        break;
      case XML_STRING:
        transMeta =
          new TransMeta( XMLHandler.getSubNode( XMLHandler.loadXMLString( configDataSource ), "transformation" ),
            connectToRepository( rc ) );
        break;
      case REPOSITORY:
        if ( rc == null ) {
          throw new IllegalArgumentException( "Type.REPOSITORY must have RepositoryConfig object" );
        }
        final Repository rep = connectToRepository( rc );
        final File transPath = new File( configDataSource );
        if ( isEmpty( transPath.getName() ) ) {
          throw new IllegalArgumentException( "Type.REPOSITORY configDataSource must have path to transformation" );
        }
        RepositoryDirectoryInterface directory = rep.loadRepositoryDirectoryTree();
        if ( !isEmpty( transPath.getParent() ) ) {
          directory = directory.findDirectory( transPath.getParent() );
        }
        if ( directory == null ) {
          throw new IllegalArgumentException(
            String.format( "Directory %s not found in repository %s", transPath.getParent(), rc.repositoryName ) );
        }
        transMeta = rep.loadTransformation( transPath.getName(), directory, null, true, rc.version );
      default:
        throw new IllegalArgumentException( String.format( "Unknown Type %s", type ) );
    }

    transMeta.setName( name );
  }

  private Repository connectToRepository( final RepositoryConfig rc ) throws KettleException {
    if ( rc == null ) {
      return null;
    }

    final RepositoriesMeta reppsitoriesMeta = new RepositoriesMeta();
    if ( !reppsitoriesMeta.readData() ) {
      throw new IllegalArgumentException( "No repositories defined" );
    }

    final RepositoryMeta repositoryMeta = reppsitoriesMeta.findRepository( rc.repositoryName );
    if ( repositoryMeta == null ) {
      throw new IllegalArgumentException( String.format( "Repository %s not found", rc.repositoryName ) );
    }
    final Repository rep =
      PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
    rep.init( repositoryMeta );
    rep.connect( rc.userName, rc.password );
    return rep;
  }

  protected TransMeta getTransMeta( final VariableSpace variableSpace ) throws KettleException {
    transMeta.initializeVariablesFrom( variableSpace );
    return transMeta;
  }

  public static class RepositoryConfig {
    public final String repositoryName;
    public final String userName;
    public final String password;
    public final String directory;
    public final String transformation;
    public final String version;

    public RepositoryConfig( final String repositoryName, final String userName, final String password,
                             final String directory, final String transformation, final String version ) {
      super();
      this.repositoryName = repositoryName;
      this.userName = userName;
      this.password = password;
      this.directory = directory;
      this.transformation = transformation;
      this.version = version;
    }

    public static RepositoryConfig get( final String repositoryName, final String userName, final String password,
                                        final String directory, final String transformation, final String version ) {
      if ( isEmpty( trim( repositoryName ) )
        || isEmpty( trim( userName ) )
        || password == null
        || isEmpty( trim( directory ) )
        || isEmpty( trim( transformation ) )
        || isEmpty( trim( version ) ) ) {
        throw new IllegalArgumentException( "Invalid RepositoryConfig" );
      }
      return new RepositoryConfig( repositoryName, userName, password, directory, transformation, version );
    }
  }
}
