package pt.webdetails.cda.settings;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdaRepositoryResourceLoader extends CdaFileResourceLoader {

  public CdaRepositoryResourceLoader( String name ) {
    super( name );
  }

  protected IReadAccess getReader() {
    return CdaEngine.getRepo().getUserContentAccess( "/" );
  }
  protected IACAccess getAccessControl() {
    return CdaEngine.getRepo().getUserContentAccess( "/" );
  }
}
