package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor.Type;
import pt.webdetails.cda.query.QueryOptions;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 16, 2010
 * Time: 11:36:05 PM
 */
public abstract class CompoundDataAccess extends AbstractDataAccess {

  public CompoundDataAccess(final Element element) {
    super(element);
  }

  public CompoundDataAccess() {
  }

  public void closeDataSource() throws QueryException {
    // not needed
  }

  private ParameterDataRow createParameterDataRowFromParameters(final ArrayList<Parameter> parameters) throws InvalidParameterException {

    final ArrayList<String> names = new ArrayList<String>();
    final ArrayList<Object> values = new ArrayList<Object>();

    for (final Parameter parameter : parameters) {
      names.add(parameter.getName());
      values.add(parameter.getValue());
    }

    final ParameterDataRow parameterDataRow = new ParameterDataRow(names.toArray(new String[]{}), values.toArray());

    return parameterDataRow;

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
}
