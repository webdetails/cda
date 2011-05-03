/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.utils;

import java.util.Comparator;
import java.util.List;
import javax.swing.table.TableModel;

/**
 *
 * @author pdpi
 */
public class NaturalOrderComparator implements Comparator
{

  private TableModel baseData;
  private List<String> sortBy;


  public NaturalOrderComparator(TableModel baseData, List<String> sortBy)
  {
    this.baseData = baseData;
    this.sortBy = sortBy;
  }


  public int compare(Object t, Object t1)
  {
    int i0 = (Integer) t;
    int i1 = (Integer) t1;

    for (String entry : sortBy)
    {

      char direction = entry.charAt(entry.length() - 1);
      boolean ascending = direction == 'A' || direction == 'a';
      int i = Integer.parseInt(entry.substring(0, entry.length() - 1), 10);
      int bigger = 0;

      String v0 = baseData.getValueAt(i0, i) != null ? baseData.getValueAt(i0, i).toString() : "";
      String v1 = baseData.getValueAt(i1, i) != null ? baseData.getValueAt(i1, i).toString() : "";

      bigger = compareStrings(v0, v1);
      if (bigger != 0)
      {
        return ascending ? bigger : -bigger;
      }
    }
    return 0;
  }


  public int compareStrings(String a, String b)
  {

    boolean pna = false, pnb = false;
    int ia = 0, ib = 0;
    int nza = 0, nzb = 0;
    char ca, cb;
    int result;

    while (true)
    {
      // only count the number of zeroes leading the last number compared
      nza = nzb = 0;

      ca = charAt(a, ia);
      cb = charAt(b, ib);

      // skip over leading spaces or zeros
      while (Character.isSpaceChar(ca) || ca == '0')
      {
        if (ca == '0')
        {
          nza++;
        }
        else
        {
          // only count consecutive zeroes
          nza = 0;
          pna = false;
        }

        ca = charAt(a, ++ia);
      }

      while (Character.isSpaceChar(cb) || cb == '0')
      {
        if (cb == '0')
        {
          nzb++;
        }
        else
        {
          // only count consecutive zeroes
          nzb = 0;
          pnb = false;
        }

        cb = charAt(b, ++ib);
      }

      // process run of digits
      if (Character.isDigit(ca) && Character.isDigit(cb))
      {
        if (pna != pnb) {
          return pna ? -1 : 1;
        }
        else if ((result = compareRight(a.substring(ia), b.substring(ib))) != 0)
        {
          return pna ? - result : result;
        }
      }

      if (ca == 0 && cb == 0)
      {
        // The strings compare the same.  Perhaps the caller
        // will want to call strcmp to break the tie.
        return nza - nzb;
      }
      pna = ca == '-';
      pnb = cb == '-';
      if (ca < cb)
      {
        return -1;
      }
      else if (ca > cb)
      {
        return +1;
      }

      ++ia;
      ++ib;
    }
  }


  int compareRight(String a, String b)
  {
    int bias = 0;
    int ia = 0;
    int ib = 0;
    boolean decimal = false;

    // The longest run of digits wins.  That aside, the greatest
    // value wins, but we can't know that it will until we've scanned
    // both numbers to know that they have the same magnitude, so we
    // remember it in BIAS.
    for (;; ia++, ib++)
    {
      char ca = charAt(a, ia);
      char cb = charAt(b, ib);

      // Skip commas -- we'e expecting those as thousand separators.
      // We won' bother validating those are at the thousand marks, though.
      while (ca == ',')
      {
        ca = charAt(a, ++ia);
      }
      while (cb == ',')
      {
        cb = charAt(b, ++ib);
      }
      // Decimal mark. Length no longer matters!
      if (ca == '.' && cb == '.')
      {
        if (decimal)
        {
          // we already found a decimal!
          return 0;
        }
        else
        {
          decimal = true;
        }
        continue;
      }
      if (!Character.isDigit(ca) && !Character.isDigit(cb))
      {
        return bias;
      }
      else if (!Character.isDigit(ca))
      {
        return decimal ? bias : -1;
      }
      else if (!Character.isDigit(cb))
      {
        return decimal ? bias : 1;
      }
      else if (ca < cb)
      {
        if (bias == 0)
        {
          bias = -1;
        }
      }
      else if (ca > cb)
      {
        if (bias == 0)
        {
          bias = +1;
        }
      }
      else if (ca == 0 && cb == 0)
      {
        return bias;
      }
    }
  }


  static char charAt(String s, int i)
  {
    if (i >= s.length())
    {
      return 0;
    }
    else
    {
      return s.charAt(i);
    }
  }
}
