/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.filetests;

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
