package pt.webdetails.cda.utils;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 3:37:46 PM
 */
public class Util
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

    e.printStackTrace();
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


  public static final boolean isNullOrEmpty(final String str)
  {
    return (str == null || str.trim().length() == 0);
  }
}
