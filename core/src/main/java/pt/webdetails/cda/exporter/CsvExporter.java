/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.exporter;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

import pt.webdetails.cda.CdaEngine;

public class CsvExporter extends AbstractKettleExporter {

  public static final String CSV_SEPARATOR_SETTING = "csvSeparator";
  public static final String CSV_QUOTE_SETTING = "csvQuote";

  private static final Log logger = LogFactory.getLog( CsvExporter.class );
  private static final String DEFAULT_CSV_SEPARATOR_SETTING = ";";
  private static final String DEFAULT_CSV_ENCLOSURE_SETTING = "\"";
  private String separator;

  private String enclosure;
  private String attachmentName;
  private boolean showColumnHeaders;

  public CsvExporter( Map<String, String> extraSettings ) {
    super( extraSettings );

    this.separator = getSetting(
      CSV_SEPARATOR_SETTING,
      CdaEngine.getInstance().getConfigProperty(
        "pt.webdetails.cda.exporter.csv.Separator", DEFAULT_CSV_SEPARATOR_SETTING ) );

    this.enclosure = getSetting(
      CSV_QUOTE_SETTING,
      CdaEngine.getInstance().getConfigProperty(
        "pt.webdetails.cda.exporter.csv.Enclosure", DEFAULT_CSV_ENCLOSURE_SETTING ) );

    this.attachmentName = getSetting( ATTACHMENT_NAME_SETTING, "cda-export.csv" );

    this.showColumnHeaders = Boolean.parseBoolean( getSetting( COLUMN_HEADERS_SETTING, "true" ) );

    logger.debug( "Initialized CsvExporter with attachement filename '" + attachmentName + "'" );
    logger.debug( "Initialized CsvExporter with enclosure '" + enclosure + "'" );
    logger.debug( "Initialized CsvExporter with show columns '" + showColumnHeaders + "'" );

    logger.debug( "Initialized CsvExporter with separator '" + separator + "'" );

  }

  protected StepMeta getExportStepMeta( String name ) {
    TextFileOutputMeta csvOutputStepMeta = new TextFileOutputMeta();
    csvOutputStepMeta.setOutputFields( new TextFileField[ 0 ] );
    csvOutputStepMeta.setSeparator( this.separator );
    csvOutputStepMeta.setEnclosure( this.enclosure );
    csvOutputStepMeta.setEnclosureForced( true );
    csvOutputStepMeta.setHeaderEnabled( this.showColumnHeaders );
    csvOutputStepMeta.setFooterEnabled( false );
    csvOutputStepMeta.setFilename( "${java.io.tmpdir}/" + getFileName() );
    csvOutputStepMeta.setExtension( "csv" );
    csvOutputStepMeta.setFastDump( true );

    StepMeta stepMeta = new StepMeta( name, csvOutputStepMeta );
    stepMeta.setCopies( 1 );
    return stepMeta;
  }

  public String getMimeType() {
    return "text/csv";
  }

  public String getAttachmentName() {
    return this.attachmentName;
  }

  public String getType() {
    return "csv";
  }


}
