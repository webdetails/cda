package pt.webdetails.cda.utils.kettle;

import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.RowProducer;

public abstract class RowProducerBridge
{
  private final AtomicBoolean started  = new AtomicBoolean(false);
  private final AtomicBoolean finished = new AtomicBoolean(false);
  private RowMetaInterface    rowMeta;
  private RowProducer         rowProducer;

  public void setRowProducer(final RowProducer rowProducer)
  {
    this.rowProducer = rowProducer;
  }

  public void start(final RowMetaInterface rowMeta)
  {
    if (rowProducer == null) throw new IllegalStateException("RowProducer is null and RowProducerBridge.start() was called.");
    if (rowMeta == null) throw new IllegalStateException("RowMeta is null");
    if (started.getAndSet(true)) throw new IllegalStateException("RowProducerBridge was already started");
    this.rowMeta = rowMeta;
  }

  public void putRow(final Object[] row)
  {
    if (!started.get()) throw new IllegalStateException("RowProducerBridge not started");
    if (finished.get()) throw new IllegalStateException("RowProducerBridge already finished");
    rowProducer.putRow(rowMeta, row);
  }

  public void finish()
  {
    finished.set(true);
    rowProducer.finished();
  }
}
