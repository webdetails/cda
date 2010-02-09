package pt.webdetails.cda;

import org.pentaho.reporting.engine.classic.core.ClassicEngineInfo;
import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;

/**
 * Todo: Document me!
 * <p/>
 * Date: 09.02.2010
 * Time: 17:51:47
 *
 * @author Thomas Morgner.
 */
public class CdaInfo extends ProjectInformation
{
  private static CdaInfo instance;

  public static synchronized CdaInfo getInstance()
  {
    if (instance == null)
    {
      instance = new CdaInfo();
      instance.initialize();
    }
    return instance;
  }

  public CdaInfo()
  {
    super("cda", "CDA - Community Data Access");
  }

  private void initialize()
  {
    setLicenseName("LGPL");
    setInfo("http://code.google.com/p/pentaho-cda/");
    setCopyright("(C)opyright 2006-2007, by WebDetails and Contributors");

    setBootClass("pt.webdetails.cda.CdaBoot");

    addLibrary(ClassicEngineInfo.getInstance());
  }
}
