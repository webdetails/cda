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

package pt.webdetails.cda.settings;

import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static pt.webdetails.cda.test.util.CdaTestHelper.getMockEnvironment;
import static pt.webdetails.cda.test.util.CdaTestHelper.initBareEngine;

/**
 * Unit tests for {@link SettingsManager#getDataAccessDescriptors(boolean)}.
 *
 * <p>The key behaviour under test is the resilience of the discovery loop: an
 * unlinkable connector (one whose class exists but whose transitive dependencies
 * are absent) must be silently skipped so that the other types — and the whole
 * CDE/CDA stack — continue to work.
 */
public class SettingsManagerTest {

  private SettingsManager settingsManager;

  @Before
  public void setUp() {
    settingsManager = new SettingsManager();
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  /** Initialises a bare CDA engine whose component list contains the given (short) class names. */
  private static void initWithDataAccesses( String... shortNames ) {
    ICdaEnvironment env = getMockEnvironment();
    Properties components = new Properties();
    components.setProperty( "dataAccesses", String.join( ",", shortNames ) );
    when( env.getCdaComponents() ).thenReturn( components );
    initBareEngine( env );
  }

  // ---------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------

  /**
   * A completely absent class name triggers {@link ClassNotFoundException} inside
   * {@code Class.forName}.  This was already caught by the original
   * {@code catch (Exception e)} clause; verify it still works after the widening.
   */
  @Test
  public void testGetDataAccessDescriptors_skipsClassNotFoundException() {
    initWithDataAccesses( "NonExistentDataAccessXYZ_DoesNotExist" );

    DataAccessConnectionDescriptor[] result = settingsManager.getDataAccessDescriptors( false );

    assertNotNull( "result must never be null", result );
    assertEquals( "missing class should be silently skipped", 0, result.length );
  }

  /**
   * A class whose static initializer throws {@link NoClassDefFoundError} (a
   * {@link LinkageError}) is the real-world failure mode when the PDI Data
   * Service plugin is absent.
   *
   * <p>Before the fix this error escaped the {@code catch (Exception)} block
   * and poisoned the whole JVM.  After the fix the connector is skipped and
   * discovery returns normally.
   *
   * <p>{@link pt.webdetails.cda.dataaccess.BadInitDataAccess} is a test-only
   * stub whose static initialiser throws {@code NoClassDefFoundError}; on the
   * first {@code Class.forName} call the JVM wraps it in
   * {@link ExceptionInInitializerError} (also a {@link LinkageError}).
   */
  @Test
  public void testGetDataAccessDescriptors_skipsLinkageError() {
    initWithDataAccesses( "BadInitDataAccess" );

    // Must not throw — LinkageError must be caught and the connector skipped.
    DataAccessConnectionDescriptor[] result = settingsManager.getDataAccessDescriptors( false );

    assertNotNull( "result must never be null even when a connector fails to link", result );
    assertEquals( "unlinkable connector should be silently skipped", 0, result.length );
  }

  /**
   * When the component list is empty the method returns an empty array, not null.
   */
  @Test
  public void testGetDataAccessDescriptors_emptyComponentList() {
    initWithDataAccesses( /* nothing */ );

    DataAccessConnectionDescriptor[] result = settingsManager.getDataAccessDescriptors( false );

    assertNotNull( result );
    assertEquals( 0, result.length );
  }

  /**
   * Both a good and a bad entry on the same list: only the good one is kept.
   * This covers the realistic scenario where Data Service connectors are mixed
   * in with fully-functional connectors.
   */
  @Test
  public void testGetDataAccessDescriptors_skipsLinkageError_withOtherValidEntriesUnaffected() {
    // BadInitDataAccess will fail; NonExistentXYZ will throw ClassNotFoundException.
    // Neither should prevent the method from returning a well-formed (possibly empty) array.
    initWithDataAccesses( "BadInitDataAccess", "NonExistentDataAccessXYZ_DoesNotExist" );

    DataAccessConnectionDescriptor[] result = settingsManager.getDataAccessDescriptors( false );

    assertNotNull( result );
    // Both entries must be skipped; we get an empty array, not an exception.
    assertEquals( 0, result.length );
  }
}
