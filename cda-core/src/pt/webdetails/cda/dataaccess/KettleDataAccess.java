/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.CacheKey;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.kettle.KettleConnection;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.UnknownConnectionException;

import java.io.Serializable;
/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 13:20:39
 *
 * @author Thomas Morgner.
 */
public class KettleDataAccess extends PREDataAccess
{
  
  private String path;

  public KettleDataAccess(final Element element)
  {
    super(element);
  }

  public KettleDataAccess()
  {
    super();
  }

  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {
    final KettleConnection connection = (KettleConnection) getCdaSettings().getConnection( getConnectionId() );

    final KettleDataFactory dataFactory = new KettleDataFactory();
    dataFactory.setQuery("query", connection.createTransformationProducer( getQuery(), getCdaSettings()) );
    return dataFactory;
  }

  public String getType()
  {
    return "kettle";
  }

  @Override
  public ConnectionType getConnectionType()
  {
    return ConnectionType.KETTLE;
  }
  
  @Override 
  public void setCdaSettings(CdaSettings cdaSettings) {
    super.setCdaSettings(cdaSettings);
    final ResourceManager resourceManager = CdaEngine.getInstance().getSettingsManager().getResourceManager();
    ResourceKey fileKey;
    try {
      fileKey = resourceManager.deriveKey(getCdaSettings().getContextKey(), "");
      path = fileKey.getIdentifierAsString();
    } catch (ResourceKeyCreationException e) {
      path = null;//shouldn't happen and will blow down the road
    }
  };

  /**
   * ContextKey is used to resolve the transformation file, and so must be stored in the cache key.
   * We only use solution paths, only the path needs to be stored.
   */
  @Override
  public Serializable getExtraCacheKey(){

    CacheKey cacheKey = getCacheKey() != null ? ( (CacheKey) getCacheKey() ).clone() : new CacheKey();

    cacheKey.addKeyValuePair( "path", path );

    return cacheKey;
  }
}
