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


package pt.webdetails.cda;

import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;

public class CdaPluginEnvironment extends PentahoPluginEnvironment {

  private static CdaPluginEnvironment instance = new CdaPluginEnvironment();

  private CdaPluginEnvironment() {
  }

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
