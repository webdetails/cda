/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.exporter;

import java.io.OutputStream;
import java.util.Date;
import javax.swing.table.TableModel;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 5, 2010
 * Time: 5:06:31 PM
 */
public abstract class AbstractExporter implements Exporter
{


  public abstract void export(final OutputStream out, final TableModel tableModel) throws ExporterException;

  public abstract String getMimeType();


  protected String getColType(final Class<?> columnClass) throws ExporterException
  {

    if (columnClass.equals(String.class))
    {
      return "String";
    }
    else if (columnClass.equals(Boolean.class))
    {
      return "Boolean";
    }
    else if (columnClass.equals(Integer.class) || columnClass.equals(Short.class) || columnClass.equals(Byte.class))
    {
      return "Integer";
    }
    else if (Number.class.isAssignableFrom(columnClass))
    {
      return "Numeric";
    }
    else if (Date.class.isAssignableFrom(columnClass))
    {
      return "Date";
    }
    else if (columnClass.equals(Object.class) )
    {
      // todo: Quick and dirty hack, as the formula never knows what type is returned. 
      return "String";
    }
    else{

      throw new ExporterException("CDA exporter doesn't know how to handle: " + columnClass.toString(), null);

    }

  }

}
