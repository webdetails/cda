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
