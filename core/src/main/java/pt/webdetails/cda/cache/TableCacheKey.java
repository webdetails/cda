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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.dataaccess.Parameter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TableCacheKey implements Serializable {

  private static final long serialVersionUID = 5L; //5: hazelcast version

  private int connectionHash;
  private String query;
  private String queryType;
  private Parameter[] parameters;

  private CacheKey extraCacheKey;

  /**
   * For serialization
   */
  protected TableCacheKey() {
  }


  public TableCacheKey( final Connection connection, final String query,
                        final List<Parameter> parameters, final CacheKey extraCacheKey ) {
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
    this.parameters = parameters.toArray( new Parameter[ parameters.size() ] );
    sortParameters( this.parameters );
    this.extraCacheKey = extraCacheKey;
  }

  public TableCacheKey( final Connection connection, final String query, final String queryType,
                        final List<Parameter> parameters, final CacheKey extraCacheKey ) {
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
    this.parameters = parameters.toArray( new Parameter[ parameters.size() ] );
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


  public void setExtraCacheKey( CacheKey extraCacheKey ) {
    this.extraCacheKey = extraCacheKey;
  }

  //Hazelcast will use serialized version to perform comparisons and hashcodes
  private void readObject( java.io.ObjectInputStream in ) throws IOException, ClassNotFoundException {
    //to be hazelcast compatible needs to serialize EXACTLY the same
    //binary comparison/hash will be used

    //connection
    connectionHash = in.readInt();
    //query
    query = in.readUTF();
    //queryTpe
    queryType = in.readUTF();

    int len = in.readInt();
    Parameter[] params = new Parameter[ len ];

    for ( int i = 0; i < params.length; i++ ) {
      Parameter param = new Parameter();
      param.readObject( in );
      params[ i ] = param;
    }
    parameters = params;
    extraCacheKey = (CacheKey) in.readObject();
  }

  //Hazelcast will use serialized version to perform comparisons and hashcodes
  private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
    //to be hazelcast compatible needs to serialize EXACTLY the same
    //binary comparison/hash will be used

    out.writeInt( connectionHash );
    out.writeUTF( query );
    out.writeUTF( queryType );

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
    throws IOException {
    ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
    ObjectOutputStream objStream = new ObjectOutputStream( keyStream );
    cacheKey.writeObject( objStream );
    return new String( Base64.encodeBase64( keyStream.toByteArray() ), StandardCharsets.UTF_8 );
  }

  /**
   * @see TableCacheKey#getTableCacheKeyAsString(TableCacheKey)
   */
  public static TableCacheKey getTableCacheKeyFromString( String encodedCacheKey )
    throws IOException, ClassNotFoundException {
    ByteArrayInputStream keyStream = new ByteArrayInputStream( Base64.decodeBase64( encodedCacheKey.getBytes() ) );

    // We add this filter to allow only the specific classes that will be read further ahead,
    // and block everything else from readObject. This is important because readObject can allow injection attacks.
    ObjectInputFilter paramFilter = ObjectInputFilter.Config.createFilter(
      "pt.webdetails.cda.dataaccess.Parameter;"
        + "pt.webdetails.cda.dataaccess.Parameter$Type;"
        + "pt.webdetails.cda.cache.CacheKey;"
        + "javax.swing.table.TableModel;"
        + "pt.webdetails.cda.cache.monitor.ExtraCacheInfo;"
        + "pt.webdetails.cda.dataaccess.MdxDataAccess$BANDED_MODE;"
        + "!*"
    );
    ObjectInputStream objStream = new FilteredObjectInputStream( keyStream, paramFilter );
    TableCacheKey cacheKey = new TableCacheKey();
    cacheKey.readObject( objStream );
    return cacheKey;
  }

  // We set the filter in this custom class, because it cannot be applied after read operations have already happened,
  // which was the case when using the normal constructor and applying the filter after class instantiation.
  private static class FilteredObjectInputStream extends ObjectInputStream {
    FilteredObjectInputStream( InputStream in, ObjectInputFilter filter ) throws IOException {
      super( in );
      setObjectInputFilter( filter );
    }
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
    if ( !Objects.equals( query, that.query ) ) {
      return false;
    }
    return Objects.equals( extraCacheKey, that.extraCacheKey );
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
    ArrayList<Parameter> parameters = new ArrayList<>();
    if ( row != null ) {
      for ( String name : row.getColumnNames() ) {
        Object value = row.get( name );
        Parameter param = new Parameter( name, value );
        Parameter.Type type = Parameter.Type.inferTypeFromObject( value );
        param.setType( type );
        parameters.add( param );
      }
    }
    Parameter[] params = parameters.toArray( new Parameter[ parameters.size() ] );
    //so comparisons will not fail when parameters are added in different order
    sortParameters( params );
    return params;
  }

}
