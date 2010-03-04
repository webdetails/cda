package pt.webdetails.cda.utils.kettle.kettle;

import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.TransMeta;

public abstract class KettleUtils
{
  public static TransMeta initTransMeta(final String name)
  {
    EnvUtil.environmentInit();
    final TransMeta transMeta = new TransMeta();
    transMeta.setName(name);
    return transMeta;
  }
}
