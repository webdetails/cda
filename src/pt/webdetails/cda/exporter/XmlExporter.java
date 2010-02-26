package pt.webdetails.cda.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.table.TableModel;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * JsonExporter
 * <p/>
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:07:12 PM
 */
public class XmlExporter extends AbstractExporter
{

  public XmlExporter()
  {
    super();
  }


  public void export(final OutputStream out, final TableModel tableModel) throws ExporterException
  {


    final Document document = DocumentHelper.createDocument();

    // Generate metadata

    final Element root = document.addElement("CdaExport");

    final Element metadata = root.addElement("MetaData");

    final int columnCount = tableModel.getColumnCount();
    final int rowCount = tableModel.getRowCount();

    for (int i = 0; i < columnCount; i++)
    {
      final Element columnInfo = metadata.addElement("ColumnMetaData");
      columnInfo.addAttribute("index", (String.valueOf(i)));
      columnInfo.addAttribute("type", getColType(tableModel.getColumnClass(i)));
      columnInfo.addAttribute("name", tableModel.getColumnName(i));

    }

    final Element resultSet = root.addElement("ResultSet");
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
    {

      final Element row = resultSet.addElement("Row");

      for (int colIdx = 0; colIdx < columnCount; colIdx++)
      {
        final Element col = row.addElement("Col");

        final Object value = tableModel.getValueAt(rowIdx, colIdx);
        if (value != null)
        {
          col.setText(value.toString());
        }
        else{
          col.addAttribute("isNull","true");
        }

      }
    }

    try
    {
      final Writer writer = new BufferedWriter(new OutputStreamWriter(out));

      document.setXMLEncoding("UTF-8");
      document.write(writer);
      writer.flush();
    }
    catch (IOException e)
    {
      throw new ExporterException("IO Exception converting to utf-8", e);
    }

  }

  public String getMimeType()
  {
    return "text/xml";
  }
}
