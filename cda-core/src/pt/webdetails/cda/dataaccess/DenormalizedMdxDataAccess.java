/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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

  protected AbstractNamedMDXDataFactory createDataFactory() {
    return new ExtDenormalizedMDXDataFactory();
  }

  public String getType() {
    return "denormalizedMdx";
  }

  @Override
  protected Serializable getExtraCacheKey() { //TODO: is this necessary after role assembly in EvaluableConnection
    // .evaluate()?
    MondrianConnectionInfo mci;
    try {
      mci = ( (AbstractMondrianConnection) getCdaSettings().getConnection( getConnectionId() ) ).getConnectionInfo();
    } catch ( Exception e ) {
      logger.error( "Failed to get a connection info for cache key" );
      mci = null;
    }

    CacheKey cacheKey = getCacheKey() != null ? ( (CacheKey) getCacheKey() ).clone() : new CacheKey();

    cacheKey.addKeyValuePair( "roles", mci.getMondrianRole() );

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

      if ( this.roles == null ? other.roles != null : !this.roles.equals( other.roles ) ) {
        return false;
      }
      return true;
    }


    private void readObject( java.io.ObjectInputStream in ) throws IOException, ClassNotFoundException {
      this.roles = (String) in.readObject();
    }


    private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
      out.writeObject( this.roles );
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
