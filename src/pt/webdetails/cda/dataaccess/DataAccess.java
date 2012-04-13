package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import org.dom4j.Element;
import pt.webdetails.cda.xml.DomVisitor;

/**
 * DataAccess interface
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:44:01 PM
 */
public interface DataAccess
{

  enum OutputMode
  {
	  
    INCLUDE, EXCLUDE
  };

  String getId();

  String getName();

  String getType();

  DataAccessEnums.ACCESS_TYPE getAccess();

  /**
   * @deprecated use {@link #isCacheEnabled()}
   */
  boolean isCache();
  
  boolean isCacheEnabled();
  
  int getCacheDuration();

  CdaSettings getCdaSettings();

  void setCdaSettings(CdaSettings cdaSettings);

  TableModel doQuery(QueryOptions queryOptions) throws QueryException;

  ColumnDefinition getColumnDefinition(int idx);

  ArrayList<ColumnDefinition> getCalculatedColumns();
  
  ArrayList<ColumnDefinition> getColumnDefinitions();;

  ArrayList<Integer> getOutputs();
  
  ArrayList<Integer> getOutputs(int id);

  OutputMode getOutputMode();
  
  OutputMode getOutputMode(int id);

  TableModel listParameters(DiscoveryOptions discoveryOptions);

  void storeDescriptor(DataAccessConnectionDescriptor descriptor);
  
  void setQuery(String query);
  
  void accept(DomVisitor v, Element ele);

}
