/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.ctools.cda;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryProvider;

public class RepositoryProvider implements KettleRepositoryProvider {
  private final String repoName;
  private final String repoUsername;
  private final String repoPassword;

  private Repository repository;

  public RepositoryProvider( String repositoryName, String username, String password ) {
    this.repoName = repositoryName;
    this.repoUsername = username;
    this.repoPassword = password;
  }

  @Override
  public Repository getRepository() {
    if ( this.repository == null ) {
      try {
        // Connecting to the repository
        RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
        repositoriesMeta.readData();
        RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repoName );
        PluginRegistry registry = PluginRegistry.getInstance();

        this.repository = registry.loadClass(
            RepositoryPluginType.class,
            repositoryMeta,
            Repository.class
        );

        this.repository.init( repositoryMeta );
        this.repository.connect( repoUsername, repoPassword );
      } catch ( KettleException e ) {
        e.printStackTrace();
      }
    }

    return this.repository;
  }
}