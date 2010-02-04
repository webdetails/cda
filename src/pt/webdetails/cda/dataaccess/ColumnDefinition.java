package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 4:49:25 PM
 */
public class ColumnDefinition
{

  public enum TYPE
  {
    COLUMN, CALCULATED_COLUMN
  }


  private TYPE type;
  private Integer index;
  private String name;
  private String formula;

  public ColumnDefinition()
  {
  }

  public ColumnDefinition(final Element p)
  {

    this();

    setName(p.selectSingleNode("Node").getText());

    if (p.getName().equals("CalculatedColumn"))
    {
      setType(TYPE.CALCULATED_COLUMN);
      setFormula(p.selectSingleNode("Formula").getText());
    }
    else
    {
      setType(TYPE.COLUMN);
      setIndex(Integer.parseInt(p.attributeValue("idx")));
    }

  }

  public TYPE getType()
  {
    return type;
  }

  public void setType(final TYPE type)
  {
    this.type = type;
  }

  public Integer getIndex()
  {
    return index;
  }

  public void setIndex(final Integer index)
  {
    this.index = index;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getFormula()
  {
    return formula;
  }

  public void setFormula(final String formula)
  {
    this.formula = formula;
  }
}
