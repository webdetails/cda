package pt.webdetails.cda.services;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

// TODO just impl aid, to be changed
public abstract class BaseService {


//  private static final Map<String,String> NO_TOKENS = Collections.<String,String>emptyMap();

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

//  protected String getResourceAsString(final String path, FileAccess access, Locale locale) throws IOException, AccessDeniedException {
//    String resource = getResourceAsString(path, access);
//    if (locale != null) {
//      resource = StringUtils.replace(resource, LOCALE_TOKEN, locale.toLanguageTag());
//    }
//    return resource;
//  }

  protected String getResourceAsString(final String path) throws IOException, AccessDeniedException {
    return getResourceAsString(path, FileAccess.READ);
  }

  protected String getResourceAsString(final String path, final Map<String, String> tokens) throws IOException, AccessDeniedException
  {
    // Read file
    String resourceContents = getResourceAsString(path);
    
    // Make replacement of tokens
    if (tokens != null)
    {
      for (final String key : tokens.keySet())
      { 
        resourceContents = StringUtils.replace(resourceContents, key, tokens.get(key));
      }
    }
    return resourceContents;
  }
}
