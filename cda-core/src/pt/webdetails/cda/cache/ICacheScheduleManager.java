/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.cache;

/**
 *
 * @author Your Name <your.name at your.org>
 */
import java.io.OutputStream;
import pt.webdetails.cpf.http.ICommonParameterProvider;
public interface ICacheScheduleManager {
    public void handleCall(ICommonParameterProvider requParam, OutputStream out);
}
