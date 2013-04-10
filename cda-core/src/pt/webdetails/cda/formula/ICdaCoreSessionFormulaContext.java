/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.formula;

import org.pentaho.reporting.libraries.formula.FormulaContext;

import pt.webdetails.cpf.session.IUserSession;

public interface ICdaCoreSessionFormulaContext extends FormulaContext {

    public Object resolveReference(final Object name);

    public Object[] convertToArray(/*final JavaScriptResultSet resultSet*/);
    
    public void setSession(IUserSession session);
    
    
    
}
