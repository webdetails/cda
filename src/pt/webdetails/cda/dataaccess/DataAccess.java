package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;

/**
 * DataAccess interface
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:44:01 PM
 */
public interface DataAccess {

  public String getId();

  public DataAccessEnums.ACCESS_TYPE getAccess();
 
  public boolean isCache();

  public int getCacheDuration();

  public TableModel queryData();


}