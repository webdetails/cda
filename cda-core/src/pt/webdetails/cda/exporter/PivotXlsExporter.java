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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.*;
import pt.webdetails.cda.utils.MetadataTableModel;

import javax.swing.table.TableModel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * tobias.tempel@hgv-online.de
 * (inspired by felix CXlsExporter)
 *
 * performs pivot operation on tableModel data and accordingly builds sheet for export
 */
public class PivotXlsExporter extends AbstractExporter
{
    public static final String TEMPLATE_NAME_SETTING = "templateName";
    private final String templatesDir = "/opt/pentaho/xls-templates/";
    private static final String MIME_TYPE = "application/vnd.ms-excel";
    private static final Log logger = LogFactory.getLog( PivotXlsExporter.class );
    private String attachmentName;
    public HashMap<String, String> templateSettings = new HashMap<String, String>();


    public PivotXlsExporter(Map<String, String> extraSettings)
    {
        super( extraSettings );
        this.attachmentName = getSetting(ATTACHMENT_NAME_SETTING, "analytics-report." + getType());
        logger.debug( "Initialized CXlsExporterExporter with attachement filename '" + attachmentName + "'" );
    }
    public void export( final OutputStream out, final TableModel tableModel ) throws ExporterException {

        XSSFWorkbook wb;
        InputStream inputStream = null;
        MetadataTableModel table = (MetadataTableModel) tableModel;
        XSSFSheet sheet;

        String pivotAfterColumnName = null;
        String pivotRowGroupColumn = null;
        String [] pivotColumnGroupColumns = null;
        String pivotColumnGroupTitleColumn = null;

        try {
            inputStream = new FileInputStream(templatesDir + templateSettings.get("filename"));
            wb = new XSSFWorkbook(inputStream);
            sheet = wb.getSheetAt(0);
            if(templateSettings.containsKey("PivotAfterColumnName")){
                pivotAfterColumnName = templateSettings.get("PivotAfterColumnName");
            }
            if(templateSettings.containsKey("PivotCsGroupColumns")){
                try {
                    pivotColumnGroupColumns = templateSettings.get("PivotCsGroupColumns").split(",");
                } catch( Exception ex) {
                    throw new ExporterException( "PivotCsGroupColumns - illegal value", ex );
                }
            }
            if(templateSettings.containsKey("PivotRowGroupColumn")){
                pivotRowGroupColumn = templateSettings.get("PivotRowGroupColumn");
            }
            if(templateSettings.containsKey("PivotGroupTitleColumn")){
                pivotColumnGroupTitleColumn = templateSettings.get("PivotGroupTitleColumn");
            }

            if( pivotAfterColumnName==null || pivotColumnGroupColumns==null || pivotRowGroupColumn==null || pivotColumnGroupTitleColumn==null) {
                throw new Exception( "required template settings missing (PivotAfterColumnName,PivotCsGroupColumns,PivotRowGroupColumn,PivotGroupTitleColumn");
            }
        } catch ( Exception e ) {
            throw new ExporterException( "Error at loading TemplateFile", e );
        }

        PivotTableData pivotTableData = retrievePivotTableData( table, pivotRowGroupColumn, pivotAfterColumnName, pivotColumnGroupTitleColumn, pivotColumnGroupColumns);
        writePivotColumns( table, sheet, pivotTableData, pivotColumnGroupColumns);
        writePivotRows( sheet, pivotTableData, pivotColumnGroupColumns);

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

    /*
     * private class PivotTableDataInfo
     */
    private class PivotTableData {
        int pivotTableColumnNumber = 0;
        int lastFixedColumnIndex = 0;
        int pivotGroupTitleColumnIndex = 0;
        int pivotRowGroupColumnIndex = 0;
        Set<String> groupTitleSet = new HashSet<String>();
        List<String> rowGroupSelectors = new ArrayList<String>();
        Map<String,List<String>> rowGroupData = new HashMap<String,List<String>>();
        Map<String,Map<String,List<String>>> pivotData = new HashMap<String,Map<String,List<String>>>();
    }

    /*
     * PivotTableDataInfo retrievePivotOperationColumns
     * String pivotRowGroupColumn - column that groups rows for pivot operation
     * String pivotAfterColumnName - columns left of this one are simply copied, titles in 2nd header row
     * String pivotGroupTitleColumn - title for group of columns (see above) that is written to 1st header row
     * String [] pivotGroupColumns - comma separated list of column titles that will be repeated for each distinct title occurring in pivotGroupTitleColumn
     */
    private PivotTableData retrievePivotTableData(
            MetadataTableModel table,
            String pivotRowGroupColumn,
            String pivotAfterColumnName, String pivotGroupTitleColumn,
            String [] pivotGroupColumns)
    {
        PivotTableData pivotTableData = new PivotTableData();
        int tableColumnsCount = table.getColumnCount();
        // find column indices
        for( int columnIdx = 0; columnIdx<tableColumnsCount; ++columnIdx) {
            if( table.getColumnName( columnIdx).equals( pivotRowGroupColumn)) {
                pivotTableData.pivotRowGroupColumnIndex = columnIdx;
            } else if( table.getColumnName( columnIdx).equals( pivotAfterColumnName)) {
                pivotTableData.lastFixedColumnIndex = columnIdx;
            } else if( table.getColumnName( columnIdx).equals( pivotGroupTitleColumn)) {
                pivotTableData.pivotGroupTitleColumnIndex = columnIdx;
            }
        }
        // build set of column group titles
        for(int rowIdx=2;rowIdx<table.getRowCount();rowIdx++){
            pivotTableData.groupTitleSet.add(table.getValueAt(rowIdx, pivotTableData.pivotGroupTitleColumnIndex).toString());
        }
        // iterate over table rows to retrieve data
        for(int tableRowIdx=0;tableRowIdx<table.getRowCount();tableRowIdx++) {
            String currenRowGroupSelector = table.getValueAt(tableRowIdx, pivotTableData.pivotRowGroupColumnIndex).toString();
            String currentColumnGroupTitle = table.getValueAt(tableRowIdx, pivotTableData.pivotGroupTitleColumnIndex).toString();
            // fetch fixed row head data
            if( ! pivotTableData.rowGroupData.containsKey( currenRowGroupSelector)) {
                pivotTableData.rowGroupSelectors.add(currenRowGroupSelector);
                List<String> groupDataList = new ArrayList<String>();
                for( int colIdx=0; colIdx<= pivotTableData.lastFixedColumnIndex; ++colIdx) {
                    groupDataList.add( table.getValueAt(tableRowIdx, colIdx).toString() );
                }
                pivotTableData.rowGroupData.put(currenRowGroupSelector, groupDataList);
            }
            // fetch data for dynamically built new pivot columns
            if( !pivotTableData.pivotData.containsKey( currenRowGroupSelector)) {
                pivotTableData.pivotData.put(currenRowGroupSelector, new HashMap<String, List<String>>());
            }
            Map<String, List<String>> currentRow = pivotTableData.pivotData.get(currenRowGroupSelector);
            if( !currentRow.containsKey( currentColumnGroupTitle)) {
                currentRow.put( currentColumnGroupTitle, new ArrayList());
            }
            List currentRowColumnGroup = currentRow.get( currentColumnGroupTitle);
            int tableColIdx = pivotTableData.pivotGroupTitleColumnIndex;
            for( String columnTitle : pivotGroupColumns ) {
                if( table.getValueAt(tableRowIdx, ++tableColIdx)!=null) {
                    currentRowColumnGroup.add(table.getValueAt(tableRowIdx, tableColIdx).toString());
                } else {
                    currentRowColumnGroup.add("");
                }
            }
        }


        logger.info("pivotTableDataInfo.groupTitleSet: " + pivotTableData.groupTitleSet.size());
        return pivotTableData;
    }

    /*
     * writePivotColumns
     * String [] pivotGroupColumns - column titles for group of columns that will be repeated for each distinct name within pivotGroupTitleColumn, column titles in 2nd header row
     */
    private void writePivotColumns(
            MetadataTableModel table,
            XSSFSheet sheet,
            PivotTableData pivotTableData,
            String [] pivotGroupColumns)
    {
        // create first header row
        CellStyle headerCellStyle = sheet.getRow( 0).getCell( 0).getCellStyle();
        Row header = sheet.createRow( 0);
        boolean processingPivotColumns = false;
        int columnsToCreateIndex = 0;
        while( true) {
            if( !processingPivotColumns) {
                Cell cell = header.createCell(columnsToCreateIndex);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue("");
                if (pivotTableData.lastFixedColumnIndex == columnsToCreateIndex) {
                    processingPivotColumns = true;
                } else {
                    ++columnsToCreateIndex;
                }
            } else {
                // create one column group for each columnGroupTitle
                for( String groupTitle : pivotTableData.groupTitleSet) {
                    boolean writeTitle = true;
                    for( String columnTitle : pivotGroupColumns ) {
                        Cell cell = header.createCell(++columnsToCreateIndex);
                        cell.setCellStyle(headerCellStyle);
                        if( writeTitle) {
                            cell.setCellValue(groupTitle);
                            writeTitle = false;
                        }
                    }
                }
                break;
            }
        }
        // create second header row
        header = sheet.createRow( 1);
        processingPivotColumns = false;
        columnsToCreateIndex = 0;
        while( true) {
            if( !processingPivotColumns) {
                Cell cell = header.createCell(columnsToCreateIndex);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(table.getColumnName(columnsToCreateIndex));
                if (pivotTableData.lastFixedColumnIndex == columnsToCreateIndex) {
                    processingPivotColumns = true;
                } else {
                    ++columnsToCreateIndex;
                }
            } else {
                // create one column group for each columnGroupTitle
                for( String groupTitle : pivotTableData.groupTitleSet) {
                    for( String columnTitle : pivotGroupColumns ) {
                        Cell cell = header.createCell(++columnsToCreateIndex);
                        cell.setCellStyle(headerCellStyle);
                        cell.setCellValue( columnTitle);
                    }
                }
                break;
            }
        }
        // finish creating header rows
        sheet.createFreezePane( 0, 2);
        pivotTableData.pivotTableColumnNumber = columnsToCreateIndex+1;
        logger.debug("columns lastFixedColumnIndex=" + pivotTableData.lastFixedColumnIndex + " columnNumber=" + pivotTableData.pivotTableColumnNumber);
    }

    /*
     * writePivotRows
     */
    private void writePivotRows( XSSFSheet sheet, PivotTableData pivotTableData, String [] pivotGroupColumns)
    {
        // create sheet content
        CellStyle rowCellStyle = sheet.getRow( 2).getCell(0).getCellStyle();
        int sheetRowIdx = 2;
        for( String rowGroupSelector : pivotTableData.rowGroupSelectors) {
            Row row = sheet.createRow( sheetRowIdx++);
            int sheetRowColumnIdx = 0;
            for( String rowGroupDataValue : pivotTableData.rowGroupData.get( rowGroupSelector)) {
                Cell cell = row.createCell( sheetRowColumnIdx++);
                cell.setCellStyle(rowCellStyle);
                cell.setCellValue(rowGroupDataValue);
            }
            Map<String, List<String>> currentRow = pivotTableData.pivotData.get(rowGroupSelector);
            for( String columnGroupTitle : pivotTableData.groupTitleSet) {
                List<String> columnGroupData = currentRow.get( columnGroupTitle);
                if( columnGroupData != null) {
                    for (String columnData : columnGroupData) {
                        Cell cell = row.createCell(sheetRowColumnIdx++);
                        cell.setCellStyle(rowCellStyle);
                        cell.setCellValue(columnData);
                    }
                } else {
                    // no data for this column group in this row
                    for( String columnTitle : pivotGroupColumns ) {
                        Cell cell = row.createCell(sheetRowColumnIdx++);
                        cell.setCellStyle(rowCellStyle);
                        cell.setCellValue("");
                    }
                }
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
        return "xlsx";
    }
}
