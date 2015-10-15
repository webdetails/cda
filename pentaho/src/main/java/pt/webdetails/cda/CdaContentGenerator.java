/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.audit.CpfAuditHelper;

import java.util.UUID;

public class CdaContentGenerator extends SimpleContentGenerator {

  private boolean edit = false;

  private static Log logger = LogFactory.getLog( CdaContentGenerator.class );
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;

  @Override
  public void createContent() throws Exception {
    CdaUtils utils = new CdaUtils();

    IParameterProvider requestParams = parameterProviders.get( MethodParams.REQUEST );
    String path = getPathParameterAsString( MethodParams.PATH, "" );

    long start = System.currentTimeMillis();


    UUID uuid = CpfAuditHelper.startAudit( getPluginName(), path, getObjectName(),
      this.userSession, this, requestParams );


    if ( edit ) {
      utils.editFile( path, getResponse() );
    } else {
      utils.previewQuery( path, getResponse() );
    }


    long end = System.currentTimeMillis();
    CpfAuditHelper.endAudit( getPluginName(), path, getObjectName(), this.userSession, this, start, uuid, end );
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  public String getPluginName() {
    return PLUGIN_NAME;
  }

  public String getObjectName() {
    return this.getClass().getName();
  }


  /**
   * @return if is in edit mode
   */
  public boolean isEdit() {
    return edit;
  }


  /**
   * @param edit edit mode
   */
  public void setEdit( boolean edit ) {
    this.edit = edit;
  }


  private class MethodParams {
    public static final String PATH = "path";
    public static final String REQUEST = "request";
  }

}
