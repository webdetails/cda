/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.robochef;

import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.RowProducer;

public abstract class RowProducerBridge {
  private final AtomicBoolean started = new AtomicBoolean( false );
  private final AtomicBoolean finished = new AtomicBoolean( false );
  private RowMetaInterface rowMeta;
  private RowProducer rowProducer;

  public void setRowProducer( final RowProducer rowProducer ) {
    this.rowProducer = rowProducer;
  }

  public void start( final RowMetaInterface rowMeta ) {
    if ( rowProducer == null ) {
      throw new IllegalStateException( "RowProducer is null and RowProducerBridge.start() was called." );
    }
    if ( rowMeta == null ) {
      throw new IllegalStateException( "RowMeta is null" );
    }
    if ( started.getAndSet( true ) ) {
      throw new IllegalStateException( "RowProducerBridge was already started" );
    }
    this.rowMeta = rowMeta;
  }

  public void putRow( final Object[] row ) {
    if ( !started.get() ) {
      throw new IllegalStateException( "RowProducerBridge not started" );
    }
    if ( finished.get() ) {
      throw new IllegalStateException( "RowProducerBridge already finished" );
    }
    rowProducer.putRow( rowMeta, row );
  }

  public void finish() {
    finished.set( true );
    rowProducer.finished();
  }
}
