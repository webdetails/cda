package pt.webdetails.cda.dataaccess.kettle;

import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.TransMeta;


public abstract class KettleUtils
{
    public static TransMeta initTransMeta(String name)
    {
        EnvUtil.environmentInit();
        TransMeta transMeta = new TransMeta();
        transMeta.setName(name);
        return transMeta;
    }
}
