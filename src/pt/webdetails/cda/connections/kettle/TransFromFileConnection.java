package pt.webdetails.cda.connections.kettle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransFromFileProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.platform.plugin.connection.PentahoKettleTransFromFileProducer;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.ConnectionCatalog;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 14:02:09
 *
 * @author Thomas Morgner.
 */
public class TransFromFileConnection extends AbstractConnection implements KettleConnection
{

  private static final Log logger = LogFactory.getLog(TransFromFileConnection.class);
  private TransFromFileConnectionInfo connectionInfo;


  public TransFromFileConnection()
  {
  }


  public TransFromFileConnection(final Element connection)
          throws InvalidConnectionException
  {
    super(connection);
  }


  /**
   * @param query the name of the transformation step that should be polled.
   * @return the initialized transformation producer.
   */
  public KettleTransformationProducer createTransformationProducer(final String query)
  {
    if (CdaEngine.getInstance().isStandalone())
    {
      return new KettleTransFromFileProducer("",
              connectionInfo.getTransformationFile(),
              query, null, null, connectionInfo.getDefinedArgumentNames(),
              connectionInfo.getDefinedVariableNames());
    }




    try {
      Constructor c = PentahoKettleTransFromFileProducer.class.getConstructor( new Class[]{String.class, String.class,
              String.class, String.class, String.class, String[].class, ParameterMapping[].class} );
      Object obj = c.newInstance( "", connectionInfo.getTransformationFile(), query, null, null, connectionInfo.getDefinedArgumentNames(),
              connectionInfo.getDefinedVariableNames() );
      return (PentahoKettleTransFromFileProducer) obj;
    } catch ( Exception  e ) {

      try {
        Class formulaArgument =
                Class.forName( "org.pentaho.reporting.engine.classic.extensions.datasources.kettle.FormulaArgument" );
        Class formulaParameter =
                Class.forName( "org.pentaho.reporting.engine.classic.extensions.datasources.kettle.FormulaParameter" );
        Method convertFormulaArgument = formulaArgument.getMethod( "convert", new Class[] {String[].class} );
        Method convertParameterArgument = formulaParameter.getMethod( "convert", new Class[] {ParameterMapping[].class} );
        Constructor c = PentahoKettleTransFromFileProducer.class.getConstructors()[0];
        Object obj = c.newInstance( "",
                connectionInfo.getTransformationFile(),
                query, null, null,
                formulaArgument.cast( convertFormulaArgument.invoke( null,
                        new Object[]{ connectionInfo.getDefinedArgumentNames() } ) ),
                formulaParameter.cast( convertParameterArgument.invoke( null,
                        new Object[]{ connectionInfo.getDefinedVariableNames() } ) ) );
        return (PentahoKettleTransFromFileProducer) obj;
      } catch ( InstantiationException ie ) {
        logger.error( "Error while creating PentahoKettleTransFromFileProducer", ie );
      } catch ( InvocationTargetException ite ) {
        logger.error( "Error while creating PentahoKettleTransFromFileProducer", ite );
      } catch ( IllegalAccessException iae) {
        logger.error( "Error while creating PentahoKettleTransFromFileProducer", iae );
      } catch ( ClassNotFoundException cnfe ) {
        logger.error( "Error while creating PentahoKettleTransFromFileProducer", cnfe );
      } catch ( NoSuchMethodException nsme ) {
        logger.error( "Error while creating PentahoKettleTransFromFileProducer", nsme );
      }
      return null;
    }
  }


  public ConnectionCatalog.ConnectionType getGenericType()
  {
    return ConnectionCatalog.ConnectionType.KETTLE;
  }


  protected void initializeConnection(final Element connection) throws InvalidConnectionException
  {
    connectionInfo = new TransFromFileConnectionInfo(connection);
  }


  public String getType()
  {
    return "kettleTransFromFile";
  }


  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final TransFromFileConnection that = (TransFromFileConnection) o;

    if (connectionInfo != null ? !connectionInfo.equals(that.connectionInfo) : that.connectionInfo != null)
    {
      return false;
    }

    return true;
  }


  public int hashCode()
  {
    return connectionInfo != null ? connectionInfo.hashCode() : 0;
  }


  @Override
  public ArrayList<PropertyDescriptor> getProperties()
  {
    final ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("ktrFile", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("variables", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    return properties;
  }


  @Override
  public String getTypeForFile()
  {
    return "kettle.TransFromFile";
  }

  public TransFromFileConnectionInfo getConnectionInfo() {
	  return connectionInfo;
  }
}
