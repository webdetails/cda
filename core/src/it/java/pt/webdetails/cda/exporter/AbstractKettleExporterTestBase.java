package pt.webdetails.cda.exporter;

import org.junit.BeforeClass;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import pt.webdetails.cda.utils.test.CdaTestHelper;

import static pt.webdetails.cda.utils.test.CdaTestHelper.getMockEnvironment;

public class AbstractKettleExporterTestBase {

  @BeforeClass
  public static void init() throws KettleException {
    // not great but about half the time of a full init;
    // will not work where reporting libraries are needed
    KettleEnvironment.init();
    CdaTestHelper.initBareEngine( getMockEnvironment() );
  }
}
