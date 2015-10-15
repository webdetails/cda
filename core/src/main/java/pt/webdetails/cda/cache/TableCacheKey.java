/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.cache;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.dataaccess.Parameter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TableCacheKey implements Serializable {

  private static final long serialVersionUID = 5L; //5: hazelcast version

  private int connectionHash;
  private String query;
  private String queryType;
  private Parameter[] parameters;

  private Serializable extraCacheKey;

  /**
   * For serialization
   */
  protected TableCacheKey() {
  }


  public TableCacheKey( final Connection connection, final String query,
                       final List<Parameter> parameters, final Serializable extraCacheKey ) {
    if ( connection == null ) {
      throw new NullPointerException();
    }
    if ( query == null ) {
      throw new NullPointerException();
    }
    if ( parameters == null ) {
      throw new NullPointerException();
    }

    this.connectionHash = connection.hashCode();
    this.query = query;
    this.parameters = parameters.toArray( new Parameter[parameters.size()] );
    sortParameters( this.parameters );
    this.extraCacheKey = extraCacheKey;
  }

  public TableCacheKey( final Connection connection, final String query, final String queryType,
                        final List<Parameter> parameters, final Serializable extraCacheKey ) {
    if ( connection == null ) {
      throw new NullPointerException();
    }
    if ( query == null ) {
      throw new NullPointerException();
    }
    if ( parameters == null ) {
      throw new NullPointerException();
    }

    this.connectionHash = connection.hashCode();
    this.query = query;
    this.queryType = queryType;
    this.parameters = parameters.toArray( new Parameter[parameters.size()] );
    sortParameters( this.parameters );
    this.extraCacheKey = extraCacheKey;
  }

  public int getConnectionHash() {
    return connectionHash;
  }


  public void setConnectionHash( int connectionHash ) {
    this.connectionHash = connectionHash;
  }


  public String getQuery() {
    return query;
  }


  public void setQuery( String query ) {
    this.query = query;
  }

  public String getQueryType() {
    return queryType;
  }

  public void setQueryType( String queryType ) {
    this.queryType = queryType;
  }

//    public ParameterDataRow getParameterDataRow() {
//      return Parameter.createParameterDataRowFromParameters(parameters);
//    }

  public Parameter[] getParameters() {
    return parameters;
  }


  public void setParameterDataRow( ParameterDataRow parameterDataRow ) {
    //this.parameterDataRow = parameterDataRow;
    this.parameters = createParametersFromParameterDataRow( parameterDataRow );
  }


  public Object getExtraCacheKey() {
    return extraCacheKey;
  }


  public void setExtraCacheKey( Serializable extraCacheKey ) {
    this.extraCacheKey = extraCacheKey;
  }

  //Hazelcast will use serialized version to perform comparisons and hashcodes
  private void readObject( java.io.ObjectInputStream in ) throws IOException, ClassNotFoundException {
    //to be hazelcast compatible needs to serialize EXACTLY the same
    //binary comparison/hash will be used

    //connection
    connectionHash = in.readInt();
    //query
    query = (String) in.readObject();
    //queryTpe
    queryType = (String) in.readObject();

    int len = in.readInt();
    Parameter[] params = new Parameter[len];

    for ( int i = 0; i < params.length; i++ ) {
      Parameter param = new Parameter();
      param.readObject( in );
      params[i] = param;
    }
    parameters = params;
    extraCacheKey = (Serializable) in.readObject();
  }

  //Hazelcast will use serialized version to perform comparisons and hashcodes
  private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
    //to be hazelcast compatible needs to serialize EXACTLY the same
    //binary comparison/hash will be used

    out.writeInt( connectionHash );
    out.writeObject( query );
    out.writeObject( queryType );

    out.writeInt( parameters.length );
    for ( Parameter param : parameters ) {
      param.writeObject( out );
    }

    out.writeObject( extraCacheKey );
  }

  /**
   * Serialize as printable <code>String</code>.
   */
  public static String getTableCacheKeyAsString( TableCacheKey cacheKey )
    throws IOException, UnsupportedEncodingException {
    ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
    ObjectOutputStream objStream = new ObjectOutputStream( keyStream );
    cacheKey.writeObject( objStream );
    String identifier = new String( Base64.encodeBase64( keyStream.toByteArray() ), "UTF-8" );
    return identifier;
  }

  /**
   * @see TableCacheKey#getTableCacheKeyAsString(TableCacheKey)
   */
  public static TableCacheKey getTableCacheKeyFromString( String encodedCacheKey )
    throws IOException, ClassNotFoundException {
    ByteArrayInputStream keyStream = new ByteArrayInputStream( Base64.decodeBase64( encodedCacheKey.getBytes() ) );
    ObjectInputStream objStream = new ObjectInputStream( keyStream );
    TableCacheKey cacheKey = new TableCacheKey();
    cacheKey.readObject( objStream );
    return cacheKey;
  }

  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final TableCacheKey that = (TableCacheKey) o;

    if ( connectionHash != that.connectionHash ) {
      return false;
    }
    if ( parameters != null ? !Arrays.equals( parameters, that.parameters ) : that.parameters != null ) {
      return false;
    }
    if ( query != null ? !query.equals( that.query ) : that.query != null ) {
      return false;
    }
    if ( extraCacheKey != null ? !extraCacheKey.equals( that.extraCacheKey ) : that.extraCacheKey != null ) {
      return false;
    }

    return true;
  }


  @Override
  public int hashCode() {
    int result = connectionHash;
    result = 31 * result + ( query != null ? query.hashCode() : 0 );
    result = 31 * result + ( queryType != null ? queryType.hashCode() : 0 );
    result = 31 * result + ( parameters != null ? Arrays.hashCode( parameters ) : 0 );
    result = 31 * result + ( extraCacheKey != null ? extraCacheKey.hashCode() : 0 );
    return result;
  }

  @Override
  public String toString() {
    return
        TableCacheKey.class.getName() + " [" + hashCode() + "]\n"
            + "\tConnectionHash:[" + getConnectionHash() + "]\n"
            + "\tQuery:[" + getQuery() + "]\n"
            + "\tQueryType:[" + getQueryType() + "]\n"
            + "\tParameters: [" + StringUtils.join( getParameters(), ", " ) + "]\n"
            + "\tExtra: [" + getExtraCacheKey() + "]\n";
  }

  private static void sortParameters( Parameter[] params ) {
    Arrays.sort( params, new Comparator<Parameter>() {
      public int compare( Parameter o1, Parameter o2 ) {
        return o1.getName().compareTo( o2.getName() );
      }
    } );
  }

  /**
   * for serialization
   */
  private static Parameter[] createParametersFromParameterDataRow( final ParameterDataRow row ) {
    ArrayList<Parameter> parameters = new ArrayList<Parameter>();
    if  ( row != null ) {
      for ( String name : row.getColumnNames() ) {
        Object value = row.get( name );
        Parameter param = new Parameter( name, value != null ? value : null );
        Parameter.Type type = Parameter.Type.inferTypeFromObject( value );
        param.setType( type );
        parameters.add( param );
      }
    }
    Parameter[] params = parameters.toArray( new Parameter[parameters.size()] );
    //so comparisons will not fail when parameters are added in different order
    sortParameters( params );
    return params;
  }

}
