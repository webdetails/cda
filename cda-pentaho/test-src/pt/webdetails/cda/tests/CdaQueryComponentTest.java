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
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cda.CdaQueryComponent;
/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 15, 2010
 * Time: 7:53:13 PM
 */
public class CdaQueryComponentTest extends CdaTestCase
{

  private static final Log logger = LogFactory.getLog(CdaQueryComponentTest.class);

  public CdaQueryComponentTest()
  {
    super();
  }

  public CdaQueryComponentTest(final String name)
  {
    super(name);
  }

  public void testCdaQueryComponent() throws Exception {
    CdaQueryComponent component = new CdaQueryComponent();
    component.setFile("sample-sql.cda");
    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put("dataAccessId", "1");
    inputs.put("paramorderDate", "2003-04-01");
    component.setInputs(inputs);
    
    component.validate();
    component.execute();
    Assert.assertNotNull(component.getResultSet());
    
    Assert.assertEquals(4, component.getResultSet().getColumnCount());
    Assert.assertEquals(3, component.getResultSet().getRowCount());
    
  }
}
