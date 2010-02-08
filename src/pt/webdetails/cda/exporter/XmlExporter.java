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


    Document document = DocumentHelper.createDocument();

    // Generate metadata

    final Element root = document.addElement("CdaExport");

    final Element metadata = root.addElement("MetaData");

    final int columnCount = tableModel.getColumnCount();
    final int rowCount = tableModel.getRowCount();

    for (int i = 0; i < columnCount; i++)
    {
      Element columnInfo = metadata.addElement("ColumnMetaData");
      columnInfo.addAttribute("index", (new Integer(i).toString()));
      columnInfo.addAttribute("type", getColType(tableModel.getColumnClass(i)));
      columnInfo.addAttribute("name", tableModel.getColumnName(i));

    }

    Element resultSet = root.addElement("ResultSet");
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
    {

      Element row = resultSet.addElement("Row");

      for (int colIdx = 0; colIdx < columnCount; colIdx++)
      {
        final Element col = row.addElement("Col");
        // col.setData(tableModel.getValueAt(rowIdx, colIdx));
        col.setText(tableModel.getValueAt(rowIdx, colIdx).toString());

      }
    }

    try
    {
      Writer writer = new BufferedWriter(new OutputStreamWriter(out));

      document.setXMLEncoding("UTF-8");
      document.write(writer);
      writer.flush();
    }
    catch (IOException e)
    {
      throw new ExporterException("IO Exception converting to utf-8", e);
    }

  }
}
