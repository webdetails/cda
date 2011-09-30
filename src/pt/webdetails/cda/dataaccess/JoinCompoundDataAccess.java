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
import org.dom4j.Element;
import pt.webdetails.cda.CdaProperties;
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
public class JoinCompoundDataAccess extends CompoundDataAccess implements RowProductionManager {

  private static final Log logger = LogFactory.getLog(JoinCompoundDataAccess.class);
  private static final String TYPE = "join";

  private String leftId;
  private String rightId;
  private int[] leftKeys;
  private int[] rightKeys;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();
  
  public JoinCompoundDataAccess() {
  }

  public JoinCompoundDataAccess(final Element element) {
    super(element);

    Element left = (Element) element.selectSingleNode("Left");
    Element right = (Element) element.selectSingleNode("Right");

    leftId = left.attributeValue("id");
    rightId = right.attributeValue("id");

    String[] leftKeysStr = left.attributeValue("keys").split(",");
    leftKeys = new int[leftKeysStr.length];
    for(int i=0; i<leftKeysStr.length; i++){
      leftKeys[i] = Integer.parseInt(leftKeysStr[i]);
    }
    
    String[] rightKeysStr = right.attributeValue("keys").split(",");
    rightKeys = new int[rightKeysStr.length];
    for(int i=0; i<rightKeysStr.length; i++){
      rightKeys[i] = Integer.parseInt(rightKeysStr[i]);
    }
    
  }

  public String getType() {
    return TYPE;
  }


  protected TableModel queryDataSource(final QueryOptions queryOptions) throws QueryException 
  {
    TableModel output = null;
    inputCallables.clear();

    try {
      QueryOptions croppedOptions = (QueryOptions) queryOptions.clone();
      croppedOptions.setSortBy(new ArrayList<String>());
      croppedOptions.setPageSize(0);
      croppedOptions.setPageStart(0);
     
      DataAccess left = this.getCdaSettings().getDataAccess(leftId);
      DataAccess right = this.getCdaSettings().getDataAccess(rightId);
      
      TableModel tableModelLeft = left.doQuery(croppedOptions);
      TableModel tableModelRight = right.doQuery(croppedOptions);

      if (tableModelLeft.getRowCount() == 0 || tableModelRight.getRowCount() == 0) {
        return new MetadataTableModel(new String[0], new Class[0], 0);
        
      }
      
      DynamicTransConfig transConfig = createMergeStep(tableModelLeft, tableModelRight);

      RowMetaToTableModel outputListener = new RowMetaToTableModel(false, true, false);
      transConfig.addOutput("mergeJoin", outputListener);
      
      DynamicTransMetaConfig transMetaConfig = new DynamicTransMetaConfig(Type.EMPTY, "JoinCompoundData", null, null);
      DynamicTransformation trans = new DynamicTransformation(transConfig, transMetaConfig);
            
      trans.executeCheckedSuccess(null, null, this);
      logger.info(trans.getReadWriteThroughput());
      output = outputListener.getRowsWritten();
    } catch (UnknownDataAccessException e) {
      throw new QueryException("Unknown Data access in CompoundDataAccess ", e);
    } catch (Exception e) {
      throw new QueryException("Exception during query ", e);
    }
    
    if(output == null)
    {  
      throw new QueryException("Join transformation returned null", null);
    }
//      
//      //just return empty table model
//      
//      return createBogusTableModel(0);
////      //give out an empty table with sufficient metadata
////      SortedSet<Integer> sortedIndexes = new TreeSet<Integer>();
////      for(int outIdx : getOutputs())sortedIndexes.add(outIdx); 
////        
////      output = createBogusTableModel(sortedIndexes.last() + 1);
//    }

    return output;
  }

//  private TableModel createBogusTableIfNoColumns(TableModel tableModel, int[] keys, int other) 
//  {
//    if(tableModel.getColumnCount() < 1)
//    {
//      logger.warn("Attempting to join table with no metadata. Creating bogus empty table to comply with join.");
//      
//      SortedSet<Integer> keysSorted = new TreeSet<Integer>();
//      for(int i=0; i < keys.length; i++) keysSorted.add(keys[i]);
//    
//      int nbrColumns = getOutputs().size() + 1 - other;
//      nbrColumns = Math.max(nbrColumns, keysSorted.last() + 1);
//      
//      tableModel = createBogusTableModel(nbrColumns);
//    }
//    return tableModel;
//  }

  /**
   * @param tableModelLeft
   * @param tableModelRight
   * @return
   */
  private DynamicTransConfig createMergeStep(final TableModel tableModelLeft, final TableModel tableModelRight) {
    String[] leftColumnNames = getColumnNames(tableModelLeft, leftKeys); //new String[leftKeys.length];
    String[] rightColumnNames = getColumnNames(tableModelRight, rightKeys);

    String sortLeftXML = getSortXmlStep("sortLeft", leftColumnNames);
    String sortRightXML = getSortXmlStep("sortRight", rightColumnNames);

    StringBuilder mergeJoinXML = new StringBuilder(
            "<step><name>mergeJoin</name><type>MergeJoin</type><join_type>FULL OUTER</join_type><step1>sortLeft</step1><step2>sortRight</step2>");
    
    mergeJoinXML.append("<keys_1>");
    for(String columnName: leftColumnNames){
      mergeJoinXML.append("<key>").append(columnName).append("</key>");
    }
    
    mergeJoinXML.append("</keys_1><keys_2>");
    for(String columnName: rightColumnNames){
      mergeJoinXML.append("<key>").append(columnName).append("</key>");
    }
    mergeJoinXML.append("</keys_2></step>");


    DynamicTransConfig transConfig = new DynamicTransConfig();

    transConfig.addConfigEntry(EntryType.STEP, "input1", "<step><name>input1</name><type>Injector</type></step>");
    transConfig.addConfigEntry(EntryType.STEP, "input2", "<step><name>input2</name><type>Injector</type></step>");
    transConfig.addConfigEntry(EntryType.STEP, "sortLeft", sortLeftXML);
    transConfig.addConfigEntry(EntryType.STEP, "sortRight", sortRightXML);
    transConfig.addConfigEntry(EntryType.STEP, "mergeJoin", mergeJoinXML.toString());

    transConfig.addConfigEntry(EntryType.HOP, "input1", "sortLeft");
    transConfig.addConfigEntry(EntryType.HOP, "input2", "sortRight");
    transConfig.addConfigEntry(EntryType.HOP, "sortLeft", "mergeJoin");
    transConfig.addConfigEntry(EntryType.HOP, "sortRight", "mergeJoin");

    TableModelInput input1 = new TableModelInput();
    transConfig.addInput("input1", input1);
    inputCallables.add(input1.getCallableRowProducer(tableModelLeft, true));
    TableModelInput input2 = new TableModelInput();
    transConfig.addInput("input2", input2);
    inputCallables.add(input2.getCallableRowProducer(tableModelRight, true));
    
    return transConfig;
  }

  private String[] getColumnNames(TableModel tableModel, int[] keys) {

    int columnCount = tableModel.getColumnCount();
    List<String> columnNames = new ArrayList<String>();
    for (int i = 0; i < keys.length; i++) {
      if(keys[i] >= columnCount )
      {
        logger.error("Attempting to get column " + keys[i] + " from " + columnCount + " columns. Skipping.");
      }
      else {
        columnNames.add( tableModel.getColumnName(keys[i]) );
      }
    }
    
    return columnNames.toArray(new String[columnNames.size()]);
  }
  
  private String getSortXmlStep(final String name, final String[] columnNames) {

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

    for (int i = 0; i < columnNames.length; i++) {
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

  public void startRowProduction() 
  {
    TimeUnit unit = CdaProperties.Entries.getRowProductionTimeoutUnit();
    long timeout = CdaProperties.Entries.getRowProductionTimeout();
//    String timeoutStr = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeout");
//    long timeout = StringUtil.isEmpty(timeoutStr)? DEFAULT_ROW_PRODUCTION_TIMEOUT : Long.parseLong(timeoutStr);
//    String unitStr = CdaBoot.getInstance().getGlobalConfig().getConfigProperty("pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit");
//    TimeUnit unit = StringUtil.isEmpty(unitStr)? DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT : TimeUnit.valueOf(unitStr);
    startRowProduction(timeout, unit);
  }

  public void startRowProduction(long timeout, TimeUnit unit) {
    try {
      List<Future<Boolean>> results = executorService.invokeAll(inputCallables, timeout, unit);
      for (Future<Boolean> result : results) {
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
  public ConnectionType getConnectionType() {
    return ConnectionType.NONE;
  }
  
  
//  private TableModel createBogusTableModel(int numberOfColumns)
//  {//create generic table
//    TypedTableModel tableModel = new TypedTableModel();
//    for(int i=0; i < numberOfColumns; i++){
//      tableModel.addColumn("col"+i, Object.class);
//    }
//    return tableModel;
//  }


  public ArrayList<PropertyDescriptor> getInterface() {
    ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    properties.add(new PropertyDescriptor("id", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.ATTRIB));
    properties.add(new PropertyDescriptor("left", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("right", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("parameters", PropertyDescriptor.Type.STRING, PropertyDescriptor.Placement.CHILD));
    properties.add(new PropertyDescriptor("output", PropertyDescriptor.Type.ARRAY, PropertyDescriptor.Placement.CHILD));
    return properties;
  }
}

