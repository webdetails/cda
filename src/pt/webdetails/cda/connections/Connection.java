package pt.webdetails.cda.connections;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.sql.ConnectionProvider;
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

  public CdaSettings getCdaSettings();

  public void setCdaSettings(CdaSettings cdaSettings);



}
