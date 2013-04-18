/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections.mondrian;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 9, 2010
 * Time: 4:10:52 PM
 */
public interface MondrianConnectionInfo
{
  
  public String getCatalog();
  public String getUser();
  public String getPass();

  public String getMondrianRole();

  public String getRoleField();
  public String getUserField();
  public String getPasswordField();

}
