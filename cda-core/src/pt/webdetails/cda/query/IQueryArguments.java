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

package pt.webdetails.cda.query;

import java.util.List;

import pt.webdetails.cda.dataaccess.Parameter;

/**
 * Arguments passed to a data access to execute query
 */
public interface IQueryArguments {

  /**
   * If {@link #getPageSize()} and {@link #getPageStart()} should be taken into account
   */
  public boolean isPaginate();

  public int getPageSize();

  public int getPageStart();

  /**
   * get list of sort by directives in the form &lt;column-index&gt;&lt;A|D&gt;
   */
  public List<String> getSortBy();

  /**
   * @return mutable parameters list
   */
  public List<Parameter> getParameters();

  /**
   * get a specific parameter
   *
   * @param name
   * @return null if not found
   */
  public Parameter getParameter( String name );

  /**
   * @return if shouldn't try to fetch from cache
   */
  public boolean isCacheBypass();

}
