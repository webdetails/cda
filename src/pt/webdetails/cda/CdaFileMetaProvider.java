/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.SolutionFileMetaAdapter;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cpf.InterPluginCall;

/**
 * Parses a Dom4J document and creates an IFileInfo object containing the
 * xcda info.
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class CdaFileMetaProvider extends SolutionFileMetaAdapter {
  
  private static final Log logger = LogFactory.getLog(InterPluginCall.class);
  public CdaFileMetaProvider() {
  }

  public IFileInfo getFileInfo(ISolutionFile solutionFile, InputStream in)
  {
    try {
      Document doc = XmlDom4JHelper.getDocFromStream(in);
      if (doc == null) {
        return null;
      }
      String result = "data";  //$NON-NLS-1$
      String author = XmlDom4JHelper.getNodeText("/cda/author", doc, "");  //$NON-NLS-1$ //$NON-NLS-2$
      String description = XmlDom4JHelper.getNodeText("/cda/description", doc, "");  //$NON-NLS-1$ //$NON-NLS-2$
      String icon = XmlDom4JHelper.getNodeText("/cda/icon", doc, "");  //$NON-NLS-1$ //$NON-NLS-2$
      String title = XmlDom4JHelper.getNodeText("/cda/title", doc, "");  //$NON-NLS-1$ //$NON-NLS-2$

      IFileInfo info = new FileInfo();
      info.setAuthor(author);
      info.setDescription(description);
      info.setDisplayType(result);
      info.setIcon(icon);
      info.setTitle(title);
      return info;
    } catch (Exception e) {
      if (logger != null) {
        logger.error(getClass().toString(), e);
      }
      return null;
    }
  }
}
