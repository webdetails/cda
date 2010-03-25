package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Mar 25, 2010
 * Time: 5:06:02 PM
 */
public class DataAccessConnectionDescriptor
{

  private String name;

  private String dataAccessType;

  private ArrayList<PropertyDescriptor> dataAccessInfo;

  private ArrayList<PropertyDescriptor> connectionInfo;



  public DataAccessConnectionDescriptor()
  {
    dataAccessInfo = new ArrayList<PropertyDescriptor>();
    connectionInfo = new ArrayList<PropertyDescriptor>();
  }

  public DataAccessConnectionDescriptor(final String name)
  {
    this();
    setName(name);
  }

  public void addDataAccessProperty(PropertyDescriptor p)
  {
    dataAccessInfo.add(p);
  }

  public void addConnectionProperty(PropertyDescriptor p)
  {
    connectionInfo.add(p);
  }


  public ArrayList<PropertyDescriptor> getDescriptors(){

    ArrayList<PropertyDescriptor> collapsedInfo = new ArrayList<PropertyDescriptor>();
    collapsedInfo.addAll(connectionInfo);
    collapsedInfo.addAll(dataAccessInfo);

    return collapsedInfo;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }


}