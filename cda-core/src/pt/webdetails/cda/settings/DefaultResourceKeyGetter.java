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

import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.repository.IRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;

/**
 *
 * @author pedrovale
 */
public class DefaultResourceKeyGetter implements IResourceKeyGetter {


	@Override
	public ResourceKey getResourceKey(String id, ResourceManager resourceManager)  throws ResourceKeyCreationException {
		// TODO: we need a resourceloader that can deal with repository relative paths
		IRepositoryAccess repo = CdaEngine.getEnvironment().getRepositoryAccess();
		if (repo != null) {
			IRepositoryFile f = repo.getRepositoryFile(id, FileAccess.READ);
			return resourceManager.createKey(f.getData());
		}
		return resourceManager.createKey(id);
	}

}
