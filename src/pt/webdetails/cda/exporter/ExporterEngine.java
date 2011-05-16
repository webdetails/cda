package pt.webdetails.cda.exporter;

import java.text.MessageFormat;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
  
  public enum OutputType{
  	
  	JSON("json"),
  	XML("xml"),
  	CSV("csv"),
  	XLS("xls"),
  	HTML("html");
  	
  	private String type;
  	OutputType(String type){this.type = type;}
  	public String toString(){ return type;}
  	
  	public static OutputType parse(String typeStr){
  	  for(OutputType outputType: OutputType.values()){
  	    if(StringUtils.equalsIgnoreCase(outputType.toString(), typeStr)){
  	      return outputType;
  	    }
  	  }
  	  return null;
  	}
  }

  public ExporterEngine()
  {
    logger.info("Initializing CdaEngine");
    init();

  }

  
  public Exporter getExporter(final String outputType) throws UnsupportedExporterException
  {
    return getExporter(outputType, null);
  }

  public Exporter getExporter(final String outputType, final HashMap<String, String> extraSettings) throws UnsupportedExporterException
  {
    Exporter exporter = getExporter( OutputType.parse(outputType), extraSettings);
    if(exporter != null)
    {
      return exporter;
    }
    //else fallback to old version
    logger.info(MessageFormat.format("getExporter for {0} failed, falling back to old version", outputType));

    try
    {

      final String className = "pt.webdetails.cda.exporter."
              + outputType.substring(0, 1).toUpperCase() + outputType.substring(1, outputType.length()) + "Exporter";

      final Class clazz = Class.forName(className);
      final Class[] params =
      {
        HashMap.class
      };

      exporter = (Exporter) clazz.getConstructor(params).newInstance(new Object[]
              {
                extraSettings
              });
      return exporter;

    }
    catch (Exception e)
    {
      throw new UnsupportedExporterException("Error initializing export class: " + Util.getExceptionDescription(e), e);
    }

  }
  
  private Exporter getExporter(OutputType type, HashMap<String, String> extraSettings)
  {
    if(type == null) return null;
    
    switch(type){
      case CSV:
        return new CsvExporter(extraSettings);
      case HTML:
        return new HtmlExporter(extraSettings);
      case JSON:
        return new JsonExporter(extraSettings);
      case XLS:
        return new XlsExporter(extraSettings);
      case XML:
        return new XmlExporter(extraSettings);
      default:
        return null;
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
