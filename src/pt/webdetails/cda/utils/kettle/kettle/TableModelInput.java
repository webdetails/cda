package pt.webdetails.cda.utils.kettle.kettle;

import java.util.concurrent.Callable;

import javax.swing.table.TableModel;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

public class TableModelInput
{
    private String stepName;
    private StepMeta stepMeta;
    private int copyNumber;
    private RowProducer rowProducer;

    public TableModelInput(String stepName) { this(stepName, 0); }
    public TableModelInput(String stepName, int copyNumber)
    {
        super();
        this.stepName = stepName;
        this.copyNumber = copyNumber;
        
        StepLoader stepLoader = StepLoader.getInstance();
        InjectorMeta im = new InjectorMeta();
        String imPID = stepLoader.getStepPluginID(im);
        this.stepMeta = new StepMeta(imPID, stepName, im);
    }
    
    public String getStepName()
    {
        return stepName;
    }
    public StepMeta getStepMeta()
    {
        return stepMeta;
    }

    public void connectRowProducer(Trans trans) throws KettleException
    {
        rowProducer = trans.addRowProducer(stepName, copyNumber);
    }
    
    public Callable<Boolean> produceRows(final TableModel tableModel)
    {
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call()
            {
                if (rowProducer == null)
                    throw new IllegalAccessError("Invalid call order.  Must be:\nconnectRowProducer(trans);\ntrans.startThreads();\nproduceRows(tableModel);\n");
                
                RowMetaInterface rowMeta = getRowMetaForTableModel(tableModel);
                for (int i = 0; i < tableModel.getRowCount(); i++)
                {
                    Object[] row = new Object[tableModel.getColumnCount()];
                    for (int j = 0; j < tableModel.getColumnCount(); j++)
                    {
                        row[j] = getDataObjectForColumn(rowMeta.getValueMeta(j), tableModel.getValueAt(i, j));
                    }
                    rowProducer.putRow(rowMeta, row);
                }
                rowProducer.finished();
                return true;
            }
        };
        return callable;
    }
    private Object getDataObjectForColumn(ValueMetaInterface valueMeta, Object value)
    {
        Object newValue;
        switch (valueMeta.getType())
        {
            case ValueMetaInterface.TYPE_STRING:
                newValue = value;
                break;
            case ValueMetaInterface.TYPE_NUMBER:
                if (value instanceof Double) newValue = value;
                else newValue = Double.valueOf(value.toString());
                break;
            case ValueMetaInterface.TYPE_INTEGER:
                if (value instanceof Long) newValue = value;
                else newValue = Long.valueOf(value.toString());
                break;
            case ValueMetaInterface.TYPE_DATE:
                newValue = value;
                break;
            case ValueMetaInterface.TYPE_BIGNUMBER:
                if (value instanceof java.math.BigDecimal) newValue = value;
                else newValue = java.math.BigDecimal.valueOf(((java.math.BigInteger)value).doubleValue());
                break;
            case ValueMetaInterface.TYPE_BOOLEAN:
                newValue = value;
                break;
            default:
                throw new IllegalArgumentException(String.format("ValueMeta mismatch %s (%s)", valueMeta.toString(), value));
        }
        return newValue;
    }
    private RowMetaInterface getRowMetaForTableModel(TableModel tableModel) throws IllegalArgumentException
    {
        RowMetaInterface rowMeta = new RowMeta();
        for (int i = 0; i < tableModel.getColumnCount(); i++)
        {
            Class<?> columnClass = tableModel.getColumnClass(i);
            while (columnClass != Object.class) {
                if (columnClass == String.class) {
                    rowMeta.addValueMeta(new ValueMeta(tableModel.getColumnName(i), ValueMeta.TYPE_STRING));
                    break;
                    
                } else if (columnClass == java.util.Date.class) {
                    rowMeta.addValueMeta(new ValueMeta(tableModel.getColumnName(i), ValueMeta.TYPE_DATE));
                    break;
                    
                } else if (columnClass == Boolean.class) {
                    rowMeta.addValueMeta(new ValueMeta(tableModel.getColumnName(i), ValueMeta.TYPE_BOOLEAN));
                    break;
                    
                } else if (columnClass == java.math.BigDecimal.class ||
                        columnClass == java.math.BigInteger.class) {
                    rowMeta.addValueMeta(new ValueMeta(tableModel.getColumnName(i), ValueMeta.TYPE_BIGNUMBER));
                    break;
                    
                } else if (columnClass == Long.class ||
                        columnClass == Short.class ||
                        columnClass == Integer.class ||
                        columnClass == Byte.class) {
                    rowMeta.addValueMeta(new ValueMeta(tableModel.getColumnName(i), ValueMeta.TYPE_INTEGER));
                    break;
                    
                } else if (columnClass == Double.class ||
                        columnClass == Float.class) {
                    rowMeta.addValueMeta(new ValueMeta(tableModel.getColumnName(i), ValueMeta.TYPE_NUMBER));
                    break;
                    
                } else {
                    columnClass = columnClass.getSuperclass();
                }
            }
            if (columnClass == Object.class)
                throw new IllegalArgumentException(String.format("No type conversion found for Column %d %s", i, tableModel.getColumnName(i)));
        }
        
        return rowMeta;
    }
}
