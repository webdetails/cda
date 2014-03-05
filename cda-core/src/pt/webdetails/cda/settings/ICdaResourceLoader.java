package pt.webdetails.cda.settings;

import org.pentaho.reporting.libraries.resourceloader.ResourceLoader;

public interface ICdaResourceLoader extends ResourceLoader {

  public boolean hasReadAccess( String id );

  public long getLastModified( String id );

  public boolean isValidId( String id );

}
