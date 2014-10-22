/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.exporter;

//import org.apache.poi.hssf.model.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.pentaho.reporting.engine.classic.core.MetaTableModel;
import pt.webdetails.cda.utils.MetadataTableModel;

import javax.swing.table.TableModel;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class CXlsExporter extends AbstractExporter
{
    public static final String TEMPLATE_NAME_SETTING = "templateName";
    private static final String MIME_TYPE = "application/vnd.ms-excel";
    private static final Log logger = LogFactory.getLog( CXlsExporter.class );
    private String attachmentName;
    private CellStyle currencyCellStyle;


  public CXlsExporter(Map<String, String> extraSettings)
  {
      super( extraSettings );
      this.attachmentName = getSetting(ATTACHMENT_NAME_SETTING, "cda-export." + getType());
      logger.debug( "Initialized CXmlExporter with attachement filename '" + attachmentName + "'" );
  }
    public void export( final OutputStream out, final TableModel tableModel ) throws ExporterException {




        MetadataTableModel table = (MetadataTableModel) tableModel;
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");

        DataFormat cf = wb.createDataFormat();
        currencyCellStyle = wb.createCellStyle();
        currencyCellStyle.setDataFormat(cf.getFormat("#,##0.00\\ _€"));

        Row header = sheet.createRow(0);
        for(int col=0;col<table.getColumnCount();col++){
            Cell cell = header.createCell(col);
            cell.setCellValue(table.getColumnName(col));
        }


        for(int r=0;r<table.getRowCount();r++){
            Row row = sheet.createRow(r+1);
            for(int col=0;col<table.getColumnCount();col++){
                Cell cell = row.createCell(col);
                setConvertedValue(cell,r,col,table);
            }
        }
        try {
            wb.write(out);
        } catch ( IOException e ) {
            throw new ExporterException( "IO Exception converting to utf-8", e );
        }
    }
  private void setConvertedValue(Cell cell,int row,int col,MetadataTableModel table){
    try{
        if(table.getCustomType(col).equals("")){
            Object o = table.getValueAt(row, col);
            if(o instanceof Number){
                cell.setCellValue(Double.parseDouble(o.toString()));
            }else if(o instanceof String){
                cell.setCellValue((String) o);
            }else if(o instanceof Boolean){
                cell.setCellValue((Boolean) o);
            }else if(o instanceof Date){
                cell.setCellValue((Date)o);
            }else if(o instanceof Calendar){
                cell.setCellValue((Calendar)o);
            }
        }else{
            String clazz = table.getCustomType(col).toUpperCase();
            if(clazz.equals("EURO")){
                cell.setCellStyle(currencyCellStyle);
                cell.setCellValue(Double.parseDouble(table.getValueAt(row, col).toString()));
            }else if(clazz.equals("PERCENT")){
                cell.setCellStyle(currencyCellStyle);
                cell.setCellValue(Double.parseDouble(table.getValueAt(row, col).toString()));
            }
        }
    }catch(Exception e){
        cell.setCellType(Cell.CELL_TYPE_ERROR);
    }
  }

  public String getMimeType()
  {
    return MIME_TYPE;
  }

  public String getAttachmentName()
  {
      return this.attachmentName;
  }

  public String getType()
  {
      return "xls";
  }
}
