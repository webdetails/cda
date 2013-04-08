/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.connections;

import java.util.ArrayList;

import pt.webdetails.cda.connections.ConnectionCatalog.ConnectionType;
import pt.webdetails.cda.dataaccess.PropertyDescriptor;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.xml.DomVisitable;

/**
 * Holds the Connections Settings of a file
 *
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:44:01 PM
 */
public interface Connection extends DomVisitable {

  public String getId();

  public String getType();

  public ConnectionType getGenericType();

  public CdaSettings getCdaSettings();

  public void setCdaSettings(CdaSettings cdaSettings);

  public ArrayList<PropertyDescriptor> getProperties();

  public String getTypeForFile();
}
