package pt.webdetails.cda.exporter;

import java.io.OutputStream;

/**
 * Stream exporter interface
 * 
 * @author Michael Spector
 */
public interface StreamExporter extends Exporter {
	public void export(OutputStream out) throws ExporterException;
}
