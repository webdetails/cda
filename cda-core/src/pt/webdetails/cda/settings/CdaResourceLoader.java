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
import org.pentaho.reporting.libraries.resourceloader.ResourceLoader;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.loader.AbstractResourceData;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * CDA Resource Loader. TBC
 */
public class CdaResourceLoader implements ResourceLoader {

  private static final Log logger = LogFactory.getLog(CdaResourceLoader.class);
  private static String SCHEMA = CdaResourceLoader.class.getName();

  private IReadAccess reader;


  public CdaResourceLoader ( IReadAccess reader ) {
    this.reader = reader;
  }

  public boolean isSupportedKey( ResourceKey key ) {
    return SCHEMA.equals( key.getSchema() );
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
      return new ResourceKey( SCHEMA, value, factoryKeys);
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
    return new ResourceKey( SCHEMA, newPath, factoryKeys );
  }

  public ResourceData load( ResourceKey key ) throws ResourceLoadingException {
    assert key.getSchema().equals( SCHEMA );
    return new CdaResourceData( key, reader, key.getIdentifierAsString() );
  }

  public URL toURL( ResourceKey key ) {
    return null;
  }

  public boolean isSupportedDeserializer( String data ) {
    return SCHEMA.equals(ResourceKeyUtils.readSchemaFromString(data));
  }

  public String serialize( ResourceKey bundleKey, ResourceKey key ) throws ResourceException {
    return ResourceKeyUtils.createStringResourceKey(
        key.getSchema().toString(),
        key.getIdentifierAsString(),
        key.getFactoryParameters() );
  }

  public ResourceKey deserialize( ResourceKey bundleKey, String stringKey ) throws ResourceKeyCreationException {
    ResourceKeyData keyData = ResourceKeyUtils.parse(stringKey);
    if ( !SCHEMA.equals( keyData.getSchema() ) ) {
      throw new ResourceKeyCreationException( "Wrong schema" );
    }
    return new ResourceKey( SCHEMA, keyData.getIdentifier(), keyData.getFactoryParameters() );
  }

  protected static class CdaResourceData extends AbstractResourceData implements ResourceData {
    // serializable!
    private static final long serialVersionUID = 1L;
    private ResourceKey key;
    private byte[] contents;
    private String fileName;
    private String mimeType;
    private String path;
//    private long lastModified;

    public CdaResourceData ( ResourceKey key, IReadAccess reader, String path ) throws ResourceLoadingException {
      this.key = key;
//      this.lastModified = reader.getLastModified( path );
      this.path = path;
      IBasicFile file = reader.fetchFile( path );
      if ( file == null ) {
        String msg = String.format( "Unable to fetch '%s'.", path );
        logger.error( msg );
        throw new ResourceLoadingException( msg );
      }
      try {
        this.contents = IOUtils.toByteArray( file.getContents() );
      } catch ( IOException e ) {
        throw new ResourceLoadingException( e.getLocalizedMessage(), e );
      }
      this.mimeType = MimeTypes.getMimeTypeFromExt( file.getExtension() );
      this.fileName = file.getName();
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
      // getVerson has to be always updated
      long version = CdaEngine.getRepo().getUserContentAccess( null ).getLastModified( path );
      return ( version == 0 ) ? -1 : version;
//      return lastModified;
    }

  }
}
