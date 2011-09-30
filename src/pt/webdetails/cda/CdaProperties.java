package pt.webdetails.cda;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.dataaccess.MdxDataAccess.BANDED_MODE;

public class CdaProperties {
  
  private static final Log logger = LogFactory.getLog(CdaProperties.class);
  
  
  /**
   * Entries from cda.properties
   */
  public static class Entries {

    private Entries() {}

    public static long getRowProductionTimeout() {
      return getTypedProperty("pt.webdetails.cda.DefaultRowProductionTimeout", new IntParser(), 120);
    }

    public static TimeUnit getRowProductionTimeoutUnit() {
      return getTypedProperty("pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit", new IParser<TimeUnit>() {
        public TimeUnit parse(String val) throws IllegalArgumentException, NullPointerException {
          return TimeUnit.valueOf(val);
        }
      }, TimeUnit.SECONDS);
    }
    
    public static BANDED_MODE getMdxBandedMode(){
      return getTypedProperty("pt.webdetails.cda.BandedMDXMode", new IParser<BANDED_MODE>() {
        public BANDED_MODE parse(String s) throws IllegalArgumentException, NullPointerException {
          return BANDED_MODE.valueOf(s);
        }
      },
      BANDED_MODE.CLASSIC);
    }
    
//    public static CacheType getCacheType(){
//      return getTypedProperty("pt.webdetails.cda.cache.type", new IParser<CacheType>() {
//        public CacheType parse(String s) throws IllegalArgumentException, NullPointerException {
//          return CacheType.valueOf(s);
//        }
//      },
//      CacheType.HAZELCAST);
//    }
    
    public static boolean isHazelcastSuperClient(){
      return false;
      // return getTypedProperty( "pt.webdetails.cda.cache.hazelcast.IsSuperClient", new BoolParser(), false);
    }
  }
  
  public static interface  IParser<T>{
    T parse(String val) throws Exception;
  }
  protected static class IntParser implements IParser<Integer> {
    public Integer parse(String val) throws NumberFormatException {
      return Integer.parseInt(val);
    }
  }
  protected static class BoolParser implements IParser<Boolean> {
    public Boolean parse(String val) throws NumberFormatException {
      return Boolean.parseBoolean(val);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T getTypedProperty(String key, IParser<T> parser, T defaultValue)
  { 
    String value = getStringProperty(key);
    try { 
      if(parser != null) return parser.parse(value);
      else if(defaultValue instanceof String) return (T) value;
      else return null;
    }
    catch(Exception e){
      logger.error(MessageFormat.format("Could not parse {0} in property {1}, using default {2}.", value, key, defaultValue) );
      return defaultValue;
    }
  }
    
  public static String getStringProperty(String key){
    String result = CdaBoot.getInstance().getGlobalConfig().getConfigProperty(key);
    return result;
  }
  
//  protected static String getStringProperty(Entry entry){
//    String result = getStringProperty(entry.getKey());
//    if(result == null) return entry.getDefault();
//    return result;
//  }
  
}
