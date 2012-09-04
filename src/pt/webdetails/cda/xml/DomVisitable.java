/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.xml;

import org.dom4j.Element;

/**
 * This interface indicates that a domain object can be visited
 * by DomVisitor in the DOM-building process
 * 
 * @author mgie
 *
 */
public interface DomVisitable {
	
	public void accept(DomVisitor v, Element ele);

}
