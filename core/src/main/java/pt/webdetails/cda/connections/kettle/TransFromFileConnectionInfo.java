/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
    @SuppressWarnings("unchecked")
    final List<Element> argsList = connection.elements("arguments");
    final String[] args = new String[argsList.size()];
    for (int i = 0; i < argsList.size(); i++)
    {
      final Element element = (Element) argsList.get(i);
      args[i] = element.getText();
    }
    definedArgumentNames = args;

    @SuppressWarnings("unchecked")
    final List<Element> varsList = connection.elements("variables");
    final ParameterMapping[] vars = new ParameterMapping[varsList.size()];
    for ( int i = 0; i < varsList.size(); i++)
    {
      final Element element = varsList.get(i);
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
    if (!Arrays.deepEquals( parameterMappingToStringArray(definedVariableNames), parameterMappingToStringArray(that.definedVariableNames)))
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
    result = 31 * result + (definedVariableNames != null ? Arrays.deepHashCode( parameterMappingToStringArray(definedVariableNames)) : 0);
    return result;
  }
  
  private String[][] parameterMappingToStringArray(ParameterMapping[] paramMaps)
  {
   if(paramMaps == null) return null;
   String[][] result = new String[paramMaps.length][];
   for(int i = 0; i< paramMaps.length; i++){
     String[] item = new String[] {paramMaps[i].getName(), paramMaps[i].getAlias()};
     result[i] = item;
   }
   return result;
  }
}
