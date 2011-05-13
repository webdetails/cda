package pt.webdetails.cda.connections;

import java.util.ArrayList;

import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Holds the Connections Settings of a file
 *
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:44:01 PM
 */
public interface Connection {

  public String getId();

  public String getType();

  public ConnectionType getGenericType();

  public CdaSettings getCdaSettings();

  public void setCdaSettings(CdaSettings cdaSettings);

  public ArrayList<PropertyDescriptor> getProperties();

  public String getTypeForFile();
}
