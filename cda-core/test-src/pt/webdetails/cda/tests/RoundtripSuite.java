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
import java.net.URL;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.xml.DomTraversalHelper;
import pt.webdetails.cda.xml.XmlUtils;

public class RoundtripSuite extends XMLTestCase {
	
	private SettingsManager settingsManager;
	private DomTraversalHelper tHelper;

	public void setUp(){
		settingsManager = SettingsManager.getInstance();
		tHelper = new DomTraversalHelper();
	}

	public String readCdaFile(String file) throws Exception {
		URL settingsURL = this.getClass().getResource(file);
	    File settingsFile = new File(settingsURL.toURI());
	    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
	    return cdaSettings.asXML();
	}
	

	
	public String generateXml(String file) throws Exception {
		
	    final File settingsFile = new File(file);
	    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
		return XmlUtils.prettyPrint(tHelper.traverse(cdaSettings).asXML());
	}
	
    private void equalityCheck(String file) throws Exception {

    	XMLUnit.setIgnoreComments(Boolean.TRUE);
    	XMLUnit.setIgnoreAttributeOrder(Boolean.TRUE);
    	XMLUnit.setIgnoreWhitespace(Boolean.TRUE);
  
		String controlXml = readCdaFile(file);
        String testXml = generateXml(file);
        
        System.out.println(testXml);

        DetailedDiff myDiff = new DetailedDiff(compareXML(controlXml, testXml));
        myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());

        
    }

    public void testDiscovery() throws Exception{
    	equalityCheck("sample-discovery.cda");
    }
    
    public void testJoin() throws Exception{
    	equalityCheck("sample-join.cda");
    }

    public void testIterable() throws Exception{
    	equalityCheck("sample-iterable-sql.cda");
    }

    public void testKettle() throws Exception{
    	equalityCheck("sample-kettle.cda");
    }

    public void testMdxStringArray() throws Exception{
    	equalityCheck("sample-mdx-stringArray.cda");
    }
    
    public void testMondrianCompact() throws Exception{
    	equalityCheck("sample-mondrian-compact.cda");
    }    

    public void testMetadata() throws Exception{
    	equalityCheck("sample-metadata.cda");
    }  
    
    public void testMondrianJndi() throws Exception{
    	equalityCheck("sample-mondrian-jndi.cda");
    }    
    
    public void testMondrian() throws Exception{
    	equalityCheck("sample-mondrian.cda");
    }    
    
    public void testOlap4j() throws Exception{
    	equalityCheck("sample-olap4j.cda");
    }     
    
    public void testOutput() throws Exception{
    	equalityCheck("sample-output.cda");
    }        

    public void testReflection() throws Exception{
    	equalityCheck("sample-reflection.cda");
    }        

    public void testScripting() throws Exception{
    	equalityCheck("sample-scripting.cda");
    }        

    public void testSecurityParam() throws Exception{
    	equalityCheck("sample-securityParam.cda");
    }    

    public void testSqlFormula() throws Exception{
    	equalityCheck("sample-sql-formula.cda");
    }
    
    public void testSqlList() throws Exception{
    	equalityCheck("sample-sql-list.cda");
    }
    
    public void testSql() throws Exception{
    	equalityCheck("sample-sql.cda");
    }

    public void testUnion() throws Exception{
    	equalityCheck("sample-union.cda");
    }
    
    public void testXpath() throws Exception{
    	equalityCheck("sample-xpath.cda");
    }
}
