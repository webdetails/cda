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

package pt.webdetails.cda.tests.utils;

import java.util.HashMap;

import javax.swing.table.TableModel;

import junit.framework.TestCase;
import pt.webdetails.cda.utils.InterPluginUtils;

/**
 *
 * @author pedrovale
 */
public class TestInterPluginUtils extends TestCase {


  public void testInterPluginUtils() {
    System.out.println("TEST");


    TableModel tm1 = InterPluginUtils.getInstance().getTableModelFromJSONArray("[{\"id\":1,\"type\":\"query\",\"name\":\"Test 1\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/sql-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/sql-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"},{\"id\":2,\"type\":\"query\",\"name\":\"Test 2\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"}]", null);

    HashMap params = new HashMap<String, Object>();
    params.put("columns", new String[]
            {
              "id", "XXX","group"
            });

    TableModel tm2 = InterPluginUtils.getInstance().getTableModelFromJSONArray("[{\"id\":1,\"type\":\"query\",\"name\":\"Test 1\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/sql-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/sql-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"},{\"id\":2,\"type\":\"query\",\"name\":\"Test 2\",\"group\":\"CDV Sample Tests\",\"createdBy\":\"Pedro\",\"createdAt\":1339430893246,\"validation\":\"/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] \\n/plugin-samples/cda/cdafiles/mondrian-jndi.cda[1] (status: Cancelled)\",\"validationName\":\"Test Existence\",\"validationType\":\"custom\",\"expected\":100,\"warnPercentage\":0.3,\"errorPercentage\":0.7,\"errorOnLow\":true,\"cron\":\"0 2 * * ? *\"}]", params);
    
  }
}
