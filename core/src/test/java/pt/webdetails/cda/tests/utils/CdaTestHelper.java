package pt.webdetails.cda.tests.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.ComparisonFailure;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.reporting.libraries.base.config.Configuration;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cpf.Util;

public class CdaTestHelper {

  /**
   * Ignores properties order in comparison, friendly output when failing.<br>
   * 
   * @param message
   * @param expectedJson parsing of this field will be very lenient, so don't mix with the actual result!
   * @param 
   */
  public static void assertJsonEquals( String message, String expectedJson, String actualJson ) throws Exception {
    ObjectMapper om = new ObjectMapper( );
    JsonNode parsedResult = om.readTree( actualJson );
    // expected ONLY
    om.configure( JsonParser.Feature.ALLOW_SINGLE_QUOTES, true );
    om.configure( JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
    JsonNode parsedExpected = om.readTree( expectedJson );
    if ( !parsedExpected.equals( parsedResult ) ) {
      try {
        // attempt to build a friendly comparison
        // om.configure( SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true );
        om.configure( SerializationConfig.Feature.INDENT_OUTPUT, true );
        Object first = om.treeToValue( parsedExpected, TreeMap.class );
        Object second = om.treeToValue( parsedResult, TreeMap.class );
        expectedJson = om.writeValueAsString( first );
        actualJson = om.writeValueAsString( second );
      } catch ( Exception e ) {
        // ignore and use originals
      }
      throw new ComparisonFailure( message, expectedJson, actualJson );
    }
  }

  /**
   * ignores indentation
   */
  public static void assertXmlEquals( String message, String xml1, String xml2 ) throws Exception {
    XMLUnit.setIgnoreWhitespace( true );
    XMLTestCase x = new XMLTestCase() {
    };
    x.assertXMLEqual( xml1, xml2 );
  }

  @SafeVarargs
  public static <T> boolean columnContains( TableModel table, int colIdx, T... values ) {
    HashSet<T> set = new HashSet<T>();
    set.addAll( Arrays.asList( values ) );
    for ( int i = 0; i < table.getRowCount(); i++ ) {
      set.remove( table.getValueAt( i, colIdx ) );
      if ( set.isEmpty() ) {
        return true;
      }
    }
    return false;
  }

  public static <T> boolean columnContains( TableModel table, int colIdx, T value ) {
    for ( int i = 0; i < table.getRowCount(); i++ ) {
      if ( value.equals( table.getValueAt( i, colIdx ) ) ) {
        return true;
      }
    }
    return false;
  }

  public static boolean numericEquals( Double first, Double second, double delta ) {
    if ( Objects.equals( first, second ) ) {
      return true;
    } else if ( first == null || second == null ) {
      return false;
    }
    return Math.abs( first - second ) < delta;
  }

  public static boolean numericEquals( BigDecimal first, BigDecimal second, BigDecimal delta ) {
    if ( Objects.equals( first, second ) ) {
      return true;
    } else if ( first == null || second == null ) {
      return false;
    }
    return first.subtract( second ).abs().compareTo( delta ) < 0;
  }

  public static boolean numericEquals( String actual, String expected, double delta ) {
    return numericEquals( Double.parseDouble( actual ), Double.parseDouble( expected ), delta );
  }

  public static ICdaEnvironment getMockEnvironment() {
    return getMockEnvironment( Collections.<String, String>emptyMap() );
  }

  public static ICdaEnvironment getMockEnvironment( final Map<String, String> configurationProperties ) {
    ICdaEnvironment env = mock( ICdaEnvironment.class );
    Configuration conf = mock( Configuration.class );
    when( conf.getConfigProperty( any( String.class ), any( String.class ) ) ).thenAnswer(
        new Answer<String>(  ) {
          @Override
          public String answer( InvocationOnMock invocation ) throws Throwable {
            String defaultValue = (String) invocation.getArguments()[1];
            String result = configurationProperties.get( invocation.getArguments()[0] );
            return result != null ? result : defaultValue;
          }
        }
    );
    when( env.getBaseConfig() ).thenReturn( conf );
    return env;
  }

  public static Element getElementFromSnippet( String xml ) throws DocumentException {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc.getRootElement();
  }

  public static void initBareEngine( ICdaEnvironment env ) {
    CdaTestEngine.init( new CdaTestEngine( env ) );
  }

  interface Comparison<T> {
    public boolean equal( T one, T two );
  }

  private static class CdaTestEngine extends CdaEngine {

    protected CdaTestEngine( ICdaEnvironment env ) throws InitializationException {
      super( env );
    }

    public static void init( ICdaEnvironment env ) {
      CdaEngine.initTestBare( new CdaTestEngine( env ) );
    }
    public static void init( CdaEngine eng ) {
      CdaEngine.initTestBare( eng );
    }
  }

  /**
   * Bare bones TableModel for quick testing
   */
  public static class SimpleTableModel extends AbstractTableModel {
    // just for warnings
    private static final long serialVersionUID = 1L;
    private String[] columnNames;
    private Class<?>[] columnClasses;

    Object[][] rows;

    public SimpleTableModel() {}

    public SimpleTableModel( Object[]... rows ) {
      this.rows = rows;
    }

    public void setRows( Object[]... rows ) {
      this.rows = rows;
    }

    public void setColumnNames( String... columnNames ) {
      this.columnNames = columnNames;
    }

    public void setColumnClasses( Class<?>... columnClasses ) {
      this.columnClasses = columnClasses;
    }

    @Override
    public int getRowCount() {
      return rows.length;
    }

    @Override
    public int getColumnCount() {
      return rows.length > 0 ? rows[0].length : 0;
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex ) {
      return rows[rowIndex][columnIndex];
    }

    @Override
    public String getColumnName( int column ) {
      if ( columnNames == null ) { return super.getColumnName( 0 ); };
      return columnNames[column];
    }
    @Override
    public Class<?> getColumnClass( int column ) {
      if ( columnClasses == null ) { return super.getColumnClass( 0 ); };
      return columnClasses[column];
    }

  }

}
