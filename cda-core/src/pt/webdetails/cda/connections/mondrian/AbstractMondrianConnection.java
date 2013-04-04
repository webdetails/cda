package pt.webdetails.cda.connections.mondrian;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
//XXX remove
//import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
//import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
//import org.pentaho.platform.engine.core.system.PentahoSystem;
//import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;

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

  private static final Log logger = LogFactory.getLog(AbstractMondrianConnection.class);
	
  public AbstractMondrianConnection()
  {
  }

  public AbstractMondrianConnection(String id)
  {
    super(id);
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
		  if (PentahoSystem.getObjectFactory().objectDefined(MDXConnection.MDX_CONNECTION_MAPPER_KEY)) {
			  final IConnectionUserRoleMapper mondrianUserRoleMapper =
				  PentahoSystem.get(IConnectionUserRoleMapper.class, MDXConnection.MDX_CONNECTION_MAPPER_KEY, null);

			  final String[] validMondrianRolesForUser =
				  mondrianUserRoleMapper.mapConnectionRoles(PentahoSessionHolder.getSession(), "solution:" + catalog.replaceAll("solution/",""));
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
				  logger.debug("Assembled role: " + buff.toString() + " for catalog: " + catalog);
				  return buff.toString();
			  }
		  }
	  }
	  catch (Exception e)
	  {
		  logger.error("Error assembling role for mondrian connection", e);
	  }
	  return "";
  }
}
