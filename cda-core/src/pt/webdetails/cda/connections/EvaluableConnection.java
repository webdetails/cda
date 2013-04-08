/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections;

/**
 * A connection with fields that need to be evaluated in some form prior to execution.<br> 
 * Needs to be stored in pre-evaluated form in settings cache but evaluated before being used in a table cache key or query execution.  
 */
public interface EvaluableConnection {

  /**
   * @return Clone of this connection with all evaluable fields evaluated.
   */
  public Connection evaluate();
  
}
