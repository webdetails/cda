package pt.webdetails.cda;

import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.utils.framework.PluginUtils;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public abstract class PentahoBaseCdaEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {
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

  public IContentAccessFactory getRepo() {
    return CdaPluginEnvironment.repository();
  }

  /**
   * @return {@link CdaSessionFormulaContext}
   */
  @Override
  public FormulaContext getFormulaContext() {
    return new CdaSessionFormulaContext();
  }
}
