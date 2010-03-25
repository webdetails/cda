package pt.webdetails.cda.dataaccess;

/**
 * Created by Pedro Alves
 * User: pedro
 * Date: Mar 25, 2010
 * Time: 5:13:33 PM
 */
public class PropertyDescriptor
{

  public static enum TYPE
  {
    STRING, NUMERIC
  }

  public static enum SOURCE
  {
    CONNECTION, DATAACCESS
  }

  private String name;
  private TYPE type;
  private SOURCE source;
  private String value;

  private PropertyDescriptor(final String name)
  {
    this.name = name;
    setType(TYPE.STRING);
  }

  private PropertyDescriptor(final String name, final TYPE type)
  {
    this.name = name;
    this.type = type;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public TYPE getType()
  {
    return type;
  }

  public void setType(final TYPE type)
  {
    this.type = type;
  }

  public SOURCE getSource()
  {
    return source;
  }

  public void setSource(final SOURCE source)
  {
    this.source = source;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(final String value)
  {
    this.value = value;
  }
}
