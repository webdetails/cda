package pt.webdetails.cda.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import javax.swing.table.TableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cda.utils.MetadataTableModel;

/**
 * JsonExporter
 * <p/>
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:07:12 PM
 */
public class JsonExporter extends AbstractExporter
{

  public JsonExporter(HashMap<String, String> extraSettings)
  {
    super();
  }


  public void export(final OutputStream out, final TableModel tableModel) throws ExporterException
  {

    try
    {
      JSONObject json = getTableAsJson(tableModel);

      try
      {
        out.write(json.toString().getBytes("UTF-8"));
      }
      catch (IOException e)
      {
        throw new ExporterException("IO Exception converting to utf-8", e);
      }

    }
    catch (JSONException e)
    {
      throw new ExporterException("JSONException building object", e);
    }


  }
  
  public JSONObject getTableAsJson(TableModel tableModel) throws JSONException, ExporterException {
    JSONObject json = new JSONObject();

    // Generate metadata
    final JSONObject queryInfo = new JSONObject();
    final JSONArray metadataArray = new JSONArray();

    final int columnCount = tableModel.getColumnCount();
    final int rowCount = tableModel.getRowCount();

    for (int i = 0; i < columnCount; i++)
    {
      JSONObject info = new JSONObject();
      info.put("colIndex", i);
      info.put("colName", tableModel.getColumnName(i));
      info.put("colType", getColType(tableModel.getColumnClass(i)));
      metadataArray.put(info);
    }
    json.put("metadata", metadataArray);

    if (tableModel instanceof MetadataTableModel)
    {
      json.put("queryInfo", ((MetadataTableModel)tableModel).getAllMetadata());
    }
    final JSONArray valuesArray = new JSONArray();
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
    {

      final JSONArray rowArray = new JSONArray();
      valuesArray.put(rowArray);
      for (int colIdx = 0; colIdx < columnCount; colIdx++)
      {
        rowArray.put(tableModel.getValueAt(rowIdx, colIdx));
      }
    }
    json.put("resultset", valuesArray);
    return json;
  }


  public String getMimeType()
  {
    return "application/json";
  }


  public String getAttachmentName()
  {
    // No attachment required
    return null;
  }
}
