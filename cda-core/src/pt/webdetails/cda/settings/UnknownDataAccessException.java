/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.settings;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 3:17:13 PM
 */
public class UnknownDataAccessException extends Exception {

  private static final long serialVersionUID = 1L;

  public UnknownDataAccessException(final String s, final Exception cause) {
    super(s,cause);
  }
}
