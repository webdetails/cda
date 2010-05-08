package pt.webdetails.cda.connections.kettle;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;

/**
 * Todo: Document me!
 * <p/>
 * Date: 08.05.2010
 * Time: 14:05:29
 *
 * @author Thomas Morgner.
 */
public class TransFromFileConnectionInfo
{
  private String transformationFile;
  private String[] definedArgumentNames;
  private ParameterMapping[] definedVariableNames;
  
  public TransFromFileConnectionInfo(final Element connection)
  {
    transformationFile = ((String) connection.selectObject("string(./KtrFile)"));
    final List argsList = connection.elements("arguments");
    final String[] args = new String[argsList.size()];
    for (int i = 0; i < argsList.size(); i++)
    {
      final Element element = (Element) argsList.get(i);
      args[i] = element.getText();
    }
    definedArgumentNames = args;

    final List varsList = connection.elements("variables");
    final ParameterMapping[] vars = new ParameterMapping[varsList.size()];
    for (int i = 0; i < varsList.size(); i++)
    {
      final Element element = (Element) varsList.get(i);
      final String dataRowName = element.attributeValue("datarow-name");
      final String variableName = element.attributeValue("variable-name");
      if (variableName == null)
      {
        vars[i] = new ParameterMapping(dataRowName, dataRowName);
      }
      else
      {
        vars[i] = new ParameterMapping(dataRowName, variableName);
      }
    }
    definedVariableNames = vars;
  }

  public String getTransformationFile()
  {
    return transformationFile;
  }

  public String[] getDefinedArgumentNames()
  {
    return definedArgumentNames;
  }

  public ParameterMapping[] getDefinedVariableNames()
  {
    return definedVariableNames;
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final TransFromFileConnectionInfo that = (TransFromFileConnectionInfo) o;

    if (!Arrays.equals(definedArgumentNames, that.definedArgumentNames))
    {
      return false;
    }
    if (!Arrays.equals(definedVariableNames, that.definedVariableNames))
    {
      return false;
    }
    if (transformationFile != null ? !transformationFile.equals(that.transformationFile) : that.transformationFile != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = transformationFile != null ? transformationFile.hashCode() : 0;
    result = 31 * result + (definedArgumentNames != null ? Arrays.hashCode(definedArgumentNames) : 0);
    result = 31 * result + (definedVariableNames != null ? Arrays.hashCode(definedVariableNames) : 0);
    return result;
  }
}
