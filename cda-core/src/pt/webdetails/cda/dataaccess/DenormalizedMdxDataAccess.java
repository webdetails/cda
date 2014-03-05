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

package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DenormalizedMDXDataFactory;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class DenormalizedMdxDataAccess extends MdxDataAccess {

//  private static final Log logger = LogFactory.getLog(DenormalizedMdxDataAccess.class);

  public DenormalizedMdxDataAccess(final Element element) {
    super(element);
  }

  public DenormalizedMdxDataAccess() {
  }

  protected AbstractNamedMDXDataFactory createDataFactory() {
    return new DenormalizedMDXDataFactory();
  }

  public String getType() {
    return "denormalizedMdx";
  }
}
