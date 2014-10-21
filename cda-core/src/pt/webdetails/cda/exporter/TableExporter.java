package pt.webdetails.cda.exporter;

import javax.swing.table.TableModel;
import java.io.OutputStream;

public interface TableExporter extends Exporter {
	public void export(OutputStream out, final TableModel tableModel) throws ExporterException;
}
