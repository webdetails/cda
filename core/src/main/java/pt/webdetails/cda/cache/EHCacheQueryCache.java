/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.swing.table.TableModel;

import mondrian.olap.InvalidArgumentException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.cache.monitor.CacheElementInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;
import pt.webdetails.cda.cache.monitor.ExtraCacheStats;

public class EHCacheQueryCache implements IQueryCache {

  private static final Log logger = LogFactory.getLog( EHCacheQueryCache.class );
  private static final String CACHE_NAME = "pentaho-cda-dataaccess";
  private static final String CACHE_CFG_FILE = "ehcache-cda.xml";
  private static final String CACHE_CFG_FILE_DIST = "ehcache-dist.xml";
  private static final String ENABLE_SHUTDOWN_HOOK_PROPERTY = "javax.cache.CacheManager.enableShutdownHook";
  private static final String USE_TERRACOTTA_PROPERTY = "pt.webdetails.cda.UseTerracotta";
  private static CacheManager cacheManager;

  private static class CacheElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private TableModel table;
    private ExtraCacheInfo info;
    private ExtraCacheStats stats;

    public CacheElement( TableModel table, ExtraCacheInfo info, ExtraCacheStats stats ) {
      this.table = table;
      this.info = info;
      this.stats = stats;
    }

    public TableModel getTable() {
      return table;
    }

    public ExtraCacheInfo getInfo() {
      return info;
    }

    public ExtraCacheStats getStats() {
      return stats;
    }

    private void writeObject(ObjectOutputStream out ) throws IOException {
      if ( table instanceof Serializable ) {
        out.writeObject( table );
      } else {
        throw new IOException("Cannot call writeObject on TableModel (object is not serializable)");
      }
      out.writeObject( info );
      out.writeObject( stats );
    }

    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
      table = (TableModel) in.readObject();
      info = (ExtraCacheInfo) in.readObject();
      stats = (ExtraCacheStats) in.readObject();
    }

  }

  protected static synchronized Cache getCacheFromManager( final boolean switchClassLoader ) throws CacheException {
    if ( cacheManager == null ) {
      // 'new CacheManager' used instead of 'CacheManager.create' to avoid overriding default cache
      final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        if ( switchClassLoader ) {
          Thread.currentThread().setContextClassLoader( EHCacheQueryCache.class.getClassLoader() );
        }
        boolean useTerracotta =
          Boolean.parseBoolean( CdaEngine.getInstance().getConfigProperty( USE_TERRACOTTA_PROPERTY ) );
        String configFilePath = useTerracotta ? CACHE_CFG_FILE_DIST : CACHE_CFG_FILE;

        InputStream configFile = null;
        try {
          configFile = CdaEngine.getRepo().getPluginSystemReader( "" ).getFileInputStream( configFilePath );
          cacheManager = Caching.getCachingProvider().getCacheManager();
          logger.debug( "Cache started using " + configFilePath );
        } catch ( IOException ioe ) {
          logger.error( "Error reading " + configFilePath );
        } finally {
          IOUtils.closeQuietly( configFile );
        }

        // enable clean shutdown so ehcache's diskPersistent attribute can work
        if ( !useTerracotta ) {
          enableCacheProperShutdown( true );
        }
      } finally {
        if ( switchClassLoader ) {
          Thread.currentThread().setContextClassLoader( contextClassLoader );
        }
      }
    }

    if ( cacheManager == null ) {
      logger.error( "Cache manager is null" );
      return null;
    }

    if ( cacheManager.getCache( CACHE_NAME ) == null ) {
      MutableConfiguration<Object,Object> configuration =
        new MutableConfiguration<>().setStoreByValue( false );
      cacheManager.createCache( CACHE_NAME , configuration );
    }

    return cacheManager.getCache( CACHE_NAME );
  }

  private static void enableCacheProperShutdown( final boolean force ) {
    if ( !force ) {
      try {
        System.getProperty( ENABLE_SHUTDOWN_HOOK_PROPERTY );
        return; // unless force, ignore if already set
      } catch ( NullPointerException npe ) {
        // key null, continue
      } catch ( InvalidArgumentException iae ) {
        // key not there, continue
      } catch ( SecurityException se ) {
        // no permissions to set
        return;
      }
    }
    System.setProperty( ENABLE_SHUTDOWN_HOOK_PROPERTY, "true" );
  }

  Cache cache = null;

  public EHCacheQueryCache( final Cache cache ) {
    this.cache = cache;
  }

  public EHCacheQueryCache() {
    this( getCacheFromManager( true ) );
  }

  public EHCacheQueryCache( boolean switchClassLoader ) {
    this( getCacheFromManager( switchClassLoader ) );
  }

  public void putTableModel( TableCacheKey key, TableModel table, int ttlSec, ExtraCacheInfo info ) {
    // EhCache2 use to provide the stats per cache entry. JCache/EhCache3 doesn't support that anymore.
    // So manually updating the Cache element with inserted/updated time
    ExtraCacheStats stats;
    // Quiet retrieval of cache element (Hit count isn't incremented).
    Object object = cache.get( key );
    if ( object == null ) {
      long insertTime = Instant.now().toEpochMilli();
      stats = new ExtraCacheStats( 0, insertTime, insertTime );
    } else {
      stats = ( ( CacheElement ) object).getStats();
      stats.setInsertOrUpdateTime( Instant.now().toEpochMilli() );
    }

    final CacheElement cacheElement = new CacheElement( table, info, stats );
    cache.put( key, cacheElement );
  }

  @Override
  public TableModel getTableModel( TableCacheKey key ) {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try {
      //make sure we have the right class loader in thread to instantiate cda classes in case DiskStore is used
      //TODO: ehcache 2.5 has ClassLoaderAwareCache
      Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
      final Object element = cache.get( key );
      if ( element != null ) {
        // EhCache2 use to provide the stats per cache entry. JCache/EhCache3 doesn't support that anymore.
        // So manually updating the hits and last accessed time when table model is retrieved
        ExtraCacheStats stats = ( (CacheElement) element ).getStats();
        stats.setHits( stats.getHits() + 1 );
        stats.setLastAccessTime( Instant.now().toEpochMilli() );
        final TableModel cachedTableModel = ( (CacheElement) element ).getTable();
        if ( cachedTableModel != null ) {
          if ( logger.isDebugEnabled() ) {
            // we have a entry in the cache ... great!
            logger.debug( "Found tableModel in cache. Returning" );
          }
          return cachedTableModel;
        }
      }
      return null;
    } catch ( Exception e ) {
      logger.error( "Error while attempting to load from cache, bypassing cache (cause: " + e.getClass() + ")", e );
      return null;
    } finally {
      Thread.currentThread().setContextClassLoader( contextCL );
    }
  }

  @Override
  public void clearCache() {
    cache.removeAll();
  }

  public Cache getCache() {
    return this.cache;
  }

  @Override
  public boolean remove( TableCacheKey key ) {
    return cache.remove( key );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public Iterable<TableCacheKey> getKeys() {
    List<TableCacheKey> keys = new ArrayList<>();
    cache.iterator().forEachRemaining( entry -> keys.add( ( ( javax.cache.Cache.Entry<TableCacheKey,?> ) entry).getKey() ) );
    return keys;
  }

  @Override
  public ExtraCacheInfo getCacheEntryInfo( TableCacheKey key ) {
    // Quiet retrieval of cache element.
    Object element = cache.get( key );
    if ( element == null ) {
      logger.warn( "Null element in cache, removing." );
      remove( key );
      return null;
    }
    Object val = element;
    if ( val instanceof CacheElement ) {
      return ( (CacheElement) val ).getInfo();
    } else {
      logger.error( "Expected " + CacheElement.class.getCanonicalName() + ", found "
        + val.getClass().getCanonicalName() + " instead" );
      remove( key );
      return null;
    }
  }

  @Override
  public CacheElementInfo getElementInfo( TableCacheKey key ) {
    // Quiet retrieval of cache element.
    Object element = cache.get( key );
    CacheElementInfo info = new CacheElementInfo();
    info.setKey( key );
    if ( element != null ) {
      Object val = element;
      if ( val instanceof CacheElement ) {
        info.setHits( ( (CacheElement) val ).getStats().getHits() );
        info.setAccessTime( ( (CacheElement) val ).getStats().getLastAccessTime() );
        info.setInsertTime( ( (CacheElement) val ).getStats().getInsertOrUpdateTime() );
        info.setRows( ( (CacheElement) val ).getTable().getRowCount() );
      }
    }
    return info;
  }

  @Override
  public int removeAll( String cdaSettingsId, String dataAccessId ) {
    int deleteCount = 0;

    if ( cdaSettingsId == null ) {
      //deleteCount = cache.getSize();
      clearCache();
    }

    for ( TableCacheKey key : getKeys() ) {
      // Quiet retrieval of cache element.
      ExtraCacheInfo info = ( (CacheElement) cache.get( key ) ).getInfo();

      if ( StringUtils.equals( cdaSettingsId, info.getCdaSettingsId() )
        && ( dataAccessId == null || StringUtils.equals( dataAccessId, info.getDataAccessId() ) ) ) {
        if ( remove( key ) ) {
          deleteCount++;
        }
      }
    }
    return deleteCount;
  }


  @Override
  public void shutdownIfRunning() {
    if ( cacheManager != null ) {
      if ( cache != null ) {
        cache.clear();
      }
      if ( !cacheManager.isClosed() ) {
        logger.debug( "Shutting down cache manager." );
        cacheManager.close();
        cacheManager = null;
      }
    }
  }


}
