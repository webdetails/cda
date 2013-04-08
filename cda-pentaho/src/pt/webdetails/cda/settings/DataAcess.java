/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cda.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cda.settings.IDataAccess;

import org.pentaho.platform.engine.core.system.PentahoSystem;
public class DataAcess implements IDataAccess {

    @Override
    public String[] getDataAcesses() {
         
        final File file = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/cda/resources/components.properties"));
        final Properties resources = new Properties();
        
        FileInputStream fin = null;
        try{
             fin = new FileInputStream(file);
            resources.load(fin);
         }
        catch(IOException e){//XXX  IOException
            
        }
         finally {
         IOUtils.closeQuietly(fin);
            }
    return StringUtils.split(StringUtils.defaultString(resources.getProperty("dataAccesses")), ",");
    }
}
