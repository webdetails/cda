/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.settings;

import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;

/**
 *
 * @author pedrovale
 */
public class DefaultResourceKeyGetter implements IResourceKeyGetter {


	@Override
	public ResourceKey getResourceKey(String id, ResourceManager resourceManager)  throws ResourceKeyCreationException {
		//IRepositoryAccess repo = CdaEngine.getEnvironment().getRepositoryAccess();
		//IRepositoryFile f = repo.getRepositoryFile(id, FileAccess.READ);
		//return resourceManager.createKey(f.getFullPath());
		return resourceManager.createKey(id);
	}

}
