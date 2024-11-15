/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.utils.framework;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import pt.webdetails.cda.utils.PentahoHelper;
import pt.webdetails.cpf.utils.CharsetHelper;

/**
 * Boilerplate for a JSON content generator.<br> Legacy
 */
public abstract class JsonCallHandler {

  private static Log logger = LogFactory.getLog( JsonCallHandler.class );

  public static final String ENCODING = "UTF-8";
  public static final int INDENT_FACTOR = 2;

  private HashMap<String, Method> methods = new HashMap<String, Method>();

  public static class JsonResultFields {
    public static final String STATUS = "status";
    public static final String ERROR_MSG = "errorMsg";
    public static final String RESULT = "result";
  }

  public enum ResponseStatus {

    OK,
    ERROR;

    @Override public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public static abstract class Method {

    private String name;

    public abstract JSONObject execute( IParameterProvider requParam ) throws Exception;

    public void setName( String name ) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * @param name
   * @param method
   */
  protected void registerMethod( String name, Method method ) {
    method.setName( name );
    methods.put( name, method );
  }

  /**
   * Simple overridable method to limit access.
   *
   * @param method  Method being executed
   * @return
   */
  protected boolean hasPermission( Method method ) {
    return PentahoHelper.isAdmin( PentahoSessionHolder.getSession() );
  }

  public void handleCall( String methodName, IParameterProvider requParam, OutputStream out ) {

    JSONObject result = null;
    Method method = methods.get( methodName );

    try {
      if ( methodName == null ) {
        result = getErrorJson( "No method received." );
      } else if ( method == null ) {
        result = getErrorJson( MessageFormat.format( "Method {0} not found.", methodName ) );
      } else if ( !hasPermission( method ) ) {
        result = getErrorJson(
          MessageFormat.format(
            "Permission denied to call method {0}:{1}.",
            this.getClass().getName(), methodName ) );
      } else {
        try {
          result = method.execute( requParam );
        } catch ( JSONException e ) {
          logger.error( MessageFormat.format( "Error building JSON response in method {0}.", methodName ), e );
        } catch ( Exception e ) {
          logger.error( MessageFormat.format( "Error executing method {0}.", methodName ), e );
          result = createJsonResultFromException( e );
        }
      }
      if ( result != null ) {
        IOUtils.write( result.toString( INDENT_FACTOR ), out, CharsetHelper.getEncoding() );
      }
    } catch ( JSONException e ) {
      logger.error( "Error building JSON response", e );
    } catch ( IOException e ) {
      logger.error( "Error writing to output stream", e );
    }
  }

  public static JSONObject createJsonResultFromException( Exception exc ) throws JSONException {
    JSONObject result = new JSONObject();

    result.put( JsonResultFields.STATUS, ResponseStatus.ERROR );
    result.put( JsonResultFields.ERROR_MSG, exc.getMessage() );

    return result;
  }

  public static JSONObject getErrorJson( String msg ) throws JSONException {
    JSONObject result = new JSONObject();
    result.put( JsonResultFields.STATUS, ResponseStatus.ERROR );
    result.put( JsonResultFields.ERROR_MSG, msg );
    return result;
  }

  public static JSONObject getOKJson( Object obj ) throws JSONException {
    JSONObject result = new JSONObject();
    result.put( JsonResultFields.STATUS, ResponseStatus.OK );
    result.put( JsonResultFields.RESULT, obj );
    return result;
  }


}
