/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cda.utils;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * This class is used for unit test only and for now it only sets the content type.
 */
public class HttpServletResponseForTests implements HttpServletResponse {
  private String contentType;

  @Override
  public void addCookie( Cookie cookie ) {  }

  @Override
  public boolean containsHeader( String name ) {
    return false;
  }

  @Override
  public String encodeURL( String url ) {
    return null;
  }

  @Override
  public String encodeRedirectURL( String url ) {
    return null;
  }

  @Override
  public void sendError( int sc, String msg ) {  }

  @Override
  public void sendError( int sc ) {  }

  @Override
  public void sendRedirect( String location ) {  }

  @Override
  public void setDateHeader( String name, long date ) {  }

  @Override
  public void addDateHeader( String name, long date ) {  }

  @Override
  public void setHeader( String name, String value ) {  }

  @Override
  public void addHeader( String name, String value ) {  }

  @Override
  public void setIntHeader( String name, int value ) {  }

  @Override
  public void addIntHeader( String name, int value ) {  }

  @Override
  public void setStatus( int sc ) {  }


  @Override
  public int getStatus() {
    return 0;
  }

  @Override
  public String getHeader( String name ) {
    return null;
  }

  @Override
  public Collection<String> getHeaders( String name ) {
    return null;
  }

  @Override
  public Collection<String> getHeaderNames() {
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public String getContentType() {
    return this.contentType;
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return null;
  }

  @Override
  public PrintWriter getWriter() {
    return null;
  }

  @Override
  public void setCharacterEncoding( String charset ) {  }

  @Override
  public void setContentLength( int len ) {  }

  @Override
  public void setContentLengthLong(long l) {

  }

  @Override
  public void setContentType( String type ) {
    this.contentType = type;
  }

  @Override
  public void setBufferSize( int size ) {  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() {  }

  @Override
  public void resetBuffer() {  }

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {  }

  @Override
  public void setLocale( Locale loc ) {  }

  @Override
  public Locale getLocale() {
    return null;
  }
}
