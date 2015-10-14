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

package pt.webdetails.cda.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.CompoundDataAccess;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.MdxDataAccess;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * This class implements traversal of the CDA Domainmodel and visits all its elements
 * <p/>
 * Grouping elements are added to the dom here
 *
 * @author mgie
 */
public class DomTraversalHelper {

  private Element root;

  public Document traverse( CdaSettings cda ) {

    // The root element is always the same, so it does not need to be
    // visited
    Document doc = DocumentFactory.getInstance().createDocument( "UTF-8" );
    doc.addElement( "CDADescriptor" );

    //should use a copy of the dom so it doesnt destroy those that come from a file
    this.root = doc.getRootElement();

    Element consEle = this.root.addElement( "DataSources" );

    // Traverse the connections.
    Iterator<Entry<String, Connection>> it = cda.getConnectionsMap().entrySet().iterator();
    while ( it.hasNext() ) {
      @SuppressWarnings( "rawtypes" )
      Map.Entry pairs = (Map.Entry) it.next();
      Connection con = (Connection) pairs.getValue();
      con.accept( new DomVisitor(), consEle );
    }

    // Traverse the dataacesses
    Iterator<Entry<String, DataAccess>> iter = cda.getDataAccessMap().entrySet().iterator();
    while ( iter.hasNext() ) {
      @SuppressWarnings( "rawtypes" )
      Map.Entry pairs = (Map.Entry) iter.next();
      DataAccess da = (DataAccess) pairs.getValue();

      String daType = "DataAccess";
      if ( da instanceof CompoundDataAccess ) {
        daType = "CompoundDataAccess";
      }
      Element daEle = this.root.addElement( daType );

      da.accept( new DomVisitor(), daEle );

      // Traverse the columns
      ArrayList<ColumnDefinition> columns = da.getColumnDefinitions();

      if ( !columns.isEmpty() ) {
        Element colsEle = daEle.addElement( "Columns" );
        for ( ColumnDefinition columnDefinition : columns ) {
          columnDefinition.accept( new DomVisitor(), colsEle );
        }
      }

      // Traverse the parameters
      if ( da instanceof AbstractDataAccess ) {
        ArrayList<Parameter> params = ( (AbstractDataAccess) da ).getParameters();
        if ( !params.isEmpty() ) {
          Element paramsEle = daEle.addElement( "Parameters" );
          for ( Parameter param : params ) {
            param.accept( new DomVisitor(), paramsEle );
          }
        }
      }

      // Banded Mode
      if ( da instanceof MdxDataAccess && ( (MdxDataAccess) da ).getBandedMode()
        .equals( MdxDataAccess.BANDED_MODE.COMPACT ) ) {
        daEle.addElement( "BandedMode" ).addText(
          MdxDataAccess.BANDED_MODE.COMPACT.name().toLowerCase() );
      }

      // Outputs
      if ( !da.getOutputs().isEmpty() ) {
        String idxs = StringUtils.join( da.getOutputs().toArray(), "," );
        Element output = daEle.addElement( "Output" );
        if ( da.getOutputMode().equals( DataAccess.OutputMode.EXCLUDE ) ) {
          output.addAttribute( "mode", da.getOutputMode().name().toLowerCase() );
        }
        output.addAttribute( "indexes", idxs );
      }
    }

    return doc;
  }

  /**
   * Writes xml of a cda to an outputstream
   *
   * @param cda
   * @param out
   * @throws IOException
   */
  public void writeToOutputStream( CdaSettings cda, OutputStream out ) throws IOException {
    Document doc = traverse( cda );
    XMLWriter writer = new XMLWriter( out );
    writer.write( doc );
  }

}
