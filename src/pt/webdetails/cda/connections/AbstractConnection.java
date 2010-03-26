package pt.webdetails.cda.connections;

import java.util.ArrayList;
import java.util.HashMap;

import org.dom4j.Element;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 5:09:59 PM
 */
public abstract class AbstractConnection implements Connection {

  private String id;
  private CdaSettings cdaSettings;
  

  public AbstractConnection()
  {
  }
  
  public AbstractConnection(final Element connection) throws InvalidConnectionException {

    id = connection.attributeValue("id");

    initializeConnection(connection);

  }

  public AbstractConnection(final HashMap settings) throws InvalidConnectionException {

    // TODO

   }



  protected abstract void initializeConnection(Element connection) throws InvalidConnectionException;


  public String getId() {
    return id;
  }

  public abstract String getType();


  public CdaSettings getCdaSettings() {
    return cdaSettings;
  }

  public void setCdaSettings(final CdaSettings cdaSettings)
  {
    this.cdaSettings = cdaSettings;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(final Object obj);

  public ArrayList<PropertyDescriptor> getProperties() {
    // TODO: Actually implement this
    throw new UnsupportedOperationException("Not implemented yet!");
  }
}
