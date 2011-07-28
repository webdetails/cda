package pt.webdetails.cda.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.dataaccess.InvalidParameterException;
import pt.webdetails.cda.dataaccess.Parameter;

public class TableCacheKey implements Serializable
  {

    private static final long serialVersionUID = 4L; //1->2 only hash of connection kept; 2->3 file/dataAccessId
    
    private int connectionHash;
    private String query;
    private ParameterDataRow parameterDataRow;
    private Object extraCacheKey;
    
    private String cdaSettingsId;
    private String dataAccessId;

    /**
     * For serialization
     */
    protected TableCacheKey()
    {
    }


    public TableCacheKey(final Connection connection, final String query,
            final ParameterDataRow parameterDataRow, final Object extraCacheKey, 
            String cdaSettingsId, String dataAccessId)
    {
      if (connection == null)
      {
        throw new NullPointerException();
      }
      if (query == null)
      {
        throw new NullPointerException();
      }
      if (parameterDataRow == null)
      {
        throw new NullPointerException();
      }

      this.connectionHash = connection.hashCode();
      this.query = query;
      this.parameterDataRow = parameterDataRow;
      this.extraCacheKey = extraCacheKey;
      this.cdaSettingsId = cdaSettingsId;
      this.dataAccessId = dataAccessId;
    }

    
    public int getConnectionHash() {
      return connectionHash;
    }


    public void setConnectionHash(int connectionHash) {
      this.connectionHash = connectionHash;
    }


    public String getQuery() {
      return query;
    }


    public void setQuery(String query) {
      this.query = query;
    }


    public ParameterDataRow getParameterDataRow() {
      return parameterDataRow;
    }


    public void setParameterDataRow(ParameterDataRow parameterDataRow) {
      this.parameterDataRow = parameterDataRow;
    }


    public Object getExtraCacheKey() {
      return extraCacheKey;
    }


    public void setExtraCacheKey(Object extraCacheKey) {
      this.extraCacheKey = extraCacheKey;
    }


    public void setCdaSettingsId(String cdaSettingsId) {
      this.cdaSettingsId = cdaSettingsId;
    }


    public void setDataAccessId(String dataAccessId) {
      this.dataAccessId = dataAccessId;
    }


    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
      //connection
      connectionHash = in.readInt();
      //query
      query = (String) in.readObject();
      //parameterDataRow
      try
      {
        Object holder = in.readObject();
        if (holder != null)
        {
          parameterDataRow = Parameter.createParameterDataRowFromParameters((ArrayList<Parameter>) (ArrayList) holder);
        }
        else
        {
          parameterDataRow = null;
        }
      }
      catch (InvalidParameterException e)
      {
        parameterDataRow = null;
      }
      //extraCacheKey
      extraCacheKey = in.readObject();
      cdaSettingsId = (String) in.readObject();
      dataAccessId = (String) in.readObject();
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
      out.writeInt(connectionHash);
      out.writeObject(query);
      out.writeObject(createParametersFromParameterDataRow(parameterDataRow));
      out.writeObject(extraCacheKey);
      out.writeObject(cdaSettingsId);//information only, not used in hash/equals
      out.writeObject(dataAccessId);//information only, not used in hash/equals
    }
    
    /**
     * Serialize as printable <code>String</code>.
     */
    public static String getTableCacheKeyAsString(TableCacheKey cacheKey) throws IOException, UnsupportedEncodingException {
      ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
      ObjectOutputStream objStream = new ObjectOutputStream(keyStream);
      cacheKey.writeObject(objStream);
      String identifier = new String(Base64.encodeBase64(keyStream.toByteArray()), "UTF-8");
      return identifier;
    }
    
    /**
     * @see TableCacheKey#getTableCacheKeyAsString(TableCacheKey)
     */
    public static TableCacheKey getTableCacheKeyFromString(String encodedCacheKey) throws IOException, UnsupportedEncodingException, ClassNotFoundException {
      ByteArrayInputStream keyStream = new ByteArrayInputStream( Base64.decodeBase64(encodedCacheKey.getBytes()));
      ObjectInputStream objStream = new ObjectInputStream(keyStream);   
      TableCacheKey cacheKey = new TableCacheKey();
      cacheKey.readObject(objStream);
      return cacheKey;
    }

    public boolean equals(final Object o)
    {
      if (this == o)
      {
        return true;
      }
      if (o == null || getClass() != o.getClass())
      {
        return false;
      }

      final TableCacheKey that = (TableCacheKey) o;

      if(connectionHash != that.connectionHash){
        return false;
      }
      if (parameterDataRow != null ? !parameterDataRow.equals(that.parameterDataRow) : that.parameterDataRow != null)
      {
        return false;
      }
      if (query != null ? !query.equals(that.query) : that.query != null)
      {
        return false;
      }
      if (extraCacheKey != null ? !extraCacheKey.equals(that.extraCacheKey) : that.extraCacheKey != null)
      {
        return false;
      }

      return true;
    }


    @Override
    public int hashCode()
    {
      int result = connectionHash;
      result = 31 * result + (query != null ? query.hashCode() : 0);
      result = 31 * result + (parameterDataRow != null ? parameterDataRow.hashCode() : 0);
      result = 31 * result + (extraCacheKey != null ? extraCacheKey.hashCode() : 0);
      return result;
    }
    
    public String getDataAccessId(){
      return this.dataAccessId;
    }
    
    public String getCdaSettingsId(){
      return this.cdaSettingsId;
    }
    
    /**
     * for serialization
     **/
    private static ArrayList<Parameter> createParametersFromParameterDataRow(final ParameterDataRow row)
    {
      ArrayList<Parameter> parameters = new ArrayList<Parameter>();
      if(row != null) for (String name : row.getColumnNames())
      {
        Object value = row.get(name);
        Parameter param = new Parameter (name, value != null ? value : null);
        Parameter.Type type = Parameter.Type.inferTypeFromObject(value);
        param.setType(type);
        parameters.add(param);
      }
      return parameters;
    }
   
}
