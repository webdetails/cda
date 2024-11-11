/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.dataaccess.streaming;

import io.reactivex.Observer;
import org.pentaho.di.core.RowMetaAndData;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.query.QueryOptions;

import java.util.List;

/**
 * 
 */
public interface IStreamingDataAccess extends DataAccess {

  /**
   * 
   * @param queryOptions
   * @param consumer
   * @throws QueryException
   */
  void doPushStreamQuery( QueryOptions queryOptions, Observer<List<RowMetaAndData>> consumer ) throws QueryException;

}
