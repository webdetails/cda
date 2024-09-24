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

package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.TableModelUtils;

/**
 * Class to join 2 datatables
 */
public class UnionCompoundDataAccess extends CompoundDataAccess {

  //private static final Log logger = LogFactory.getLog(UnionCompoundDataAccess.class);
  private static final String TYPE = "union";
  private String topId;
  private String bottomId;


  public UnionCompoundDataAccess() {
  }


  public UnionCompoundDataAccess( final Element element ) {
    super( element );

    Element top = (Element) element.selectSingleNode( "Top" );
    Element bottom = (Element) element.selectSingleNode( "Bottom" );

    topId = top.attributeValue( "id" );
    bottomId = bottom.attributeValue( "id" );

  }


  public String getType() {
    return TYPE;
  }


  protected TableModel queryDataSource( final QueryOptions queryOptions ) throws QueryException {


    try {
      QueryOptions croppedOptions = (QueryOptions) queryOptions.clone();
      croppedOptions.setSortBy( new ArrayList<String>() );
      croppedOptions.setPageSize( 0 );
      croppedOptions.setPageStart( 0 );
      final TableModel tableModelA = this.getCdaSettings().getDataAccess( topId ).doQuery( croppedOptions );
      final TableModel tableModelB = this.getCdaSettings().getDataAccess( bottomId ).doQuery( croppedOptions );

      return TableModelUtils.appendTableModel( tableModelA, tableModelB );

    } catch ( CloneNotSupportedException e ) {
      throw new QueryException( "Couldn't clone settings ", e );
    } catch ( UnknownDataAccessException e ) {
      throw new QueryException( "Unknown Data access in CompoundDataAccess ", e );
    }


  }


  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.NONE;
  }


  @Override
  public ArrayList<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = super.getInterface();
    properties.add(
      new PropertyDescriptor( "top", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    properties.add(
      new PropertyDescriptor( "bottom", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }

  public String getTopId() {
    return topId;
  }


  public String getBottomId() {
    return bottomId;
  }

  @Override
  public void setQuery( String query ) {
    // Do nothing
  }
}
