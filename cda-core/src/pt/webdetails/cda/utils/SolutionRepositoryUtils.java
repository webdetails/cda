/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.utils;

import java.util.List;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.repository.IRepositoryFileFilter;

/**
 * Utility class for SolutionRepository utils
 * User: pedro
 * Date: Feb 16, 2010
 * Time: 6:13:33 PM
 */
public class SolutionRepositoryUtils
{


  private static final Log logger = LogFactory.getLog(SolutionRepositoryUtils.class);
  private static final String EXTENSION = ".cda";

  private static SolutionRepositoryUtils _instance;

  public SolutionRepositoryUtils()
  {
  }


  public static synchronized SolutionRepositoryUtils getInstance()
  {

    if (_instance == null)
    {
      _instance = new SolutionRepositoryUtils();
    }

    return _instance;
  }

  public TableModel getCdaList(final IUserSession userSession)
  {

    logger.debug("Getting CDA list");
    //XXX IRepositoryAccess doesn't have getFullSolutionTree(FileAccess fa,CdaFilter cdaFltr) method
    IRepositoryAccess repository = (IRepositoryAccess) CdaEngine.getInstance().getBeanFactory().getBean("IRepositoryAccess");
    Document cdaTree = repository.getFullSolutionTree(FileAccess.READ, new CdaFilter());//RepositoryAccess.getRepository(userSession).getFullSolutionTree(FileAccess.READ, new CdaFilter());
    @SuppressWarnings("unchecked")
    List<Element> cdaFiles = cdaTree.selectNodes("//leaf[@isDir=\"false\"]");


    final int rowCount = cdaFiles.size();

    // Define names and types
    final String[] colNames = {"name", "path"};
    final Class<?>[] colTypes = {String.class, String.class};
    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (Object o : cdaFiles)
    {
      Element e = (Element) o;
      typedTableModel.addRow(new Object[]{e.selectSingleNode("leafText").getText(), e.selectSingleNode("path").getText()});

    }

    return typedTableModel;

  }


  private class CdaFilter implements IRepositoryFileFilter
  {
      
      public boolean keepFile(final IRepositoryFile iRepositoryFile)//XXX the signature of this method was: keepFile(final IRepositoryFile iRepositoryFile, final int i)
    {
      if (iRepositoryFile.isDirectory())
      {
        return true;
      }
      else
      {
        return iRepositoryFile.getExtension().equals(EXTENSION);
      }
    }

        @Override
        public boolean accept(IRepositoryFile irf) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
  }
}
