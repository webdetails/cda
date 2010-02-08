package pt.webdetails.cda.exporter;

import java.io.OutputStream;
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


  protected String getColType(final Class<?> columnClass) throws ExporterException
  {

    if (columnClass.equals(String.class))
    {
      return "String";
    }
    else if (columnClass.equals(Integer.class))
    {
      return "Integer";
    }
    else if (columnClass.equals(Number.class) || columnClass.equals(Long.class) || columnClass.equals(Double.class) || columnClass.equals(Float.class))
    {
      return "Numeric";
    }
    else if (columnClass.equals(Date.class) )
    {
      return "Date";
    }
    else{

      throw new ExporterException("Unknown class: " + columnClass.toString(), null);

    }

  }

}
