/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
