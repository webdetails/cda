package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;

import org.dom4j.Element;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor.Type;
import pt.webdetails.cda.xml.DomVisitable;
import pt.webdetails.cda.xml.DomVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 16, 2010
 * Time: 11:36:05 PM
 */
public abstract class CompoundDataAccess extends AbstractDataAccess implements DomVisitable{

  public CompoundDataAccess(final Element element) {
    super(element);
  }

  public CompoundDataAccess() {
  }

  public void closeDataSource() throws QueryException {
    // not needed
  }

  public ConnectionType getConnectionType() {
    return ConnectionType.NONE;
  }

  @Override
  public ArrayList<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("parameters", Type.STRING, PropertyDescriptor.Placement.CHILD));
    return properties;
  }

  public void accept(DomVisitor xmlVisitor, Element root) {
	  xmlVisitor.visit((CompoundDataAccess)this, root);
  }
}
