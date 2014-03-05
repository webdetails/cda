package pt.webdetails.cda.exporter;

import java.io.OutputStream;

import javax.swing.table.TableModel;

public class ExportedTableQueryResult extends ExportedQueryResult {

	private TableModel table;

	public ExportedTableQueryResult(TableExporter exporter, TableModel table) {
		super(exporter);
	    this.table = table;
	}

	public void writeOut(OutputStream out) throws ExporterException {
		((TableExporter)getExporter()).export(out, table);
	}
}
