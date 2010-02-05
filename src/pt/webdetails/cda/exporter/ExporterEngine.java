package pt.webdetails.cda.exporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.UnsupportedDataAccessException;
import pt.webdetails.cda.utils.Util;

/**
 * Main engine class that will answer to calls
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:24:16 PM
 */
public class ExporterEngine
{


  private static final Log logger = LogFactory.getLog(ExporterEngine.class);
  private static ExporterEngine _instance;


  public ExporterEngine()
  {
    logger.info("Initializing CdaEngine");
    init();

  }


  public Exporter getExporter(String outputType) throws UnsupportedExporterException
  {

    try
    {

      final String className = "pt.webdetails.cda.exporter." +
          outputType.substring(0, 1).toUpperCase() + outputType.substring(1, outputType.length()) + "Exporter";

      final Class clazz = Class.forName(className);
      final Class[] params = {};

      final Exporter exporter = (Exporter) clazz.getConstructor(params).newInstance(new Object[]{});
      return exporter;

    }
    catch (Exception e)
    {
      throw new UnsupportedExporterException("Error initializing expoert class: " + Util.getExceptionDescription(e), e);
    }

  }


  private void init()
  {

  }


  public static synchronized ExporterEngine getInstance()
  {

    if (_instance == null)
    {
      _instance = new ExporterEngine();
    }

    return _instance;
  }
}