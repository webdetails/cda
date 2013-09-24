/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda;

import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.utils.framework.PluginUtils;


public class PentahoCdaEnvironment extends DefaultCdaEnvironment {

  public PentahoCdaEnvironment() throws InitializationException {
    super();
  }
  
  
  private IQueryCache cacheImpl;
  
  
  //This is kept here for legacy reasons. CDC is writing over plugin.xml to 
  //switch cache types. It should be changed to change the cda.spring.xml.
  //While we don't, we just keep the old method for getting the cache
  @Override  
  public IQueryCache getQueryCache() {
    try {
      if (cacheImpl == null)
        cacheImpl = PluginUtils.getPluginBean("cda.", IQueryCache.class);
      return cacheImpl;
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    return super.getQueryCache();
  }
}
