package pt.webdetails.cda.exporter;

import javax.swing.table.TableModel;
import java.io.OutputStream;

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
