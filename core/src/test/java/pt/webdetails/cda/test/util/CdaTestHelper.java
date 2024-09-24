/*!
 * Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.test.util;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.ComparisonFailure;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.reporting.libraries.base.config.Configuration;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.InitializationException;
import pt.webdetails.cpf.Util;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;

public class CdaTestHelper {

  /**
   * Ignores properties order in comparison, friendly output when failing.<br>
   *
   * @param message
   * @param expectedJson parsing of this field will be very lenient, so don't mix with the actual result!
   * @param
   */
  public static void assertJsonEquals( String message, String expectedJson, String actualJson ) throws Exception {
    ObjectMapper om = new ObjectMapper();
    JsonNode parsedResult = om.readTree( actualJson );
    // expected ONLY
    om.configure( JsonParser.Feature.ALLOW_SINGLE_QUOTES, true );
    om.configure( JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
    JsonNode parsedExpected = om.readTree( expectedJson );
    if ( !parsedExpected.equals( parsedResult ) ) {
      try {
        // attempt to build a friendly comparison
        // om.configure( SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true );
        om.enable( SerializationFeature.INDENT_OUTPUT );
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

  public static ICdaEnvironment getMockEnvironment() {
    ICdaEnvironment env = getMockEnvironment( Collections.<String, String>emptyMap() );
    return env;
  }

  public static ICdaEnvironment getMockEnvironment( final Map<String, String> configurationProperties ) {
    ICdaEnvironment env = Mockito.mock( ICdaEnvironment.class );
    Configuration conf = Mockito.mock( Configuration.class );
    Mockito.when( conf.getConfigProperty( any( String.class ), any( String.class ) ) ).thenAnswer(
      new Answer<String>() {
        @Override
        public String answer( InvocationOnMock invocation ) throws Throwable {
          String defaultValue = (String) invocation.getArguments()[ 1 ];
          String result = configurationProperties.get( invocation.getArguments()[ 0 ] );
          return result != null ? result : defaultValue;
        }
      }
    );
    Mockito.when( conf.findPropertyKeys( any( String.class ) ) ).thenAnswer(
      new Answer<Iterator<String>>() {
        @SuppressWarnings( "unchecked" )
        public Iterator<String> answer( final InvocationOnMock invocation ) throws Throwable {
          return configurationProperties.isEmpty()
            ? Collections.<String>emptyIterator()
            : new FilterIterator( configurationProperties.keySet().iterator(), new Predicate() {
            public boolean evaluate( Object object ) {
              return StringUtils.startsWith( (String) object, (String) invocation.getArguments()[ 0 ] );
            }
          } );
        }
      } );
    Mockito.when( env.getBaseConfig() ).thenReturn( conf );
    return env;
  }

  public static Element getElementFromSnippet( String xml ) throws DocumentException {
    Document doc = getDocumentFromString( xml );
    return doc.getRootElement();
  }

  public static Document getDocumentFromString( String xml ) throws DocumentException {
    SAXReader reader = new SAXReader( false );
    Document doc = reader.read( Util.toInputStream( xml ) );
    return doc;
  }

  public static CdaEngine initBareEngine( ICdaEnvironment env ) {
    CdaTestEngine testEngine = new CdaTestEngine( env );
    CdaTestEngine.init( testEngine );
    return testEngine;
  }

  interface Comparison<T> {
    public boolean equal( T one, T two );
  }

  private static class CdaTestEngine extends CdaEngine {

    protected CdaTestEngine( ICdaEnvironment env ) throws InitializationException {
      super( env );
    }

    public static CdaEngine init( ICdaEnvironment env ) {
      CdaEngine engine = new CdaTestEngine( env );
      CdaEngine.initTestBare( engine );
      return engine;
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

    public SimpleTableModel() {
    }

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
      return rows.length > 0 ? rows[ 0 ].length : 0;
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex ) {
      try {
        return rows[ rowIndex ][ columnIndex ];
      } catch ( ArrayIndexOutOfBoundsException e ) {
        return null;
      }
    }

    @Override
    public String getColumnName( int column ) {
      if ( columnNames == null ) {
        return super.getColumnName( 0 );
      }
      return columnNames[ column ];
    }

    @Override
    public Class<?> getColumnClass( int column ) {
      if ( columnClasses == null ) {
        return super.getColumnClass( 0 );
      }
      return columnClasses[ column ];
    }

  }

}
