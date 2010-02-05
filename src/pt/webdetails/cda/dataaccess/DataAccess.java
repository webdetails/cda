package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

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

  public String getId();

  public DataAccessEnums.ACCESS_TYPE getAccess();

  public boolean isCache();

  public int getCacheDuration();

  public CdaSettings getCdaSettings();

  public void setCdaSettings(CdaSettings cdaSettings);

  public TableModel doQuery(QueryOptions queryOptions) throws QueryException;

  public ArrayList<ColumnDefinition> getColumns();

  public ArrayList<ColumnDefinition> getCalculatedColumns();


}