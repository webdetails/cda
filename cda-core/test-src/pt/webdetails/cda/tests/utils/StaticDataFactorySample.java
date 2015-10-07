/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package pt.webdetails.cda.tests.utils;

import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

/**
 * Todo: Document me
 *
 * @author Thomas Morgner
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
      model.addRow(
        new Object[] { parameter, new Long( 1 ), "Subreports can use parameters to control what data is returned." } );
      return model;
    }

  }


}
