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

import javax.swing.table.TableModel;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class CXlsExporter extends AbstractExporter
{
    public static final String TEMPLATE_NAME_SETTING = "templateName";
    private final String templatesDir = "/opt/pentaho/xls-templates/";
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
      this.attachmentName = getSetting(ATTACHMENT_NAME_SETTING, "analytics-report." + getType());
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

        int ignoreFirstXRows = 0;
        int rowOffset = 0;
        int columnOffset = 0;
        boolean writeColumns = true;
        boolean templateFound = false;

        String csvSeperator = "";
        int numberOfHeaderRows = 0;

        if(templateSettings.keySet().size() > 0){
            templateFound = true;
            try {
                //inputStream = new ClassPathResource(templateSettings.get("filename")).getInputStream();
                inputStream = new FileInputStream(templatesDir + templateSettings.get("filename"));
                wb = new HSSFWorkbook(inputStream);
                sheet = wb.getSheetAt(0);
                if(templateSettings.containsKey("RowOffset")){
                    rowOffset = Integer.parseInt(templateSettings.get("RowOffset"));
                }
                if(templateSettings.containsKey("ColumnOffset")){
                    columnOffset = Integer.parseInt(templateSettings.get("ColumnOffset"));
                }
                if(templateSettings.containsKey("WriteColumnNames")){
                    writeColumns = Boolean.parseBoolean(templateSettings.get("WriteColumnNames"));
                }
                if(templateSettings.containsKey("CsvSeperator")){
                    csvSeperator = "\\" + templateSettings.get("CsvSeperator").toString();
                }
                if(templateSettings.containsKey("WriteFirstXRowsAsHeader")){
                    numberOfHeaderRows = Integer.parseInt(templateSettings.get("WriteFirstXRowsAsHeader"));
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


        boolean interpretCsv = !csvSeperator.equals("");


        if(writeColumns){
            CellStyle headerCellStyle = null;
            if(templateFound)
                headerCellStyle = sheet.getRow(rowOffset).getCell(columnOffset).getCellStyle();
            if(numberOfHeaderRows > 0){
                ignoreFirstXRows = numberOfHeaderRows;
                for(int i=0;i<numberOfHeaderRows;i++){

                    String[] seperatedRow = new String[0];
                    int colCount=table.getColumnCount();
                    if(interpretCsv){
                        seperatedRow = table.getValueAt(i, 0).toString().split(csvSeperator,-1);
                        colCount = seperatedRow.length;
                    }
                    Row header = sheet.createRow(rowOffset);
                    for(int col=0;col<colCount;col++){
                        Cell cell = header.createCell(col+columnOffset);
                        if(templateFound)
                            cell.setCellStyle(headerCellStyle);
                        if(interpretCsv){
                            cell.setCellValue(seperatedRow[col]);
                        }else{
                            cell.setCellValue(table.getColumnName(col));
                        }
                    }
                    rowOffset++;
                }
            }else{
                Row header = sheet.createRow(rowOffset);
                for(int col=0;col<table.getColumnCount();col++){
                    Cell cell = header.createCell(col+columnOffset);
                    if(templateFound)
                        cell.setCellStyle(headerCellStyle);
                    cell.setCellValue(table.getColumnName(col));
                }
                rowOffset++;
            }
            sheet.createFreezePane(0, rowOffset);

        }


        for(int r=ignoreFirstXRows;r<table.getRowCount();r++){

            CellStyle rowCellStyle = null;

            if(templateFound)
                rowCellStyle = sheet.getRow(rowOffset).getCell(columnOffset).getCellStyle();

            Row row = sheet.createRow(r+rowOffset-ignoreFirstXRows);

            int colCount;

            String[] seperatedRow = new String[0];
            if(interpretCsv){
                seperatedRow = table.getValueAt(r, 0).toString().split(csvSeperator);
                colCount = seperatedRow.length;
            }else{
                colCount = table.getColumnCount();
            }

            for(int col=0;col<colCount;col++){
                Cell cell = row.createCell(col+columnOffset);
                if(templateFound)
                    cell.setCellStyle(rowCellStyle);
                if(!interpretCsv){
                    try{
                        setConvertedValue(cell, table.getValueAt(r, col),col,table);
                    }catch(Exception e){
                        setConvertedValue(cell, Cell.CELL_TYPE_ERROR,col,table);
                    }
                }else{
                    setConvertedValue(cell,seperatedRow[col],col,table);
                }
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






  private void setConvertedValue(Cell cell,Object obj,int col,MetadataTableModel table){
    try{
        if(table.getCustomType(col).equals("")){
            if(obj instanceof Number){
                cell.setCellValue(Double.parseDouble(obj.toString()));
            }else if(obj instanceof Boolean){
                cell.setCellValue((Boolean) obj);
            }else if(obj instanceof Date){
                cell.setCellValue((Date)obj);
            }else if(obj instanceof Calendar){
                cell.setCellValue((Calendar)obj);
            }else{
                cell.setCellValue((String) obj);
            }
        }else{
            String clazz = table.getCustomType(col).toUpperCase();
            if(clazz.equals("EURO")){
                cell.setCellStyle(euroCellStyle);
                cell.setCellValue(Double.parseDouble(obj.toString()));
            }else if(clazz.equals("DOUBLE")){
                cell.setCellStyle(doubleCellStyle);
                cell.setCellValue(Double.parseDouble(obj.toString()));
            }else if(clazz.equals("INTEGER")){
                cell.setCellStyle(integerCellStyle);
                cell.setCellValue(Double.parseDouble(obj.toString()));
            }else if(clazz.equals("STRING")){
                cell.setCellValue(obj.toString());
            }else if(clazz.equals("PERCENT")){
                cell.setCellStyle(percentCellStyle);
                cell.setCellValue(Double.parseDouble(obj.toString()));
            }else if(clazz.equals("DATE")){
                cell.setCellStyle(dateCellStyle);
                if(obj instanceof Date){
                    cell.setCellValue((Date)obj);
                }else if(obj instanceof Calendar){
                    cell.setCellValue((Calendar)obj);
                }else{
                    cell.setCellValue(obj.toString());
                }
            } else if(clazz.equals("DATEMONTH")){
                cell.setCellStyle(datemonthCellStyle);
                if(obj instanceof Date){
                    cell.setCellValue((Date)obj);
                }else if(obj instanceof Calendar){
                    cell.setCellValue((Calendar)obj);
                }else{
                    cell.setCellValue(obj.toString());
                }
            }else if(clazz.equals("DATEYEAR")){
                cell.setCellStyle(dateyearCellStyle);
                if(obj instanceof Date){
                    cell.setCellValue((Date)obj);
                }else if(obj instanceof Calendar){
                    cell.setCellValue((Calendar)obj);
                }else{
                    cell.setCellValue(obj.toString());
                }
            }else if(clazz.equals("DATEANDTIME")){
                cell.setCellStyle(dateAndTimeCellStyle);
                if(obj instanceof Date){
                    cell.setCellValue((Date)obj);
                }else if(obj instanceof Calendar){
                    cell.setCellValue((Calendar)obj);
                }else{
                    cell.setCellValue(obj.toString());
                }
            }else{
                if(obj != null)
                    cell.setCellValue(obj.toString());
                else
                    cell.setCellValue("");
            }
        }
    }catch(Exception e){
        try{
            if(obj != null)
                cell.setCellValue(obj.toString());
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
