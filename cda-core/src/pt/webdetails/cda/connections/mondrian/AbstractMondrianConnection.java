package pt.webdetails.cda.connections.mondrian;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cda.CdaEngine;
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
    
    IMondrianRoleMapper roleMapper = (IMondrianRoleMapper)CdaEngine.getInstance().getBeanFactory().getBean("IMondrianRoleMapper");
	  try
	  {
            if (roleMapper != null)
                return roleMapper.getRoles(catalog);

	  }
	  catch (Exception e)
	  {
		  logger.error("Error assembling role for mondrian connection", e);
	  }
	  return "";
  }
}
