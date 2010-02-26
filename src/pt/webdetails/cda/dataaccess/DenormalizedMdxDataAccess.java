package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.DenormalizedMDXDataFactory;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class DenormalizedMdxDataAccess extends MdxDataAccess
{
  private static final Log logger = LogFactory.getLog(DenormalizedMdxDataAccess.class);

  public DenormalizedMdxDataAccess(final Element element)
  {
    super(element);
  }

  protected AbstractNamedMDXDataFactory createDataFactory()
  {
    return new DenormalizedMDXDataFactory();
  }

  public String getType()
  {
    return "denormalizedMdx";
  }
}
