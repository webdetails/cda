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

package pt.webdetails.cda.utils.kettle;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;

/**
 * Counts processed rows
 * 
 * @author Michael Spector
 */
public class RowCountListener implements RowListener {
	private int rowsRead;
	private int rowsWritten;
	private int rowsError;

	public RowCountListener() {
	}

	public void rowReadEvent(final RowMetaInterface rowMeta, final Object[] row) {
		rowsRead++;
	}

	public void rowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row) {
		rowsWritten++;
	}

	public void errorRowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row) {
		rowsError++;
	}

	public int getRowsRead() {
		return rowsRead;
	}

	public int getRowsWritten() {
		return rowsWritten;
	}

	public int getRowsError() {
		return rowsError;
	}
}
