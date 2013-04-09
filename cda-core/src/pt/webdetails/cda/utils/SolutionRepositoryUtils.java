/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.utils;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    IRepositoryAccess repository = (IRepositoryAccess) CdaEngine.getInstance().getBeanFactory().getBean("IRepositoryAccess");
    IRepositoryFile[] cdaTree = repository.getPluginFiles("/",FileAccess.READ);
    @SuppressWarnings("unchecked")

    List<IRepositoryFile> cdaFiles = new ArrayList<IRepositoryFile>(Arrays.asList(cdaTree));
       

    final int rowCount = cdaFiles.size();

    // Define names and types
    final String[] colNames = {"name", "path"};
    final Class<?>[] colTypes = {String.class, String.class};
    final TypedTableModel typedTableModel = new TypedTableModel(colNames, colTypes, rowCount);

    for (IRepositoryFile file : cdaFiles)
    {
      typedTableModel.addRow(new Object[]{file.getFileName(), file.getFullPath()});

    }

    return typedTableModel;

  }
}
