/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cda.settings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyUtils;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.loader.AbstractResourceData;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * CDA Resource Loader. TBC
 */
public abstract class CdaFileResourceLoader implements ICdaResourceLoader {

  private static final Log logger = LogFactory.getLog( CdaFileResourceLoader.class );
  private final String name;
  private final String schema;


  public CdaFileResourceLoader( String name ) {
    assert name != null;
    this.name = name;
    this.schema = getClass().getName() + ":" + name;
  }

  protected abstract IReadAccess getReader();

  protected abstract IACAccess getAccessControl();

  public boolean hasReadAccess( String id ) {
    return getAccessControl().hasAccess( id, FileAccess.READ );
  }

  public long getLastModified( String id ) {
    return getReader().getLastModified( id );
  }

  public boolean isSupportedKey( ResourceKey key ) {
    return schema.equals( key.getSchema() );
  }

  public boolean isValidId( String id ) {
    return getReader().fileExists( id );
  }

  @SuppressWarnings( "rawtypes" )
  public ResourceKey createKey( Object value, Map factoryKeys ) throws ResourceKeyCreationException {
    if ( value instanceof String ) {
      if ( StringUtils.isEmpty( (String) value ) ) {
        throw new ResourceKeyCreationException( "Empty key." );
      }
      if ( logger.isDebugEnabled() ) {
        logger.debug( "creating key for " + value );
      }
      return new ResourceKey( schema, value, factoryKeys );
    }
    return null; // let another deal with it
  }

  @SuppressWarnings( "rawtypes" )
  public ResourceKey deriveKey( ResourceKey parent, String path, Map factoryKeys ) throws ResourceKeyCreationException {
    assert isSupportedKey( parent );
    if ( path == null ) {
      return parent;
    }
    String basePath = parent.getIdentifierAsString();
    assert basePath != null;
    String newPath = Util.joinPath( FilenameUtils.getPath( basePath ), path );
    if ( logger.isDebugEnabled() ) {
      logger.debug( String.format( "deriveKey: parent='%s', path='%s' --> '%s'  ", basePath, path, newPath ) );
    }
    return new ResourceKey( schema, newPath, factoryKeys );
  }

  public ResourceData load( ResourceKey key ) throws ResourceLoadingException {
    assert key.getSchema().equals( schema );
    return new CdaResourceData( name, key, getReader(), key.getIdentifierAsString() );
  }

  public URL toURL( ResourceKey key ) {
    return null;
  }

  public boolean isSupportedDeserializer( String data ) {
    return schema.equals( ResourceKeyUtils.readSchemaFromString( data ) );
  }

  public String serialize( ResourceKey bundleKey, ResourceKey key ) throws ResourceException {
    return ResourceKeyUtils.createStringResourceKey(
      key.getSchema().toString(),
      key.getIdentifierAsString(),
      key.getFactoryParameters() );
  }

  public ResourceKey deserialize( ResourceKey bundleKey, String stringKey ) throws ResourceKeyCreationException {
    ResourceKeyData keyData = ResourceKeyUtils.parse( stringKey );
    if ( !schema.equals( keyData.getSchema() ) ) {
      throw new ResourceKeyCreationException( "Wrong schema" );
    }
    return new ResourceKey( schema, keyData.getIdentifier(), keyData.getFactoryParameters() );
  }

  public String toString() {
    return schema;
  }

  protected static class CdaResourceData extends AbstractResourceData implements ResourceData {
    private static final long serialVersionUID = 1L;
    private static final long NO_VERSION = -1;
    private ResourceKey key;
    private byte[] contents;
    private String fileName;
    private String mimeType;
    private String path;
    private String resourceLoaderName;

    public CdaResourceData( String resourceLoader, ResourceKey key, IReadAccess reader, String path )
      throws ResourceLoadingException {
      this.key = key;
      this.path = path;
      IBasicFile file = reader.fetchFile( path );
      if ( file == null ) {
        String msg = String.format( "Unable to fetch '%s'.", path );
        logger.error( msg );
        throw new ResourceLoadingException( msg );
      }
      InputStream in = null;
      try {
        in = file.getContents();
        this.contents = IOUtils.toByteArray( in );
      } catch ( IOException e ) {
        throw new ResourceLoadingException( e.getLocalizedMessage(), e );
      } finally {
        IOUtils.closeQuietly( in );
      }
      this.mimeType = MimeTypes.getMimeTypeFromExt( file.getExtension() );
      this.fileName = file.getName();
      this.resourceLoaderName = resourceLoader;
    }

    public InputStream getResourceAsStream( ResourceManager caller ) throws ResourceLoadingException {
      return new ByteArrayInputStream( contents );
    }

    public Object getAttribute( String key ) {
      if ( StringUtils.isEmpty( key ) ) {
        return null;
      }
      if ( key.equals( ResourceData.FILENAME ) ) {
        return fileName;
      }
      if ( key.equals( ResourceData.CONTENT_TYPE ) ) {
        return mimeType;
      }
      return null;
    }


    public ResourceKey getKey() {
      return key;
    }

    public long getVersion( ResourceManager caller ) throws ResourceLoadingException {
      // getVerson has to be always up-to-date
      ICdaResourceLoader loader =
        CdaEngine.getInstance().getSettingsManager().getResourceLoader( resourceLoaderName );
      long version = ( loader != null ) ? loader.getLastModified( path ) : NO_VERSION;
      // long version = CdaEngine.getRepo().getUserContentAccess( null ).getLastModified( path );
      return ( version == 0 ) ? NO_VERSION : version;
    }

  }
}
