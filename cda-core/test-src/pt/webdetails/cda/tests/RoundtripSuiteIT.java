/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda.tests;

import java.io.File;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.w3c.dom.Node;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.tests.utils.CdaPluginTestEnvironment;
import pt.webdetails.cda.tests.utils.CdaTestEnvironment;
import pt.webdetails.cda.tests.utils.CdaTestingContentAccessFactory;
import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtDenormalizedMDXDataFactory;
import pt.webdetails.cda.xml.DomTraversalHelper;
import pt.webdetails.cda.xml.XmlUtils;
import pt.webdetails.cpf.PluginEnvironment;

public class RoundtripSuiteIT extends XMLTestCase {
  private DomTraversalHelper tHelper;
  private CdaTestEnvironment testEnvironment;
  private static final Class[] customDataFactories = {
    CompactBandedMDXDataFactory.class, ExtBandedMDXDataFactory.class, ExtDenormalizedMDXDataFactory.class };
  private static final String USER_DIR = System.getProperty( "user.dir" );

  private static final String OLAP4J_STRING = "olap4j";
  private static final String OLAP4J_DEFAULT_STRING = "olap4j.defaultolap4j";

  public void setUp() {
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    // always need to make sure there is a plugin environment initialized
    PluginEnvironment.init( new CdaPluginTestEnvironment( factory ) );
    // cda-specific environment
    // cda init
    CdaEngine.init( new CdaTestEnvironment( factory ) );
    tHelper = new DomTraversalHelper();

    // always need to make sure there is a plugin environment initialized
    PluginEnvironment.init( new CdaPluginTestEnvironment( factory ) );

    // cda-specific environment
    testEnvironment = new CdaTestEnvironment( factory );
    // cda init
    CdaEngine.init( testEnvironment );
    // making sure the custom data factories are registered
    registerCustomDataFactories();
    // due to http://jira.pentaho.com/browse/PDI-2975
    System.setProperty( "org.osjava.sj.root", getSimpleJndiPath() );
  }

  protected SettingsManager getSettingsManager() {
    return getEngine().getSettingsManager();
  }

  protected CdaEngine getEngine() {
    return CdaEngine.getInstance();
  }

  protected CdaSettings parseSettingsFile( String cdaSettingsId ) throws Exception {
    return getSettingsManager().parseSettingsFile( cdaSettingsId );
  }

  protected ICdaEnvironment getEnvironment() {
    return CdaEngine.getEnvironment();
  }

  protected String getSimpleJndiPath() {

    if ( USER_DIR.endsWith( "bin/test/classes" ) ) {
      // command-line run
      return USER_DIR + File.separator + "simplejndi";
    } else {
      // IDE run
      return USER_DIR + File.separator + "test-resources" + File.separator + "simplejndi";
    }

  }

  public String readCdaFile( String file ) throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( file );
    return cdaSettings.asXML();
  }

  public String generateXml( String file ) throws Exception {
    final CdaSettings cdaSettings = parseSettingsFile( file );
    return XmlUtils.prettyPrint( tHelper.traverse( cdaSettings ).asXML() );
  }

  private void equalityCheck( String file ) throws Exception {

    XMLUnit.setIgnoreComments( Boolean.TRUE );
    XMLUnit.setIgnoreAttributeOrder( Boolean.TRUE );
    XMLUnit.setIgnoreWhitespace( Boolean.TRUE );
    XMLUnit.setIgnoreDiffBetweenTextAndCDATA( Boolean.TRUE );

    String controlXml = readCdaFile( file );
    String testXml = generateXml( file );

    System.out.println( testXml );

    DetailedDiff myDiff = new DetailedDiff( compareXML( controlXml, testXml ) );
    myDiff.overrideElementQualifier( new ElementNameAndAttributeQualifier() );
    myDiff.overrideDifferenceListener( new DifferenceListener() {
      @Override public int differenceFound( Difference difference ) {
        if ( difference.getId() == DifferenceConstants.ATTR_VALUE_ID ) {
          String controlNodeString = difference.getControlNodeDetail().getValue(),
            testNodeString = difference.getTestNodeDetail().getValue();

          if ( controlNodeString.equals( OLAP4J_STRING )
            && ( testNodeString.equals( OLAP4J_STRING ) || testNodeString.equals( OLAP4J_DEFAULT_STRING ) ) ) {
            return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
          }
        } else if ( difference.getId() == DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID ) {
          return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        } else if ( difference.getId() == DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID
          || difference.getId() == DifferenceConstants.ATTR_NAME_NOT_FOUND_ID ) {
          Node cacheTestNode = difference.getTestNodeDetail().getNode().getAttributes().getNamedItem( "cache" ),
            controlTestNode = difference.getControlNodeDetail().getNode().getAttributes().getNamedItem( "cache" );

          if ( cacheTestNode.getNodeValue().equals( "true" )
            && cacheTestNode.getNodeValue().equals( controlTestNode.getNodeValue() ) ) {
            Node cacheDurationTestNode = difference.getTestNodeDetail().getNode().getAttributes().getNamedItem(
              "cacheDuration" ),
              cacheDurationControlNode = difference.getControlNodeDetail().getNode().getAttributes().getNamedItem(
                "cacheDuration" );

            if ( cacheDurationControlNode.getNodeValue().equals( "3600" ) && ( cacheDurationTestNode == null ||
              cacheDurationTestNode.getNodeValue().equals( "3600" ) ) ) {
              return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
          }
        }

        return 0;
      }

      @Override public void skippedComparison( Node node, Node node1 ) {

      }
    } );
    myDiff.similar();
    XMLAssert.assertXMLEqual( "pieces of XML are similar ", myDiff, true );

  }

  public void testDiscovery() throws Exception {
    equalityCheck( "sample-discovery.cda" );
  }

  public void testJoin() throws Exception {
    equalityCheck( "sample-join.cda" );
  }

  public void testIterable() throws Exception {
    equalityCheck( "sample-iterable-sql.cda" );
  }

  public void testKettle() throws Exception {
    equalityCheck( "sample-kettle.cda" );
  }

  public void testMdxStringArray() throws Exception {
    equalityCheck( "sample-mdx-stringArray.cda" );
  }

  public void testMondrianCompact() throws Exception {
    equalityCheck( "sample-mondrian-compact.cda" );
  }

  public void testMetadata() throws Exception {
    equalityCheck( "sample-metadata.cda" );
  }

  public void testMondrianJndi() throws Exception {
    equalityCheck( "sample-mondrian-jndi.cda" );
  }

  public void testMondrian() throws Exception {
    equalityCheck( "sample-mondrian.cda" );
  }

  public void testOlap4j() throws Exception {
    equalityCheck( "sample-olap4j.cda" );
  }

  public void testOutput() throws Exception {
    equalityCheck( "sample-output.cda" );
  }

  public void testReflection() throws Exception {
    equalityCheck( "sample-reflection.cda" );
  }

  public void testScripting() throws Exception {
    equalityCheck( "sample-scripting.cda" );
  }

  public void testSecurityParam() throws Exception {
    equalityCheck( "sample-securityParam.cda" );
  }

  public void testSqlFormula() throws Exception {
    equalityCheck( "sample-sql-formula.cda" );
  }

  public void testSqlList() throws Exception {
    equalityCheck( "sample-sql-list.cda" );
  }

  public void testSql() throws Exception {
    equalityCheck( "sample-sql.cda" );
  }

  public void testUnion() throws Exception {
    equalityCheck( "sample-union.cda" );
  }

  public void testXpath() throws Exception {
    equalityCheck( "sample-xpath.cda" );
  }

  protected static void registerCustomDataFactories() {
    for ( Class clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
        clazz.getName(), "", "", true, false, true, false, false, false, false, false,
        new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }
}
