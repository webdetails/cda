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


package pt.webdetails.cda.dataaccess.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;

import java.util.ArrayList;

/**
 * Adapts data access to Kettle transformation step
 */
public interface DataAccessKettleAdapter {

  /**
   * @param name    Step name
   * @param columns Named columns form Table Input
   * @return Kettle step definition
   * @throws KettleAdapterException
   */
  public StepMeta getFilterStepMeta( String name, String[] columns )
    throws KettleAdapterException;

  /**
   * @param name Step name
   * @return Kettle step definition
   * @throws KettleAdapterException
   */
  public StepMeta getKettleStepMeta( String name ) throws KettleAdapterException;

  /**
   * @return return used database connections if any, otherwise <code>null</code>
   * @throws KettleAdapterException
   */
  public DatabaseMeta[] getDatabases() throws KettleAdapterException;

  /**
   * @return parameter names as they appear in query
   */
  public String[] getParameterNames() throws KettleAdapterException;

  /**
   * @return data access parameters
   */
  public DataRow getParameters() throws KettleAdapterException;

  /**
   * @return data access outputs indexes
   */
  public ArrayList<Integer> getDataAccessOutputs() throws KettleAdapterException;

  public boolean hasCalculatedColumns();

  public StepMeta getFormulaStepMeta( String name );

}
