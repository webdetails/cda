package pt.webdetails.cda.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.repository.api.FileAccess;


/**
 * Serves the previewer page.
 */
public class Previewer extends BaseService{

  //TODO: previewer dependencies, separate header from rest of page;
  private static Log logger = LogFactory.getLog(Previewer.class);
  private static final String PREVIEWER_SOURCE = "/previewer/previewer.html";
  
  public String previewQuery() throws Exception
  {
    final String previewerPath = "system/" + getPluginId() + PREVIEWER_SOURCE;
    return getResourceAsString(previewerPath, FileAccess.EXECUTE);
  }

}
