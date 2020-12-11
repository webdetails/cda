/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.push;

import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.RowMetaAndData;
import pt.webdetails.cda.CdaCoreService;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.dataaccess.StreamingDataservicesDataAccess;
import pt.webdetails.cda.exporter.TableExporter;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cda.utils.QueryParameters;
import pt.webdetails.cda.utils.kettle.RowMetaToTableModel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WebsocketJsonQueryEndpoint implements IWebsocketEndpoint {

  private static final Log logger = LogFactory.getLog( WebsocketJsonQueryEndpoint.class );

  public static final String ACCEPTED_SUB_PROTOCOL = "JSON-CDA-query";

  private PublishSubject<List<RowMetaAndData>> consumer;
  private WebsocketDisposableObserver<List<RowMetaAndData>> disposableConsumer;

  private QueryParameters queryParametersUtil;

  public WebsocketJsonQueryEndpoint( ) {
    this.queryParametersUtil = new QueryParameters();
  }

  public WebsocketJsonQueryEndpoint( QueryParameters queryParameters ) {
    this.queryParametersUtil = queryParameters;
  }

  public void setQueryParametersUtil( QueryParameters queryParametersUtil ) {
    this.queryParametersUtil = queryParametersUtil;
  }

  public QueryParameters getQueryParametersUtil() {
    return queryParametersUtil;
  }

  @Override public void onOpen( Consumer<String> outboundMessageConsumer ) {

  }

  @Override
  public void onMessage( String message, Consumer<String> outboundMessageConsumer ) {
    try {
      Map<String, List<String>> params = queryParametersUtil.getParametersFromJson( message );
      DoQueryParameters parameters = queryParametersUtil.getDoQueryParameters( params );

      final String path = parameters.getPath();
      final CdaEngine cdaEngine = CdaEngine.getInstance();
      final CdaSettings cdaSettings = cdaEngine.getSettingsManager().parseSettingsFile( path );
      final QueryOptions queryOptions = CdaCoreService.getQueryOptions( parameters );
      final TableExporter tableExporter = cdaEngine.getExporter( queryOptions );

      StreamingDataservicesDataAccess dataAccess = (StreamingDataservicesDataAccess) cdaSettings.getDataAccess( queryOptions.getDataAccessId() );

      this.consumer = PublishSubject.create();

      consumer.toFlowable( BackpressureStrategy.LATEST )
        .onBackpressureBuffer( 1, () -> { }, BackpressureOverflowStrategy.DROP_OLDEST )
        .map( RowMetaToTableModel.getConverter()::apply )
        .subscribe( tableModel -> {
          try ( OutputStream outputStream = new ByteArrayOutputStream() ) {
            tableExporter.export( outputStream, tableModel );
            outboundMessageConsumer.accept( outputStream.toString() );
          } catch ( Exception e ) {
            logger.error( "Error converting a stream push table model into a json output", e );
          }
        } );

      disposableConsumer = new WebsocketDisposableObserver<>( consumer );

      dataAccess.doPushStreamQuery( queryOptions, disposableConsumer );

    } catch ( Exception e ) {
      logger.error( "Error processing JSON query message.", e );
      throw new RuntimeException( e );
    }
  }

  @Override public void onClose() {
    if ( this.consumer != null ) {
      this.consumer.onComplete();
    }
    if ( this.disposableConsumer != null && !this.disposableConsumer.isDisposed() ) {
      this.disposableConsumer.dispose();
    }
  }

  public static class WebsocketDisposableObserver<T> implements Observer<T>, Disposable {

    private Observer<T> actual;
    private Disposable toDispose;

    public WebsocketDisposableObserver( Observer<T> subject ) {
      this.actual = subject;
    }

    @Override
    public void dispose() {
      if ( toDispose != null ) {
        toDispose.dispose();
      }
    }

    @Override
    public boolean isDisposed() {
      return toDispose != null && toDispose.isDisposed();
    }

    @Override
    public void onSubscribe( Disposable d ) {
      toDispose = d;
      actual.onSubscribe( d );
    }

    @Override
    public void onNext( T t ) {
      actual.onNext( t );
    }

    @Override
    public void onError( Throwable e ) {
      actual.onError( e );
    }

    @Override
    public void onComplete() {
      actual.onComplete();
    }
  }

  public PublishSubject<List<RowMetaAndData>> getConsumer() {
    return consumer;
  }

  public WebsocketDisposableObserver<List<RowMetaAndData>> getDisposableConsumer() {
    return disposableConsumer;
  }

  @Override
  public List<String> getSubProtocols() {
    return Collections.singletonList( ACCEPTED_SUB_PROTOCOL );
  }

  private CdaCoreService getCdaCoreService() {
    return new CdaCoreService( CdaEngine.getInstance() );
  }

}
