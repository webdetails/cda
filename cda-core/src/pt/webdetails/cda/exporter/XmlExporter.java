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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.swing.table.TableModel;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * XmlExporter
 * <p/>
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:07:12 PM
 */
public class XmlExporter extends AbstractExporter
{

  public XmlExporter(Map <String,String> extraSettings)
  {
    super();
  }


  public void export(final OutputStream out, final TableModel tableModel) throws ExporterException
  {


    final Document document = DocumentHelper.createDocument();

    // Generate metadata

    final Element root = document.addElement("CdaExport");

    final Element metadata = root.addElement("MetaData");

    final int columnCount = tableModel.getColumnCount();
    final int rowCount = tableModel.getRowCount();

    for (int i = 0; i < columnCount; i++)
    {
      final Element columnInfo = metadata.addElement("ColumnMetaData");
      columnInfo.addAttribute("index", (String.valueOf(i)));
      columnInfo.addAttribute("type", getColType(tableModel.getColumnClass(i)));
      columnInfo.addAttribute("name", tableModel.getColumnName(i));

    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    final Element resultSet = root.addElement("ResultSet");
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
    {

      final Element row = resultSet.addElement("Row");

      for (int colIdx = 0; colIdx < columnCount; colIdx++)
      {
        final Element col = row.addElement("Col");

        final Object value = tableModel.getValueAt(rowIdx, colIdx);
        if (value instanceof Date)
        {
          col.setText(format.format(value));
        }
        else if (value != null)
        {
          // numbers can be safely converted via toString, as they use a well-defined format there
          col.setText(value.toString());
        }
        else{
          col.addAttribute("isNull","true");
        }

      }
    }

    try
    {
      final Writer writer = new BufferedWriter(new OutputStreamWriter(out));

      document.setXMLEncoding("UTF-8");
      document.write(writer);
      writer.flush();
    }
    catch (IOException e)
    {
      throw new ExporterException("IO Exception converting to utf-8", e);
    }

  }

  public String getMimeType()
  {
    return "text/xml";
  }

  public String getAttachmentName()
  {
    // No attachment required
    return null;
  }
}
