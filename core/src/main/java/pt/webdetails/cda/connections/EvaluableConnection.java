/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cda.connections;

/**
 * A connection with fields that need to be evaluated in some form prior to execution.<br> Needs to be stored in
 * pre-evaluated form in settings cache but evaluated before being used in a table cache key or query execution.
 */
public interface EvaluableConnection {

  /**
   * @return Clone of this connection with all evaluable fields evaluated.
   */
  public Connection evaluate();

}
