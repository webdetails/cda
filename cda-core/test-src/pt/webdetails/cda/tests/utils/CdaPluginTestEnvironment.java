package pt.webdetails.cda.tests.utils;

import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public class CdaPluginTestEnvironment extends PluginEnvironment {
  private CdaTestingContentAccessFactory factory;

  public CdaPluginTestEnvironment( CdaTestingContentAccessFactory factory ) {
    this.factory = factory;
  }

  public IContentAccessFactory getContentAccessFactory() {
    return factory;
  }

  public IUrlProvider getUrlProvider() {
    throw new UnsupportedOperationException();
  }

  public PluginSettings getPluginSettings() {
    throw new UnsupportedOperationException();
  }

  public String getPluginId() {
    return "cda";
  }

  public IPluginCall getPluginCall( String pluginId, String service, String method ) {
    throw new UnsupportedOperationException();
  }
}
