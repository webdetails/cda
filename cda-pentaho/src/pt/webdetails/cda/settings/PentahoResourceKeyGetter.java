/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
