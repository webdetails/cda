package pt.webdetails.robochef;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface RowProductionManager
{

  /**
   * Start producing rows with a default timeout for execution.
   */
  public void startRowProduction(Collection<Callable<Boolean>> inputCallables);
  
  /**
   * Start producing rows with a specific timeout for execution
   * @param timeout the number of time units to wait before interrupting execution
   * @param unit TimeUnit that timeout is represent in (i.e. TimeUnit.SECONDS)
   */
  public void startRowProduction(long timeout, TimeUnit unit, Collection<Callable<Boolean>> inputCallables);

}
