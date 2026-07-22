/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package pt.webdetails.cda.dataaccess;

/**
 * Test-only stub used by {@code SettingsManagerTest}.
 *
 * <p>Its static initializer throws {@link NoClassDefFoundError} to simulate the
 * situation where a connector class (e.g. {@code DataservicesDataAccess}) exists
 * on the classpath but cannot be linked because a transitive dependency
 * (e.g. the PDI Data Service API jar) is absent.
 *
 * <p>On the first {@code Class.forName} call the JVM throws
 * {@link ExceptionInInitializerError}; on any subsequent call it throws
 * {@link NoClassDefFoundError}.  Both are {@link LinkageError}s and are now
 * caught by the discovery loop in {@code SettingsManager.getDataAccessDescriptors}.
 */
public class BadInitDataAccess {
  static {
    if ( Boolean.TRUE ) { // keep javac from complaining about unreachable code after the throw
      throw new NoClassDefFoundError( "simulated/missing/DependencyClass" );
    }
  }
}
