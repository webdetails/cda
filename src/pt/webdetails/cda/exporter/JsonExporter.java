package pt.webdetails.cda.exporter;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.table.TableModel;

/**
 * JsonExporter
 *
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:07:12 PM
 */
public class JsonExporter extends AbstractExporter
{

  public JsonExporter()
  {
    super();
  }


  public void export(final OutputStream out, final TableModel tableModel)
  {

    String test = "YAYYYYYYYYYYYYYYYY";

    try
    {
      out.write(test.getBytes("utf-8"));
    }
    catch (IOException e)
    {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }
}
