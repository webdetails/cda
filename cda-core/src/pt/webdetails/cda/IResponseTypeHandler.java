/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda;

//import javax.servlet.http.HttpServletResponse;

import java.util.Locale;

/**
 *
 * @author joao
 */
public interface IResponseTypeHandler {
    
    
    public void setResponseHeaders(final String mimeType, final int cacheDuration, final String attachmentName);
    public boolean hasResponse();
    public Locale getLocale();
}
