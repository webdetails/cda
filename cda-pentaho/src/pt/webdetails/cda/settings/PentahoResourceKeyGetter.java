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

package pt.webdetails.cda.settings;

import java.io.File;
import java.util.HashMap;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.platform.plugin.RepositoryResourceLoader;

/**
 *
 * @author pedrovale
 */
public class PentahoResourceKeyGetter implements IResourceKeyGetter {

  public static final String SOLUTION_SCHEMA_NAME = RepositoryResourceLoader.SOLUTION_SCHEMA_NAME; //$NON-NLS-1$
  public static final String SCHEMA_SEPARATOR = RepositoryResourceLoader.SCHEMA_SEPARATOR; //$NON-NLS-1$


  @Override
  public ResourceKey getResourceKey(String id, ResourceManager resourceManager)  throws ResourceKeyCreationException {
        final HashMap<String, Object> helperObjects = new HashMap<String, Object>();
        return resourceManager.createKey(SOLUTION_SCHEMA_NAME + SCHEMA_SEPARATOR + id, helperObjects);
  }
  
}
