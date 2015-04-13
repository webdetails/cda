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

package pt.webdetails.robochef;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface RowProductionManager {

  /**
   * Start producing rows with a default timeout for execution.
   */
  public void startRowProduction( Collection<Callable<Boolean>> inputCallables );

  /**
   * Start producing rows with a specific timeout for execution
   *
   * @param timeout the number of time units to wait before interrupting execution
   * @param unit    TimeUnit that timeout is represent in (i.e. TimeUnit.SECONDS)
   */
  public void startRowProduction( long timeout, TimeUnit unit, Collection<Callable<Boolean>> inputCallables );

}
