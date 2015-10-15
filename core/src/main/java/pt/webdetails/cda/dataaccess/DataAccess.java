/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import org.dom4j.Element;
import pt.webdetails.cda.xml.DomVisitor;

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

  DataAccessEnums.ACCESS_TYPE getAccess();

  boolean isCacheEnabled();

  int getCacheDuration();

  CdaSettings getCdaSettings();

  void setCdaSettings( CdaSettings cdaSettings );

  TableModel doQuery( QueryOptions queryOptions ) throws QueryException;

  ColumnDefinition getColumnDefinition( int idx );

  ArrayList<ColumnDefinition> getCalculatedColumns();

  ArrayList<ColumnDefinition> getColumnDefinitions();

  ;

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
