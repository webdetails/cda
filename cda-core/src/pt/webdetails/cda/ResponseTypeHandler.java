/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

/**
 *
 * @author joao
 */
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
public class ResponseTypeHandler implements IResponseTypeHandler {

    private HttpServletResponse response;
    public ResponseTypeHandler(){}
    public ResponseTypeHandler(HttpServletResponse response){this.response=response;}
    public void setHttpServletResponse(HttpServletResponse response){this.response=response;}
    @Override
    public boolean hasResponse(){return this.response!=null;}
    @Override
    public void setResponseHeaders(String mimeType, int cacheDuration, String attachmentName) {
        
        // Make sure we have the correct mime type
        //FIXME what to do with mimes?
      /*
      final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
      if (mimeTypeListener != null)
      {
        mimeTypeListener.setMimeType(mimeType);
      }
      */
      response.setHeader("Content-Type", mimeType);

      if (attachmentName != null)
      {
        response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
      } // Cache?

      if (cacheDuration > 0)
      {
        response.setHeader("Cache-Control", "max-age=" + cacheDuration);
      }
      else
      {
        response.setHeader("Cache-Control", "max-age=0, no-store");
      }
    }

    @Override
    public Locale getLocale() {
      return response.getLocale();
    }
    
}
