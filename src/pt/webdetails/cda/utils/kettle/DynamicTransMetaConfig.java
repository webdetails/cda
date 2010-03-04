package pt.webdetails.cda.utils.kettle;

import static org.pentaho.di.core.Const.isEmpty;
import static org.pentaho.di.core.Const.trim;

import java.io.File;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.TransMeta;

/**
 * Configuration parameters for constructing the TransMeta object that drives
 * the Kettle Transformation. You can construct a TransMeta in three ways:
 * <ul>
 * <li>Type.XML_FILE, <path to filename> -- Kettle will read the .ktr file and
 * configure the transformation
 * <li>Type.XML_STRING, <xml data> -- Kettle will parse the XML string and
 * configure the transformation
 * <li>Type.REPOSITORY, <repository path>, <Repository object> -- Kettle will
 * connect to the repository and configure the transformation at the specified
 * path
 * 
 * @author Daniel Einspanjer
 */
public class DynamicTransMetaConfig
{
  public enum Type {
    EMPTY, XML_FILE, XML_STRING, REPOSITORY
  }

  private final TransMeta transMeta;

  public DynamicTransMetaConfig(final Type type, final String name, final String configDataSource, final RepositoryConfig rc) throws KettleException
  {
    if (type == null) throw new IllegalArgumentException("Type is null");
    if (isEmpty(trim(name))) throw new IllegalArgumentException("Name is null");

    switch (type) {
      case EMPTY:
        transMeta = new TransMeta();
        transMeta.setRepository(connectToRepository(rc));
        break;
      case XML_FILE:
        transMeta = new TransMeta(configDataSource, connectToRepository(rc));
        break;
      case XML_STRING:
        transMeta = new TransMeta(XMLHandler.getSubNode(XMLHandler.loadXMLString(configDataSource), "transformation"), connectToRepository(rc));
        break;
      case REPOSITORY:
        if (rc == null) throw new IllegalArgumentException("Type.REPOSITORY must have RepositoryConfig object");
        final Repository rep = connectToRepository(rc);
        final File transPath = new File(configDataSource);
        if (isEmpty(transPath.getName())) throw new IllegalArgumentException("Type.REPOSITORY configDataSource must have path to transformation");
        RepositoryDirectory directory = rep.getDirectoryTree();
        if (!isEmpty(transPath.getParent())) {
          directory = rep.getDirectoryTree().findDirectory(transPath.getParent());
        }
        if (directory == null)
          throw new IllegalArgumentException(String.format("Directory %s not found in repository %s", transPath.getParent(), rc.repositoryName));
        transMeta = new TransMeta(rep, transPath.getName(), directory);
      default:
        throw new IllegalArgumentException(String.format("Unknown Type %s", type));
    }

    transMeta.setName(name);
  }

  private Repository connectToRepository(final RepositoryConfig rc) throws IllegalArgumentException
  {
    if (rc == null) return null;

    final LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_ERROR);
    final RepositoriesMeta repsinfo = new RepositoriesMeta(log);
    if (!repsinfo.readData()) throw new IllegalArgumentException("No repositories defined");

    final RepositoryMeta repinfo = repsinfo.findRepository(rc.repositoryName);
    if (repinfo == null) throw new IllegalArgumentException(String.format("Repository %s not found", rc.repositoryName));

    final Repository rep = new Repository(log, repinfo, null);
    try {
      if (!rep.connect("CDA DynamicTransformation")) throw new KettleException("Repository.connect() returned false");
    } catch (final KettleException e) {
      throw new IllegalArgumentException(String.format("Could not connect to repository %s", rc.repositoryName), e);
    }

    UserInfo userinfo;
    try {
      userinfo = new UserInfo(rep, rc.userName, rc.password);
      if (userinfo.getID() == 0) throw new KettleException("UserInfo returned ID 0");
    } catch (final KettleException e) {
      throw new IllegalArgumentException(String.format("Username %s not found in repository %s", rc.userName, rc.repositoryName));
    }

    return rep;
  }

  protected TransMeta getTransMeta(final VariableSpace variableSpace) throws KettleException
  {
    transMeta.initializeVariablesFrom(variableSpace);
    return transMeta;
  }

  public static class RepositoryConfig
  {
    public final String repositoryName;
    public final String userName;
    public final String password;

    private RepositoryConfig(final String repositoryName, final String userName, final String password)
    {
      this.repositoryName = repositoryName;
      this.userName = userName;
      this.password = password;
    }

    public static RepositoryConfig get(final String repositoryName, final String userName, final String password)
    {
      if (isEmpty(trim(repositoryName)) || isEmpty(trim(userName)) || password == null) throw new IllegalArgumentException("Invalid RepositoryConfig");
      return new RepositoryConfig(repositoryName, userName, password);
    }
  }
}
