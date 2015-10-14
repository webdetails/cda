/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.SolutionFileMetaAdapter;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * Parses a Dom4J document and creates an IFileInfo object containing the
 * xcda info.
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class CdaFileMetaProvider extends SolutionFileMetaAdapter {

  //hiding super.logger because not present in sugar
  private static Log logger = LogFactory.getLog(CdaFileMetaProvider.class);

  public CdaFileMetaProvider() {
  }

  public IFileInfo getFileInfo( ISolutionFile solutionFile, InputStream in ) {
    try {
      // XmlDom4JHelper would have throw if inputstream doesn't support marks
      final SAXReader saxReader = new SAXReader();
      final Document doc = saxReader.read( in );
      if ( doc == null ) {
        return null;
      }
      String result = "data"; //$NON-NLS-1$
      String author = XmlDom4JHelper.getNodeText( "/cda/author", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$
      String description = XmlDom4JHelper.getNodeText( "/cda/description", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$
      String icon = XmlDom4JHelper.getNodeText( "/cda/icon", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$
      String title = XmlDom4JHelper.getNodeText( "/cda/title", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$

      IFileInfo info = new FileInfo();
      info.setAuthor( author );
      info.setDescription( description );
      info.setDisplayType( result );
      info.setIcon( icon );
      info.setTitle( title );
      return info;

    } catch ( Exception e ) {
      if ( logger != null ) {
        logger.error( getClass().toString(), e );
      }
    }
    return null;
  }
}
