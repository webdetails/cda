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
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.settings.SettingsManager;

/**
 * Created by IntelliJ IDEA. User: pedro Date: Mar 25, 2010 Time: 5:06:02 PM
 */
public class DataAccessConnectionDescriptor {

  private static final Log logger = LogFactory.getLog( SettingsManager.class );
  private String name;
  private Connection conn;
  private AbstractDataAccess dataAccess;

  public DataAccessConnectionDescriptor() {
  }

  public DataAccessConnectionDescriptor( final String name ) {
    this();
    setName( name );
  }

  public void setConnection( Connection conn ) {
    this.conn = conn;
  }

  public void setDataAccess( AbstractDataAccess da ) {
    this.dataAccess = da;
  }

  public ArrayList<PropertyDescriptor> getDescriptors() {

    ArrayList<PropertyDescriptor> collapsedInfo = new ArrayList<PropertyDescriptor>();
    collapsedInfo.addAll( this.conn.getProperties() );
    collapsedInfo.addAll( this.dataAccess.getInterface() );

    return collapsedInfo;
  }

  public String getName() {
    return name;
  }

  public void setName( final String name ) {
    this.name = name;
  }

  public String toJSON() {
    List<PropertyDescriptor> dataAccessInfo = this.dataAccess.getInterface();
    List<PropertyDescriptor> connectionInfo = this.conn.getProperties();
    StringBuilder output = new StringBuilder();
    if ( dataAccessInfo.size() > 0 ) {
      output.append( "\"" + name + "\": {\n" );
      /*
       * Metadata block
       */
      output.append( "\t\"metadata\": {\n" );
      output.append( "\t\t\"name\": \"" + dataAccess.getType() + ( this.conn.getGenericType() != ConnectionType.NONE ?
        " over " + conn.getType() : "" ) + "\",\n" );
      output.append(
        this.conn.getGenericType() != ConnectionType.NONE ? "\t\t\"conntype\": \"" + conn.getTypeForFile() + "\",\n" :
          "" );
      output.append( "\t\t\"datype\": \"" + dataAccess.getType() + "\",\n" );
      output.append( "\t\t\"group\": \"" + this.conn.getGenericType().toString() + "\",\n" );
      output.append( "\t\t\"groupdesc\": \"" + ( this.conn.getGenericType() != ConnectionType.NONE ?
        this.conn.getGenericType().toString() : "Compound" ) + " Queries\",\n" );
      output.append( "\t},\n" );
      /* 
       * Definition block
       */
      output.append( "\t\"definition\": {\n" );
      if ( connectionInfo.size() > 0 ) {
        output.append( "\t\t\"connection\": {\n" );
        for ( PropertyDescriptor prop : connectionInfo ) {
          output.append(
            "\t\t\t\"" + prop.getName() + "\": {\"type\": \"" + prop.getType() + "\", \"placement\": \"" + prop
              .getPlacement() + "\"},\n" );
        }
        output.append( "\t\t},\n" );
      }
      output.append( "\t\t\"dataaccess\": {\n" );
      for ( PropertyDescriptor prop : dataAccessInfo ) {
        output.append(
          "\t\t\t\"" + prop.getName() + "\": {\"type\": \"" + prop.getType() + "\", \"placement\": \"" + prop
            .getPlacement() + "\"},\n" );
      }
      output.append( "\t\t}\n\t}\n}" );
    }
    return output.toString().replaceAll( ",\n(\t*)}", "\n$1}" );
  }

  public static DataAccessConnectionDescriptor[] fromClass( Class<? extends DataAccess> dataAccess ) throws Exception {
    ArrayList<DataAccessConnectionDescriptor> descriptors = new ArrayList<DataAccessConnectionDescriptor>();
    AbstractDataAccess sample = (AbstractDataAccess) dataAccess.newInstance();
    Connection[] conns = sample.getAvailableConnections();
    if ( conns.length > 0 ) {
      for ( Connection conn : conns ) {
        try {
          String name =
            sample.getType() + ( !( conn.getGenericType().equals( ConnectionType.NONE ) ) ? ( "_" + conn.getType() ) :
              "" );
          DataAccessConnectionDescriptor descriptor = new DataAccessConnectionDescriptor( name );
          descriptor.setDataAccess( sample );
          descriptor.setConnection( conn );
          descriptors.add( descriptor );
        } catch ( UnsupportedOperationException e ) {
          logger.warn( "Failed to generate a descriptor for " + sample.getType() + "_" + conn.getType() );
        }
      }
    }
    return descriptors.toArray( new DataAccessConnectionDescriptor[ descriptors.size() ] );
  }
}
