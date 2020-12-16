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

package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cda.utils.AuditHelper;
import pt.webdetails.cpf.SimpleContentGenerator;

public class CdaContentGenerator extends SimpleContentGenerator {

  private boolean edit = false;

  private static Log logger = LogFactory.getLog( CdaContentGenerator.class );
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;

  @Override
  public void createContent() throws Exception {
    CdaUtils utils = new CdaUtils();
    String path = getPathParameterAsString( MethodParams.PATH, "" );

    AuditHelper auditHelper = new AuditHelper( CdaContentGenerator.class, userSession, this );
    IParameterProvider requestParams = parameterProviders.get( MethodParams.REQUEST );

    try ( AuditHelper.QueryAudit qa = auditHelper.startQuery( path, requestParams ) ) {
      if ( qa.getRequestId() != null ) {
        setInstanceId( qa.getRequestId().toString() );
      }

      if ( edit ) {
        utils.editFile( path, getResponse() );
      } else {
        utils.previewQuery( path, getResponse() );
      }
    }
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
