/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.filetests;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import javax.swing.table.TableModel;

/**
 * Todo: Document me
 */
public class StaticDataFactorySample {
  private static final Long ZERO = new Long( 0 );

  public StaticDataFactorySample() {
  }

  public TableModel createMainQuery() {
    final TypedTableModel model =
      new TypedTableModel( new String[] { "ID", "TEXT" }, new Class[] { Long.class, String.class }, 0 );
    model.addRow( new Object[] { new Long( 0 ), "Hello World" } );
    model.addRow( new Object[] { new Long( 1 ), "Your DataFactory works perfectly." } );
    return model;
  }

  public TableModel createSubQuery( Long parameter ) {
    final TypedTableModel model = new TypedTableModel( new String[] { "ID", "NUMBER", "DESCRIPTION" },
      new Class[] { Long.class, String.class, String.class }, 0 );
    if ( ZERO.equals( parameter ) ) {
      model.addRow( new Object[] { parameter, new Long( 0 ), "Look, you got a new dataset." } );
      model.addRow( new Object[] { parameter, new Long( 1 ), "So Subreport queries work too.." } );
      return model;
    } else {
      model.addRow( new Object[] { parameter, new Long( 0 ), "Ahh, another query-parameter, another table." } );
      model.addRow( new Object[] {
        parameter, new Long( 1 ), "Subreports can use parameters to control what data is returned." } );
      return model;
    }

  }


}
