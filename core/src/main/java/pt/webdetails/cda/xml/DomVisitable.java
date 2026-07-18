/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cda.xml;

import org.dom4j.Element;

/**
 * This interface indicates that a domain object can be visited by DomVisitor in the DOM-building process
 */
public interface DomVisitable {

  public void accept( DomVisitor v, Element ele );

}
