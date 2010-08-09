package pt.webdetails.cda.connections.mondrian;

import java.util.ArrayList;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
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
public abstract class AbstractMondrianConnection extends AbstractConnection implements MondrianConnection
{

  public AbstractMondrianConnection()
  {
  }


  public AbstractMondrianConnection(final Element connection) throws InvalidConnectionException
  {

    super(connection);

  }


  @Override
  public ConnectionType getGenericType()
  {
    return ConnectionType.MDX;
  }


  @Override
  public ArrayList<PropertyDescriptor> getProperties()
  {
    final ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("catalog", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    return properties;
  }


  protected String assembleRole(String catalog)
  {
    try
    {
      final IConnectionUserRoleMapper mondrianUserRoleMapper =
              PentahoSystem.get(IConnectionUserRoleMapper.class, "Mondrian-UserRoleMapper", null);
      if (mondrianUserRoleMapper != null)
      {
      }
      final String[] validMondrianRolesForUser =
              mondrianUserRoleMapper.mapConnectionRoles(PentahoSessionHolder.getSession(), "solution:" + catalog);
      if ((validMondrianRolesForUser != null) && (validMondrianRolesForUser.length > 0))
      {
        final StringBuffer buff = new StringBuffer();
        for (int i = 0; i < validMondrianRolesForUser.length; i++)
        {
          final String aRole = validMondrianRolesForUser[i];
          // According to http://mondrian.pentaho.org/documentation/configuration.php
          // double-comma escapes a comma
          if (i > 0)
          {
            buff.append(",");
          }
          buff.append(aRole.replaceAll(",", ",,"));
        }
        return buff.toString();
      }
      else
      {
        return "";
      }
    }
    catch (Exception e)
    {
      return "";
    }
  }
}
