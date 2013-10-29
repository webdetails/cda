package pt.webdetails.cda.settings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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

import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * first draft for a cda resource loader
 */
public class CdaResourceLoader implements ResourceLoader {

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

    private ResourceKey key;
    private IReadAccess reader;
    private String path;

    public CdaResourceData ( ResourceKey key, IReadAccess reader, String path ) {
      this.key = key;
      this.reader = reader;
      this.path = path;
    }

    public InputStream getResourceAsStream( ResourceManager caller ) throws ResourceLoadingException {
      try {
        return reader.getFileInputStream( path );
      } catch ( IOException e ) {
        throw new ResourceLoadingException( e.getLocalizedMessage(), e );
      }
    }

    public Object getAttribute( String key ) {
      if ( StringUtils.isEmpty( key ) ) {
        return null;
      }
      if ( key.equals( ResourceData.FILENAME ) ) {
        return reader.fetchFile( path ).getName();
      }
      if ( key.equals( ResourceData.CONTENT_TYPE ) ) {
        return MimeTypes.getMimeTypeFromExt( reader.fetchFile( path ).getExtension() );
      }
      return null;
    }

    public ResourceKey getKey() {
      return key;
    }

    public long getVersion( ResourceManager caller ) throws ResourceLoadingException {
      return reader.getLastModified( path );
    }

  }
}
