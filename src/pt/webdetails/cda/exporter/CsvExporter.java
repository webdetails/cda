package pt.webdetails.cda.exporter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.swing.table.TableModel;

/**
 * JsonExporter
 * <p/>
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:07:12 PM
 */
public class CsvExporter extends AbstractExporter
{

  private String separator;
  public static final String newLine = System.getProperty("line.separator");
  private DecimalFormat doubleFormatter = new DecimalFormat("##0.#######");

  public CsvExporter()
  {
    super();
    separator = ";";
  }

  public CsvExporter(String separator)
  {
    super();
    this.separator = separator;
  }

  public void export(final OutputStream out, final TableModel tableModel) throws ExporterException
  {


    final int columnCount = tableModel.getColumnCount();
    final int rowCount = tableModel.getRowCount();
    PrintWriter pw = new PrintWriter(out);

    for (int i = 0; i < columnCount; i++)
    {
      pw.append(tableModel.getColumnName(i) + separator);
    }
    pw.append(newLine);

    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
    {
      for (int colIdx = 0; colIdx < columnCount; colIdx++)
      {
        String value = tableModel.getValueAt(rowIdx, colIdx) == null ? null : tableModel.getValueAt(rowIdx, colIdx).toString();
        if (value == null)
        {
          // nothing
        }
        else if (isDouble(value))
        {
          pw.append(doubleFormatter.format(Double.parseDouble(value)));
        }
        else
        {
          String aux = value.replaceAll("\"", "\\\\\"");
          pw.append('\"' + aux + '\"');
        }

        if (colIdx != rowCount - 1)
        {
          pw.append(separator);
        }
      }
      pw.append(newLine);
    }
    pw.flush();


  }

  public String getMimeType()
  {
    return "text/csv";
  }

  public boolean isDouble(String obj)
  {
    try
    {
      Double.parseDouble(obj);
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }
}
