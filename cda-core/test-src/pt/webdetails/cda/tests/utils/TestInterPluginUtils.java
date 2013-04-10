/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
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
