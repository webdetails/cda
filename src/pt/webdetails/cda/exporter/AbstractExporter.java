package pt.webdetails.cda.exporter;

import java.io.OutputStream;
import javax.swing.table.TableModel;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:06:31 PM
 */
public abstract class AbstractExporter implements Exporter
{

  public abstract void export(final OutputStream out, final TableModel tableModel);

}
