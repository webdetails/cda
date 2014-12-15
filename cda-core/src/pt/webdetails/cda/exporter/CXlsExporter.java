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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import pt.webdetails.cda.utils.MetadataTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class CXlsExporter extends AbstractExporter
{
    public static final String TEMPLATE_NAME_SETTING = "templateName";
    private static final String MIME_TYPE = "application/vnd.ms-excel";
    private static final Log logger = LogFactory.getLog( CXlsExporter.class );
    private String attachmentName;
    private CellStyle euroCellStyle;
    private CellStyle doubleCellStyle;
    private CellStyle integerCellStyle;
    private CellStyle percentCellStyle;
    private CellStyle dateCellStyle;
    private CellStyle datemonthCellStyle;
    private CellStyle dateyearCellStyle;
    private CellStyle dateAndTimeCellStyle;
    public HashMap<String, String> templateSettings = new HashMap<String, String>();


  public CXlsExporter(Map<String, String> extraSettings)
  {
      super( extraSettings );
      this.attachmentName = getSetting(ATTACHMENT_NAME_SETTING, "cda-export." + getType());
      logger.debug( "Initialized CXmlExporter with attachement filename '" + attachmentName + "'" );
  }
    public void export( final OutputStream out, final TableModel tableModel ) throws ExporterException {

//        <Template file="testTemplate.xls">
    //        <RowOffset>3</RowOffset>
    //        <ColumnOffset>2</ColumnOffset>
    //        <WriteColumnNames>true</WriteColumnNames>
//        </Template>

        Workbook wb;
        InputStream inputStream = null;
        MetadataTableModel table = (MetadataTableModel) tableModel;
        Sheet sheet;

        int rowOffset = 0;
        int columnOffset = 0;
        boolean writeColumns = true;

        if(templateSettings.keySet().size() > 0){
            try {
                inputStream = new ClassPathResource(templateSettings.get("filename")).getInputStream();
                wb = new HSSFWorkbook(inputStream);
                sheet = wb.getSheetAt(0);
                if(templateSettings.containsKey("RowOffset")){
                    rowOffset = Integer.parseInt(templateSettings.get("RowOffset"));
                }
                if(templateSettings.containsKey("ColumnOffset")){
                    columnOffset = Integer.parseInt(templateSettings.get("ColumnOffset"));
                }
                if(templateSettings.containsKey("WriteColumns")){
                    writeColumns = Boolean.parseBoolean(templateSettings.get("WriteColumns"));
                }
            } catch ( Exception e ) {
                throw new ExporterException( "Error at loading TemplateFile", e );
            }
        }else{
            wb = new HSSFWorkbook();
            sheet = wb.createSheet("Sheet1");
        }

        DataFormat cf = wb.createDataFormat();
        euroCellStyle = wb.createCellStyle();
        euroCellStyle.setDataFormat(cf.getFormat("#,##0.00 \"€\""));
        doubleCellStyle = wb.createCellStyle();
        doubleCellStyle.setDataFormat(cf.getFormat("0.00"));
        integerCellStyle = wb.createCellStyle();
        integerCellStyle.setDataFormat(cf.getFormat("0"));
        percentCellStyle = wb.createCellStyle();
        percentCellStyle.setDataFormat(cf.getFormat("0.00%"));
        dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(cf.getFormat("dd.mm.yyyy"));
        datemonthCellStyle = wb.createCellStyle();
        datemonthCellStyle.setDataFormat(cf.getFormat("mm.yyyy"));
        dateyearCellStyle = wb.createCellStyle();
        dateyearCellStyle.setDataFormat(cf.getFormat("yyyy"));
        dateAndTimeCellStyle = wb.createCellStyle();
        dateAndTimeCellStyle.setDataFormat(cf.getFormat("dd.mm.yyyy hh:mm:ss"));

        if(writeColumns){
            Row header = sheet.createRow(0+rowOffset);
            for(int col=0;col<table.getColumnCount();col++){
                Cell cell = header.createCell(col+columnOffset);
                cell.setCellValue(table.getColumnName(col));
            }
            rowOffset++;
        }


        for(int r=0;r<table.getRowCount();r++){
            Row row = sheet.createRow(r+rowOffset);
            for(int col=0;col<table.getColumnCount();col++){
                Cell cell = row.createCell(col+columnOffset);
                setConvertedValue(cell,r,col,table);
            }
        }
        try {
            wb.write(out);
        } catch ( IOException e ) {
            throw new ExporterException( "IO Exception converting to utf-8", e );
        } finally{
            if(templateSettings.keySet().size() > 0){
                try {
                    inputStream.close();
                } catch ( Exception e ) {
                    throw new ExporterException( "Error at closing TemplateFile", e );
                }
            }
        }
    }
  private void setConvertedValue(Cell cell,int row,int col,MetadataTableModel table){
    try{
        if(table.getCustomType(col).equals("")){
            Object o = table.getValueAt(row, col);
            if(o instanceof Number){
                cell.setCellValue(Double.parseDouble(o.toString()));
//            }else if(o instanceof String){
//                cell.setCellValue((String) o);
            }else if(o instanceof Boolean){
                cell.setCellValue((Boolean) o);
            }else if(o instanceof Date){
                cell.setCellValue((Date)o);
            }else if(o instanceof Calendar){
                cell.setCellValue((Calendar)o);
            }else{
                cell.setCellValue((String) o);
            }
        }else{
            String clazz = table.getCustomType(col).toUpperCase();
            if(clazz.equals("EURO")){
                cell.setCellStyle(euroCellStyle);
                cell.setCellValue(Double.parseDouble(table.getValueAt(row, col).toString()));
            }else if(clazz.equals("DOUBLE")){
                cell.setCellStyle(doubleCellStyle);
                cell.setCellValue(Double.parseDouble(table.getValueAt(row, col).toString()));
            }else if(clazz.equals("INTEGER")){
                cell.setCellStyle(integerCellStyle);
                cell.setCellValue(Double.parseDouble(table.getValueAt(row, col).toString()));
            }else if(clazz.equals("STRING")){
                cell.setCellValue(table.getValueAt(row, col).toString());
            }else if(clazz.equals("PERCENT")){
                cell.setCellStyle(percentCellStyle);
                cell.setCellValue(Double.parseDouble(table.getValueAt(row, col).toString()));
            }else if(clazz.equals("DATE")){
                cell.setCellStyle(dateCellStyle);
                if(table.getValueAt(row, col) instanceof Date){
                    cell.setCellValue((Date)table.getValueAt(row, col));
                }else if(table.getValueAt(row, col) instanceof Calendar){
                    cell.setCellValue((Calendar)table.getValueAt(row, col));
                }else{
                    cell.setCellValue(table.getValueAt(row, col).toString());
                }
            } else if(clazz.equals("DATEMONTH")){
                cell.setCellStyle(datemonthCellStyle);
                if(table.getValueAt(row, col) instanceof Date){
                    cell.setCellValue((Date)table.getValueAt(row, col));
                }else if(table.getValueAt(row, col) instanceof Calendar){
                    cell.setCellValue((Calendar)table.getValueAt(row, col));
                }else{
                    cell.setCellValue(table.getValueAt(row, col).toString());
                }
            }else if(clazz.equals("DATEYEAR")){
                cell.setCellStyle(dateyearCellStyle);
                if(table.getValueAt(row, col) instanceof Date){
                    cell.setCellValue((Date)table.getValueAt(row, col));
                }else if(table.getValueAt(row, col) instanceof Calendar){
                    cell.setCellValue((Calendar)table.getValueAt(row, col));
                }else{
                    cell.setCellValue(table.getValueAt(row, col).toString());
                }
            }else if(clazz.equals("DATEANDTIME")){
                cell.setCellStyle(dateAndTimeCellStyle);
                if(table.getValueAt(row, col) instanceof Date){
                    cell.setCellValue((Date)table.getValueAt(row, col));
                }else if(table.getValueAt(row, col) instanceof Calendar){
                    cell.setCellValue((Calendar)table.getValueAt(row, col));
                }else{
                    cell.setCellValue(table.getValueAt(row, col).toString());
                }
            }else{
                if(table.getValueAt(row, col) != null)
                    cell.setCellValue(table.getValueAt(row, col).toString());
                else
                    cell.setCellValue("");
            }
        }
    }catch(Exception e){
        try{
            if(table.getValueAt(row, col) != null)
                cell.setCellValue(table.getValueAt(row, col).toString());
            else
                cell.setCellValue("");
        }catch(Exception e2){
            cell.setCellType(Cell.CELL_TYPE_ERROR);
        }
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
