package pt.webdetails.cda.tests;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jxl.read.biff.SetupRecord;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.dom4j.Document;

import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.xml.DomTraversalHelper;
import pt.webdetails.cda.xml.XmlUtils;

public class XmlRoundtrip extends XMLTestCase {
	
	private SettingsManager settingsManager;
	private DomTraversalHelper tHelper;

	public void setUp(){
		settingsManager = SettingsManager.getInstance();
		tHelper = new DomTraversalHelper();
	}

    public XmlRoundtrip(String name) {
        super(name);
    }
	
	public String readCdaFile(String file) throws Exception {
	    final File settingsFile = new File(file);
	    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
	    return cdaSettings.asXML();
	}
	

	
	public String generateXml(String file) throws Exception {
		
	    final File settingsFile = new File(file);
	    final CdaSettings cdaSettings = settingsManager.parseSettingsFile(settingsFile.getAbsolutePath());
		Document doc = tHelper.traverse(cdaSettings);

		//return XmlUtils.formatXml(tHelper.traverse(cdaSettings).asXML());
		return XmlUtils.prettyPrint(tHelper.traverse(cdaSettings).asXML());
		//return tHelper.traverse(cdaSettings).asXML();

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
    	equalityCheck("test/pt/webdetails/cda/tests/sample-discovery.cda");
    }
    
    public void testJoin() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-join.cda");
    }

    public void testIterable() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-iterable-sql.cda");
    }

    public void testKettle() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-kettle.cda");
    }

    public void testMdxStringArray() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-mdx-stringArray.cda");
    }
    
    public void testMondrianCompact() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-mondrian-compact.cda");
    }    

    public void testMetadata() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-metadata.cda");
    }  
    
    public void testMondrianJndi() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-mondrian-jndi.cda");
    }    
    
    public void testMondrian() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-mondrian.cda");
    }    
    
    public void testOlap4j() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-olap4j.cda");
    }     
    
    public void testOutput() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-output.cda");
    }        

    public void testReflection() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-reflection.cda");
    }        

    public void testScripting() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-scripting.cda");
    }        

    public void testSecurityParam() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-securityParam.cda");
    }    

    public void testSqlFormula() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-sql-formula.cda");
    }
    
    public void testSqlList() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-sql-list.cda");
    }
    
    public void testSql() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-sql.cda");
    }

    public void testUnion() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-union.cda");
    }
    
    public void testXpath() throws Exception{
    	equalityCheck("test/pt/webdetails/cda/tests/sample-xpath.cda");
    }
}
