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

package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.metadata.model.concept.types.JoinType;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransConfig;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransConfig.EntryType;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransMetaConfig;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransMetaConfig.Type;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransformation;
import pt.webdetails.cda.utils.kettle.RowMetaToTableModel;
import plugins.org.pentaho.di.robochef.kettle.RowProductionManager;
import plugins.org.pentaho.di.robochef.kettle.TableModelInput;
import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.utils.MetadataTableModel;


/**
 * Created by IntelliJ IDEA. User: pedro Date: Feb 16, 2010 Time: 11:38:19 PM
 */
public class JoinCompoundDataAccess extends CompoundDataAccess implements RowProductionManager
{

  private static final Log logger = LogFactory.getLog(JoinCompoundDataAccess.class);
  private static final String TYPE = "join";
  private JoinType joinType;
  private String leftId;
  private String rightId;
  private String[] leftKeys;
  private String[] rightKeys;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();
  private static final long DEFAULT_ROW_PRODUCTION_TIMEOUT = 120;
  private static TimeUnit DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT = TimeUnit.SECONDS;
  
  private static int DEFAULT_MAX_ROWS_VALUE_TYPE_SEARCH = 500;//max nbr of rows to search for value
  public static final String MAX_ROWS_VALUE_TYPE_SEARCH_PROPERTY = "pt.webdetails.cda.TypeSearchMaxRows";

  public JoinCompoundDataAccess()
  {
  }


  public JoinCompoundDataAccess(final Element element)
  {
    super(element);

    Attribute joinTypeAttr = element.attribute("joinType");
    if (joinTypeAttr != null) {
        joinType = JoinType.valueOf(joinTypeAttr.getValue());
    } else {
        joinType = JoinType.FULL_OUTER;
    }

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = right.attributeValue("id");

    leftKeys = left.attributeValue("keys").split(",");
    rightKeys = right.attributeValue("keys").split(",");
  }


  public String getType()
  {
    return TYPE;
  }


  protected TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException
  {
    TableModel output = null;
    inputCallables.clear();

    try
    {
      QueryOptions croppedOptions = (QueryOptions) queryOptions.clone();
      croppedOptions.setSortBy(new ArrayList<String>());
      croppedOptions.setPageSize(0);
      croppedOptions.setPageStart(0);
      final TableModel tableModelA = this.getCdaSettings().getDataAccess(leftId).doQuery(croppedOptions);
      final TableModel tableModelB = this.getCdaSettings().getDataAccess(rightId).doQuery(croppedOptions);

      if (tableModelA.getColumnCount() == 0 || tableModelB.getColumnCount() == 0)
      {
        return new MetadataTableModel(new String[0], new Class[0], 0);

      }

      String[] leftColumnNames = new String[leftKeys.length];
      for (int i = 0; i < leftKeys.length; i++)
      {
        leftColumnNames[i] = tableModelA.getColumnName(Integer.parseInt(leftKeys[i]));
      }

      String[] rightColumnNames = new String[rightKeys.length];
      for (int i = 0; i < rightKeys.length; i++)
      {
        rightColumnNames[i] = tableModelB.getColumnName(Integer.parseInt(rightKeys[i]));
      }

      String sortLeftXML = getSortXmlStep("sortLeft", leftColumnNames);
      String sortRightXML = getSortXmlStep("sortRight", rightColumnNames);

      StringBuilder mergeJoinXML = new StringBuilder(
    		  "<step><name>mergeJoin</name><type>MergeJoin</type><join_type>");
	  switch (joinType) {
		  case INNER:
			  mergeJoinXML.append("INNER");
			  break;
		  case LEFT_OUTER:
			  mergeJoinXML.append("LEFT OUTER");
			  break;
		  case RIGHT_OUTER:
			  mergeJoinXML.append("RIGHT OUTER");
			  break;
		  case FULL_OUTER:
			  mergeJoinXML.append("FULL OUTER");
			  break;
	  }
	  mergeJoinXML.append("</join_type><step1>sortLeft</step1><step2>sortRight</step2>");
	  mergeJoinXML.append("<keys_1>");

      for (int i = 0; i < leftKeys.length; i++)
      {
        mergeJoinXML.append("<key>").append(leftColumnNames[i]).append("</key>");
      }
      mergeJoinXML.append("</keys_1><keys_2>");
      for (int i = 0; i < rightKeys.length; i++)
      {
        mergeJoinXML.append("<key>").append(rightColumnNames[i]).append("</key>");
      }
      mergeJoinXML.append("</keys_2></step>");

      DynamicTransMetaConfig transMetaConfig = new DynamicTransMetaConfig(Type.EMPTY, "JoinCompoundData", null, null);
      DynamicTransConfig transConfig = new DynamicTransConfig();

      String input1Xml = getInjectorStepXmlString("input1", tableModelA);
      String input2Xml = getInjectorStepXmlString("input2", tableModelB);

      transConfig.addConfigEntry(EntryType.STEP, "input1", input1Xml);
      transConfig.addConfigEntry(EntryType.STEP, "input2", input2Xml);
      transConfig.addConfigEntry(EntryType.STEP, "sortLeft", sortLeftXML);
      transConfig.addConfigEntry(EntryType.STEP, "sortRight", sortRightXML);
      transConfig.addConfigEntry(EntryType.STEP, "mergeJoin", mergeJoinXML.toString());

      transConfig.addConfigEntry(EntryType.HOP, "input1", "sortLeft");
      transConfig.addConfigEntry(EntryType.HOP, "input2", "sortRight");
      transConfig.addConfigEntry(EntryType.HOP, "sortLeft", "mergeJoin");
      transConfig.addConfigEntry(EntryType.HOP, "sortRight", "mergeJoin");

      TableModelInput input1 = new TableModelInput();
      transConfig.addInput("input1", input1);
      inputCallables.add(input1.getCallableRowProducer(tableModelA, true));
      TableModelInput input2 = new TableModelInput();
      transConfig.addInput("input2", input2);
      inputCallables.add(input2.getCallableRowProducer(tableModelB, true));

      RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);
      transConfig.addOutput("mergeJoin", outputListener);

      DynamicTransformation trans = new DynamicTransformation(transConfig, transMetaConfig);
      trans.executeCheckedSuccess(null, null, this);
      logger.info(trans.getReadWriteThroughput());
      output = outputListener.getRowsWritten();
    }
    catch (UnknownDataAccessException e)
    {
      throw new QueryException("Unknown Data access in CompoundDataAccess ", e);
    }
    catch (Exception e)
    {
      throw new QueryException("Exception during query ", e);
    }

    return output;
  }


  private String getSortXmlStep(final String name, final String[] columnNames)
  {

    StringBuilder sortXML = new StringBuilder(
            "  <step>\n"
            + "    <name>" + name + "</name>\n"
            + "    <type>SortRows</type>\n"
            + "    <description/>\n"
            + "    <distribute>Y</distribute>\n"
            + "    <copies>1</copies>\n"
            + "         <partitioning>\n"
            + "           <method>none</method>\n"
            + "           <schema_name/>\n"
            + "           </partitioning>\n"
            + "      <directory>%%java.io.tmpdir%%</directory>\n"
            + "      <prefix>out</prefix>\n"
            + "      <sort_size/>\n"
            + "      <free_memory>25</free_memory>\n"
            + "      <compress>N</compress>\n"
            + "      <compress_variable/>\n"
            + "      <unique_rows>N</unique_rows>\n"
            + "    <fields>\n");

    for (int i = 0; i < columnNames.length; i++)
    {
      sortXML.append("      <field>\n"
              + "        <name>" + columnNames[i] + "</name>\n"
              + "        <ascending>Y</ascending>\n"
              + "        <case_sensitive>N</case_sensitive>\n"
              + "      </field>\n");
    }

    sortXML.append("    </fields>\n"
            + "     <cluster_schema/>\n"
            + " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n"
            + "      <xloc>615</xloc>\n"
            + "      <yloc>188</yloc>\n"
            + "      <draw>Y</draw>\n"
            + "      </GUI>\n"
            + "    </step>\n");

    return sortXML.toString();
  }


  private String getInjectorStepXmlString(String name, TableModel t)
  {
    StringBuilder xml = new StringBuilder("<step><name>");
    Class<?> columnClass;
    xml.append(name).append("</name><type>Injector</type>");

    int maxRowsTypeSearch = getMaxTypeSearchRowCount(t);
    
    // If we have metadata information, put it here
    if (t.getColumnCount() > 0)
    {

      xml.append("<fields>");
      for (int i = 0; i < t.getColumnCount(); i++)
      {
        /* The proper way to get the column class is from t.getColumnClass().
         * However, this always returns Object when the column at hand is a
         * Calculated Column -- and we have no idea what to do with Objects.
         * Therefore, we try to infer the correct type from the getClass() of
         * the chosen column, first row, as that can't be worse than trying
         * to deal with Object.
         */
        columnClass = t.getColumnClass(i);
        if (columnClass.equals(Object.class) && t.getRowCount() > 0){
          for(int j = 0; j < maxRowsTypeSearch; j++){
            if(t.getValueAt(j, i) != null){
              columnClass = t.getValueAt(j, i).getClass();
              break;
            }
          }
        }
        xml.append("<field>");
        xml.append("<name>" + t.getColumnName(i) + "</name>");
        xml.append("<type>" + getKettleTypeFromColumnClass(columnClass) + "</type>");
        xml.append("<length>-1</length><precision>-1</precision></field>");
      }
      xml.append("</fields>");

    }

    xml.append("</step>");
    return xml.toString();

  }


  private int getMaxTypeSearchRowCount(TableModel t) {
    int maxRowsTypeSearch = DEFAULT_MAX_ROWS_VALUE_TYPE_SEARCH;
    String maxRowsTypeSearchProperty = CdaBoot.getInstance().getGlobalConfig().getConfigProperty(MAX_ROWS_VALUE_TYPE_SEARCH_PROPERTY);
    if(!StringUtils.isEmpty(maxRowsTypeSearchProperty)){
      try{
        maxRowsTypeSearch = Integer.parseInt(maxRowsTypeSearchProperty);
      }catch (NumberFormatException nfe){
        logger.error(MAX_ROWS_VALUE_TYPE_SEARCH_PROPERTY + ":" + maxRowsTypeSearchProperty + " not a valid integer.");
      }
    }
    if(maxRowsTypeSearch <= 0){
      maxRowsTypeSearch = t.getRowCount();
    }
    else {
      maxRowsTypeSearch = Math.min(maxRowsTypeSearch, t.getRowCount());
    }
    return maxRowsTypeSearch;
  }

  /*
   * This method returns the correct kettle type from the column class. Possible values:
   *  String
   *  Date
   *  Boolean
   *  Integer
   *  BigNumber
   *  Serializable
   *  Binary
   *  
   */

  private String getKettleTypeFromColumnClass(Class<?> clazz)
  {

    if (clazz == String.class)
    {
      return "String";
    }
    else if (clazz == Double.class)
    {
      return "Number";
    }
    else if (clazz == java.util.Date.class)
    {
      return "Date";
    }
    else if (clazz == Long.class || clazz == Integer.class)
    {
      return "Integer";
    }
    else if (clazz == java.math.BigDecimal.class)
    {
      return "BigNumber";
    }
    else if (clazz == Boolean.class )
    {
      return "Boolean";
    }
    else
    {
      throw new IllegalArgumentException("Unexpected class " + clazz + ", can't convert to kettle type");

    }


  }


  public void startRowProduction()
  {

    String timeoutStr = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeout");
    long timeout = StringUtil.isEmpty(timeoutStr) ? DEFAULT_ROW_PRODUCTION_TIMEOUT : Long.parseLong(timeoutStr);
    String unitStr = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit");
    TimeUnit unit = StringUtil.isEmpty(unitStr) ? DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT : TimeUnit.valueOf(unitStr);
    startRowProduction(timeout, unit);
  }


  public void startRowProduction(long timeout, TimeUnit unit)
  {
    try
    {
      List<Future<Boolean>> results = executorService.invokeAll(inputCallables, timeout, unit);
      for (Future<Boolean> result : results)
      {
        result.get();
      }
    } catch (InterruptedException e) {
      logger.error("Row production interrupted", e);
    } catch (ExecutionException e) {
      logger.error("Problem starting row production", e);
    }
  }
  /*
  public static ArrayList<DataAccessConnectionDescriptor> getDataAccessConnectionDescriptors() {
  ArrayList<DataAccessConnectionDescriptor> descriptor = new ArrayList<DataAccessConnectionDescriptor>();
  DataAccessConnectionDescriptor proto = new DataAccessConnectionDescriptor();
  proto.addDataAccessProperty(new PropertyDescriptor("Left",PropertyDescriptor.TYPE.STRING,PropertyDescriptor.SOURCE.DATAACCESS));
  proto.addDataAccessProperty(new PropertyDescriptor("Right",PropertyDescriptor.TYPE.STRING,PropertyDescriptor.SOURCE.DATAACCESS));
  descriptor.add(proto);
  return descriptor;
  }*/


  @Override
  public ConnectionType getConnectionType()
  {
    return ConnectionType.NONE;
  }


  public ArrayList<PropertyDescriptor> getInterface()
  {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("left", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("right", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("parameters", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("output", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    return properties;
  }

  public String getLeftId() {
	  return leftId;
  }

  public void setLeftId(String leftId) {
	  this.leftId = leftId;
  }

  public String getRightId() {
	  return rightId;
  }

  public String[] getLeftKeys() {
	  return leftKeys;
  }

  public String[] getRightKeys() {
	  return rightKeys;
  }

  @Override
  public void setQuery(String query) {
	  // Do nothing
  }
}
