package pt.webdetails.cda.services;

import java.io.IOException;

import pt.webdetails.cda.AccessDeniedException;
import pt.webdetails.cpf.repository.api.FileAccess;

/**
 * manageCache entry point
 */
public class CacheManager extends BaseService {

  private static final String CACHE_MANAGER_PATH = "cachemanager/cache.html";

  public String manageCache() throws AccessDeniedException, IOException {
    // TODO FILES
    String path = "system/" + getPluginId() + '/' + CACHE_MANAGER_PATH;

    return getResourceAsString(path, FileAccess.EXECUTE);
  }

//  public String redirectToMonitor() {
//    
//    return null;
//  }
}
