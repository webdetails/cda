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

package pt.webdetails.cda.dataaccess.streaming;

import javax.swing.table.TableModel;

import io.reactivex.ObservableSource;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.dataaccess.SimpleDataAccess;
import pt.webdetails.cda.query.QueryOptions;

/**
 * 
 */
public interface IStreamingDataAccess extends DataAccess {

  /**
   * 
   * @param queryOptions
   * @return
   * @throws QueryException
   */
  IPushWindowQuery doPushStreamQuery( QueryOptions queryOptions ) throws QueryException;

  public interface IPushWindowQuery  extends SimpleDataAccess.IDataSourceQuery {
    ObservableSource<TableModel> getTableSource();
  }
}
