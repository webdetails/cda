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
