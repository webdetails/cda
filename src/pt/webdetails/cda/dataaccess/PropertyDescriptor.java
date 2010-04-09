package pt.webdetails.cda.dataaccess;

/**
 * Created by Pedro Alves
 * User: pedro
 * Date: Mar 25, 2010
 * Time: 5:13:33 PM
 */
public class PropertyDescriptor
{

  public static enum Type
  {
    STRING, ARRAY, BOOLEAN, NUMERIC
  }

  public static enum Source
  {
    CONNECTION, DATAACCESS
  }

  private String name;
  private Type type;
  private Source source;
  private String value;

  public  PropertyDescriptor(final String name)
  {
    this.name = name;
    setType(Type.STRING);
  }

  public PropertyDescriptor(final String name, final Type type)
  {
    this.name = name;
    this.type = type;
  }

    public PropertyDescriptor(final String name, final Type type, final Source source)
  {
    this.name = name;
    this.type = type;
    this.source = source;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public Type getType()
  {
    return type;
  }

  public void setType(final Type type)
  {
    this.type = type;
  }

  public Source getSource()
  {
    return source;
  }

  public void setSource(final Source source)
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
