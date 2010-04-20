package pt.webdetails.cda.connections.mondrian;

import java.util.ArrayList;
import org.dom4j.Element;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractMondrianConnection extends AbstractConnection implements MondrianConnection {

  public AbstractMondrianConnection() {
  }

  public AbstractMondrianConnection(final Element connection) throws InvalidConnectionException {

    super(connection);

  }

  @Override
  public ConnectionType getGenericType() {
    return ConnectionType.MDX;
  }

  @Override
  public ArrayList<PropertyDescriptor> getProperties() {
   ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
   properties.add(new PropertyDescriptor("id",PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
   properties.add(new PropertyDescriptor("cube",PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
   properties.add(new PropertyDescriptor("catalog",PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
   return properties;
  }
}
