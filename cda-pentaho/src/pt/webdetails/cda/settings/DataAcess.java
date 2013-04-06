/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.settings;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.pentaho.platform.engine.core.system.PentahoSystem;
public class DataAcess implements IDataAcess {
    
    public String[] getDataAcesses() throws Exception {
        
        final File file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/cda/resources/components.properties"));
        final Properties resources = new Properties();
        
        FileInputStream fin = null;
        try{
             fin = new FileInputStream(file);
            resources.load(fin);
         }
         finally {
         IOUtils.closeQuietly(fin);
            }
    return StringUtils.split(StringUtils.defaultString(resources.getProperty("dataAccesses")), ",");
    }
    
}
