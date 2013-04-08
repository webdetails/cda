/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.utils;

/**
 * Created by IntelliJ IDEA.
 * User: andre
 * Date: Jul 14, 2011
 * Time: 11:13:10 PM
 */
public class InvalidOutputIndexException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidOutputIndexException(final String s, final Exception cause) {
    super(s,cause);
  }
}