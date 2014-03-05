/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.utils;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 3:37:46 PM
 */
public class Util extends pt.webdetails.cpf.Util
{

  public static String getExceptionDescription(final Exception e)
  {

    final StringBuilder out = new StringBuilder();
    out.append("[ ").append(e.getClass().getName()).append(" ] - ");
    out.append(e.getMessage());

    if (e.getCause() != null)
    {
      out.append(" .( Cause [ ").append(e.getCause().getClass().getName()).append(" ] ");
      out.append(e.getCause().getMessage());

      if (e.getCause().getCause() != null)
      {
        out.append(" .( Parent [ ").append(e.getCause().getCause().getClass().getName()).append(" ] ");
        out.append(e.getCause().getCause().getMessage());
      }
    }

    return out.toString();

  }


  /**
   * Extracts a string between after the first occurrence of begin, and before the last occurence of end
   * @param source From where to extract
   * @param begin
   * @param end
   * @return
   */
  public static String getContentsBetween(final String source, final String begin, final String end)
  {
    if (source == null)
    {
      return null;
    }

    int startIdx = source.indexOf(begin) + begin.length();
    int endIdx = source.lastIndexOf(end);
    if (startIdx < 0 || endIdx < 0)
    {
      return null;
    }

    return source.substring(startIdx, endIdx);
  }
}
