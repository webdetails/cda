package pt.webdetails.cda.connections.olap4j;

import org.dom4j.Element;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.02.2010
 * Time: 12:59:29
 *
 * @author Thomas Morgner.
 */
public class OlapJndiConnectionInfo extends pt.webdetails.cda.connections.JndiConnectionInfo
{
  private String roleField;

  public OlapJndiConnectionInfo(final String roleFiled, String jndi) {
    super(jndi, null, null, null, null);
  
  }
  public OlapJndiConnectionInfo(final Element connection) {
    
    super(connection);

    final String roleFormula = (String) connection.selectObject("string(./RoleField)");
    
    if (StringUtils.isEmpty(roleFormula) == false)
    {
      setRoleField(roleFormula);
    }

  }

  public String getRoleField()
  {
    return roleField;
  }

  public void setRoleField(final String roleField)
  {
    this.roleField = roleField;
  }
}
