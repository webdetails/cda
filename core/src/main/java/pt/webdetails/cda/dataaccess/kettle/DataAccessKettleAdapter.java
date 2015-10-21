/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.dataaccess.kettle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;

import java.util.ArrayList;

/**
 * Adapts data access to Kettle transformation step
 *
 * @author Michael Spector
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
