/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda;

public interface ICdaCoreSessionFormulaContext {
    
    
    public Object resolveReference(final Object name);

    public Object[] convertToArray(final JavaScriptResultSet resultSet);
    
    
    
    
}
