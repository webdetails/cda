package pt.webdetails.cda.connections;

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
