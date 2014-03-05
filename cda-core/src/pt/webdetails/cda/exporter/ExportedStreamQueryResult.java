package pt.webdetails.cda.exporter;

import java.io.OutputStream;

public class ExportedStreamQueryResult extends ExportedQueryResult {

	public ExportedStreamQueryResult(StreamExporter exporter) {
		super(exporter);
	}

	public void writeOut(OutputStream out) throws ExporterException {
		((StreamExporter)getExporter()).export(out);
	}
}
