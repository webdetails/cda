package pt.webdetails.cda.exporter;

import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.table.TableModel;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:06:31 PM
 */
public abstract class AbstractExporter implements Exporter
{


  public abstract void export(final OutputStream out, final TableModel tableModel) throws ExporterException;

  public abstract String getMimeType();


  protected String getColType(final Class<?> columnClass) throws ExporterException
  {

    if (columnClass.equals(String.class))
    {
      return "String";
    }
    else if (columnClass.equals(Integer.class) || columnClass.equals(Short.class))
    {
      return "Integer";
    }
    else if (columnClass.equals(Number.class) || columnClass.equals(Long.class) || columnClass.equals(Double.class) || columnClass.equals(Float.class) )
    {
      return "Numeric";
    }
    else if (columnClass.equals(Date.class) || columnClass.equals(java.sql.Date.class) || columnClass.equals(Timestamp.class) )
    {
      return "Date";
    }
    else if (columnClass.equals(Object.class) )
    {
      // todo: Quick and dirty hack, as the formula never knows what type is returned. 
      return "String";
    }
    else{

      throw new ExporterException("Unknown class: " + columnClass.toString(), null);

    }

  }

}
