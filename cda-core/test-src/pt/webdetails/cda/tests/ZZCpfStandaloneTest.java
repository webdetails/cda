package pt.webdetails.cda.tests;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import pt.webdetails.cda.CdaCoreService;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.CoreBeanFactory;
import pt.webdetails.cda.DefaultCdaEnvironment;

public class ZZCpfStandaloneTest extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CoreBeanFactory cbf = new CoreBeanFactory("cda.standalone.spring.xml");
		DefaultCdaEnvironment env = new DefaultCdaEnvironment(cbf);
		CdaEngine.init(env);
	}
	
	public void testRepository() {
		try {
			CdaCoreService ccs = new CdaCoreService();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ccs.listQueries(bos, "testfolder",null , "sample-olap4j.cda", "json");
			System.out.println(new String(bos.toByteArray()));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
