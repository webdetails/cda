package pt.webdetails.cda.query;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 5:25:53 PM
 */
public class QueryOptions
{

  private boolean paginate = false;
  private int pageSize = 20;
  private int pageStart = 0;
  private ArrayList<Integer> sortBy = new ArrayList<Integer>();

  public QueryOptions()
  {
  }




}
