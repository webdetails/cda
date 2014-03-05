package pt.webdetails.cda;

import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;

public class CdaPluginEnvironment extends PentahoPluginEnvironment {

  private static CdaPluginEnvironment instance = new CdaPluginEnvironment();

  private CdaPluginEnvironment() {  }

  public static void init() {
    PluginEnvironment.init( instance );
  }

  public static CdaPluginEnvironment getInstance() {
    return instance;
  }

  public String getPluginId() {
    return "cda";
  }

}
