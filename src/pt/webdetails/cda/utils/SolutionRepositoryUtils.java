package pt.webdetails.cda.utils;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

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

  public TableModel getCdaList(final IPentahoSession userSession)
  {

    logger.debug("Getting CDA list");

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    Document cdaTree = solutionRepository.getFullSolutionTree(ISolutionRepository.ACTION_EXECUTE, new CdaFilter());
    //  System.out.println(cdaTree.asXML());
    logger.debug("Processing list");


    return null;
  }


  private class CdaFilter implements ISolutionFilter
  {

    public boolean keepFile(final ISolutionFile iSolutionFile, final int i)
    {
      if (iSolutionFile.isDirectory())
      {
        return true;
      }
      else
      {
        return iSolutionFile.getExtension().equals(EXTENSION);
      }
    }
  }
}
