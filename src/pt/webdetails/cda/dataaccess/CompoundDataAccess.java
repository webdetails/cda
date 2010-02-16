package pt.webdetails.cda.dataaccess;

import javax.swing.table.TableModel;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 16, 2010
 * Time: 11:36:05 PM
 */
public abstract class CompoundDataAccess extends AbstractDataAccess
{
  public CompoundDataAccess(final Element element)
  {
    super(element);
  }

  public void closeDataSource() throws QueryException
  {
    // not needed
  }
}
