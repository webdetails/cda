package pt.webdetails.cda.exporter;

import java.io.OutputStream;

import javax.swing.table.TableModel;

public interface TableExporter extends Exporter {
	public void export(OutputStream out, final TableModel tableModel) throws ExporterException;
}
