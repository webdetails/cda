/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import pt.webdetails.cda.cache.CacheKey;
import pt.webdetails.cda.connections.mondrian.AbstractMondrianConnection;
import pt.webdetails.cda.connections.mondrian.MondrianConnectionInfo;
import pt.webdetails.cda.utils.mondrian.ExtDenormalizedMDXDataFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 */
public class DenormalizedMdxDataAccess extends GlobalMdxDataAccess {

  private static final Log logger = LogFactory.getLog( DenormalizedMdxDataAccess.class );

  public DenormalizedMdxDataAccess( final Element element ) {
    super( element );
  }

  public DenormalizedMdxDataAccess() {
  }

  @Override
  protected AbstractNamedMDXDataFactory createDataFactory() {
    return new ExtDenormalizedMDXDataFactory();
  }

  public String getType() {
    return "denormalizedMdx";
  }

  @Override
  protected CacheKey getExtraCacheKey() {
    // .evaluate()?
    MondrianConnectionInfo mci;
    try {
      mci = ( (AbstractMondrianConnection) getCdaSettings().getConnection( getConnectionId() ) ).getConnectionInfo();
    } catch ( Exception e ) {
      logger.error( "Failed to get a connection info for cache key" );
      mci = null;
    }

    CacheKey cacheKey = getCacheKey() != null ? ( getCacheKey() ).clone() : new CacheKey();

    if ( mci != null) {
      cacheKey.addKeyValuePair( "roles", mci.getMondrianRole() );
    } else {
      logger.warn( "No Mondrian connection info was found for cache key, roles were not included." );
    }
    return cacheKey;
  }

  protected static class ExtraCacheKey implements Serializable {

    private static final long serialVersionUID = 1L;
    private String roles;


    public ExtraCacheKey( String roles ) {
      this.roles = roles;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( obj == null ) {
        return false;
      }
      if ( getClass() != obj.getClass() ) {
        return false;
      }
      final ExtraCacheKey other = (ExtraCacheKey) obj;

      return Objects.equals( this.roles, other.roles );
    }


    private void readObject( java.io.ObjectInputStream in ) throws IOException, ClassNotFoundException {
      this.roles = in.readUTF();
    }


    private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
      out.writeUTF( this.roles );
    }


    @Override
    public int hashCode() {
      int hash = 7;
      hash = 83 * hash + ( this.roles != null ? this.roles.hashCode() : 0 );
      return hash;
    }

    @Override
    public String toString() {
      return this.getClass().getName() + "[roles:" + roles + "]";
    }
  }
}
