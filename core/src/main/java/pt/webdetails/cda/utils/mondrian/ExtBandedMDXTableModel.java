/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
