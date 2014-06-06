/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.SimpleContentGenerator;

public class CdaContentGenerator extends SimpleContentGenerator
{

  private boolean edit = false;

  private static Log logger = LogFactory.getLog(CdaContentGenerator.class);
  public static final String PLUGIN_NAME = "cda";
  private static final long serialVersionUID = 1L;

  @Override
  public void createContent() throws Exception {
    CdaUtils utils = new CdaUtils();
    String path = getPathParameterAsString(MethodParams.PATH, "");

    if (edit) {
      utils.editFile( path, getResponse() );
    }
    else {
      utils.previewQuery( path, getResponse() );
    }
  }

  @Override
  public Log getLogger()
  {
    return logger;
  }

  @Override
  public String getPluginName() {
    return "cda";
  }


  /**
   * @return if is in edit mode
   */
  public boolean isEdit()
  {
    return edit;
  }


  /**
   * @param edit edit mode
   */
  public void setEdit(boolean edit)
  {
    this.edit = edit;
  }


  private class MethodParams {
    public static final String PATH = "path";
  }
}
