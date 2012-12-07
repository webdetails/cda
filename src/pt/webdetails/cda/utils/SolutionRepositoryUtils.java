package pt.webdetails.cda.utils;

import java.util.Arrays;
import java.util.List;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;

import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;

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
    
    return null;
    
    /*String[] extensions = new String[] {EXTENSION};

    RepositoryFileTree cdaTree = RepositoryAccess.getRepository(userSession).getFullSolutionTree(FileAccess.READ, Arrays.asList(extensions), false);
    
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
*/
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
