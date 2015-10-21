package pt.webdetails.cda.settings;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdaSystemResourceLoader extends CdaFileResourceLoader {

  public CdaSystemResourceLoader( String name ) {
    super( name );
  }

  protected IReadAccess getReader() {
    return CdaEngine.getRepo().getPluginSystemReader( ".." );
  }

  protected IACAccess getAccessControl() {
    return new IACAccess() {
      public boolean hasAccess( String file, FileAccess access ) {
        switch( access ) {
          case EXECUTE:
          case READ:
            return true;
          default:
            return false;
        }
      }
    };
  }
}
