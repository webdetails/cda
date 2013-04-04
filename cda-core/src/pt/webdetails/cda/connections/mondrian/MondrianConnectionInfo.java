package pt.webdetails.cda.connections.mondrian;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 9, 2010
 * Time: 4:10:52 PM
 */
public interface MondrianConnectionInfo
{
  
  public String getCatalog();
  public String getUser();
  public String getPass();

  public String getMondrianRole();

  public String getRoleField();
  public String getUserField();
  public String getPasswordField();

}
