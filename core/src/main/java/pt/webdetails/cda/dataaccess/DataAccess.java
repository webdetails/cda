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

import org.dom4j.Element;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitor;

import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * DataAccess interface
 */
public interface DataAccess {

  enum OutputMode {
    INCLUDE, EXCLUDE
  }

  String getId();

  String getName();

  String getType();

  // the default behaviour is to have the label equal to the type
  default String getLabel() {
    return getType();
  }

  DataAccessEnums.ACCESS_TYPE getAccess();

  boolean isCacheEnabled();

  int getCacheDuration();

  CdaSettings getCdaSettings();

  void setCdaSettings( CdaSettings cdaSettings );

  TableModel doQuery( QueryOptions queryOptions ) throws QueryException;

  ColumnDefinition getColumnDefinition( int idx );

  ArrayList<ColumnDefinition> getCalculatedColumns();

  ArrayList<ColumnDefinition> getColumnDefinitions();

  ArrayList<Integer> getOutputs();

  ArrayList<Integer> getOutputs( int id );

  OutputMode getOutputMode();

  OutputMode getOutputMode( int id );

  TableModel listParameters();

  boolean hasIterableParameterValues( final QueryOptions queryOptions ) throws QueryException;

  void storeDescriptor( DataAccessConnectionDescriptor descriptor );

  void setQuery( String query );

  void accept( DomVisitor v, Element ele );

}
