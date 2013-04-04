/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;



import pt.webdetails.cpf.session.IUserSession;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;

//import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;

/**
 *
 * @author joao
 */
public class CdaCoreSessionFormulaContext extends DefaultFormulaContext implements ICdaCoreSessionFormulaContext {

    
    
    
    public CdaCoreSessionFormulaContext(){
        
        
    }
    public CdaCoreSessionFormulaContext(IUserSession session){
        
        
    }
    
    @Override
    public Object resolveReference(Object name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] convertToArray(JavaScriptResultSet resultSet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
