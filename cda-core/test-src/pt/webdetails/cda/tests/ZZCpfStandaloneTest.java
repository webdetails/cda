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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;



import org.apache.commons.io.FileUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import pt.webdetails.cda.CdaCoreService;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.CoreBeanFactory;
import pt.webdetails.cda.DefaultCdaEnvironment;


public class ZZCpfStandaloneTest {
    
    
    
    private static DefaultCdaEnvironment env;
	@BeforeClass
	public static void setUp() throws Exception {
		CoreBeanFactory cbf = new CoreBeanFactory("cda.spring.xml");
		env= new DefaultCdaEnvironment(cbf);
		CdaEngine.init(env);
	}
	
        @Test
        @Ignore
	public void testRepository() {
		try {
			final String path = "testfolder";
			final String file = "sample-olap4j.cda";
			final String outputType = "json";
			final String dataAccessId = "2";
			
			CdaCoreService ccs = new CdaCoreService();           
                        int lose1=1;
                        String rel = env.getRepositoryAccess().toString();
                        int lose=1;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ccs.listQueries(bos, path ,null , file, outputType);
			String actual = new String(bos.toByteArray());
			bos.flush();
			Assert.assertEquals(
					"{\"resultset\":[[\"2\",\"Sample query on SteelWheelsSales\",\"olap4j\"]],"
					+ "\"metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"id\"},"
					+ "{\"colIndex\":1,\"colType\":\"String\",\"colName\":\"name\"},"
					+"{\"colIndex\":2,\"colType\":\"String\",\"colName\":\"type\"}]}"
					, actual);
			
			ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			ccs.listParameters(bos2, path, null, file, outputType, dataAccessId);
			String actual2 = new String(bos2.toByteArray());
			Assert.assertEquals(
					"{\"resultset\":[[\"status\",\"String\",\"In Process\",null,\"public\"]],\"metadata\":[{\"colIndex\":0,\"colType\":\"String\",\"colName\":\"name\"},{\"colIndex\":1,\"colType\":\"String\",\"colName\":\"type\"},{\"colIndex\":2,\"colType\":\"String\",\"colName\":\"defaultValue\"},{\"colIndex\":3,\"colType\":\"String\",\"colName\":\"pattern\"},{\"colIndex\":4,\"colType\":\"String\",\"colName\":\"access\"}]}"
					, actual2);
			
			
			ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
			ccs.getCdaFile(bos3,path + "/" + file,null);
			String actual3 = new String(bos3.toByteArray());
			String expected = getFileContent("pt/webdetails/cda/tests/sample-olap4j.cda");
			Assert.assertEquals(expected, actual3);
			
			
			ByteArrayOutputStream bos4 = new ByteArrayOutputStream();
			String content = expected;
			boolean written = ccs.writeCdaFile(bos4, path, null, "sample-olap4j-copy.cda", content);
			Assert.assertTrue(written);
			String actual4 = new String(bos4.toByteArray());
			Assert.assertEquals("File saved.", actual4);
			
			String copy = getFileContent("test-resources/standalone/repository/testfolder/sample-olap4j-copy.cda");
			Assert.assertEquals(expected, copy);

			
			boolean deleted = ccs.deleteCdaFile(path, null, file);
			Assert.assertEquals(true, deleted);
			
			String deletedFile = getFileContent("test-resources/standalone/repository/testfolder/sample-olap4j-copy.cda");
			Assert.assertNull(deletedFile);


			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
                    String s = e.toString();
			e.printStackTrace();
                        
			Assert.fail();
		}
		
		
	}
	
	private String getFileContent(String filename) {
		try {
			URL u = this.getClass().getClassLoader().getResource(filename);
			File f = new File(u.toURI());
			return FileUtils.readFileToString(f, "UTF-8");
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}

}
