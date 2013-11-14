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

  protected String getResourceAsString(final String path, FileAccess access) throws IOException, AccessDeniedException {
    IUserContentAccess repo = CdaEngine.getRepo().getUserContentAccess("/");
    if ( repo.hasAccess(path, access) ){
      return Util.toString( repo.getFileInputStream( path ) );
    }
    else
    {
      throw new AccessDeniedException(path, null);
    }
  }

  protected String getResourceAsString(final String path) throws IOException, AccessDeniedException {
    return getResourceAsString(path, FileAccess.READ);
  }

}
