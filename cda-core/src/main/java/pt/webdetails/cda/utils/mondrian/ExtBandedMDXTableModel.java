/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.utils.mondrian;

import mondrian.olap.Axis;
import mondrian.olap.Member;
import mondrian.olap.Position;
import mondrian.olap.Result;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.BandedMDXTableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtBandedMDXTableModel extends BandedMDXTableModel {

  public ExtBandedMDXTableModel( Result resultSet, int rowLimit, boolean membersOnAxisSorted ) {
    super( resultSet, rowLimit, membersOnAxisSorted );
  }

  @Override
  protected List<String> computeColumnNames( Axis[] axes, List<Member> columnToMemberMapper ) {
    ArrayList<String> columnNames = new ArrayList<String>();
    for ( final Member member : columnToMemberMapper ) {
      columnNames.add( member.getLevel().getUniqueName() );
    }
    if ( axes.length > 0 ) {
      // now create the column names for the column-axis
      final Axis axis = axes[ 0 ];
      final List<Position> positions = axis.getPositions();
      for ( int i = 0; i < positions.size(); i++ ) {
        final Position position = positions.get( i );
        columnNames.add( MDXTableModelUtils.computeUniqueColumnName( position ) );
        //columnNames.add( ResultSetProcessingLib.computeUniqueColumnName( position ));
      }
    } else {
      columnNames.add( "Measure" );
    }
    return Collections.unmodifiableList( columnNames );
  }

}
