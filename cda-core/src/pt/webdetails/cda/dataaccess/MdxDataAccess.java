/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

import org.dom4j.Element;

import java.util.List;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro Date: Feb 3, 2010 Time: 12:18:05 PM
 */
public class MdxDataAccess extends GlobalMdxDataAccess {

  /**
   * @param id
   * @param name
   * @param connectionId
   * @param query
   */
  public MdxDataAccess( String id, String name, String connectionId, String query ) {
    super( id, name, connectionId, query );
  }


  public MdxDataAccess( final Element element ) {
    super( element );
  }


  public MdxDataAccess() {
  }

  public String getType() {
    return "mdx";
  }

  @Override
  public List<PropertyDescriptor> getInterface() {
    List<PropertyDescriptor> properties = super.getInterface();
    properties.add(
      new PropertyDescriptor( "bandedMode", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD ) );
    return properties;
  }
}
