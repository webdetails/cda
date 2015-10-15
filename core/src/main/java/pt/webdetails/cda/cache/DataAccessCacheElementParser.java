/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
package pt.webdetails.cda.cache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cda.utils.FormulaEvaluator;

import java.util.List;

public class DataAccessCacheElementParser {

  // default value for cacheEnabled is true for DataAccess/Cache node
  public static final boolean DEFAULT_CACHE_ENABLED = true;
  private boolean cacheEnabled = DEFAULT_CACHE_ENABLED;
  private static final Log logger = LogFactory.getLog( DataAccessCacheElementParser.class );
  private static final String ATTR_CACHE_ENABLED = "enabled"; //$NON-NLS-1$
  private static final String ATTR_DURATION = "duration"; //$NON-NLS-1$
  private static final String ATTR_KEY_NAME = "name"; //$NON-NLS-1$
  private static final String ATTR_KEY_VALUE = "value"; //$NON-NLS-1$
  private static final String ATTR_KEY_DEFAULT_VALUE = "default"; //$NON-NLS-1$
  private Integer cacheDuration;
  private CacheKey cacheKey; // DataAccess/Cache/Key nodes
  private Element element; // DataAccess/Cache node


  public DataAccessCacheElementParser( Element element ) {
    this.element = element;
    this.cacheEnabled = DEFAULT_CACHE_ENABLED;
  }
  
  public boolean parseParameters() {
	boolean success = false;

    try {

      // default is true; we change it *only* if it has been explicitly set to false
      if ( contains( element, ATTR_CACHE_ENABLED )
        && element.attributeValue( ATTR_CACHE_ENABLED ).toString().equalsIgnoreCase( "false" ) ) {
        setCacheEnabled( false );
      }

      if ( contains( element, ATTR_DURATION ) && isValidPositiveInteger(
        element.attributeValue( ATTR_DURATION ).toString() ) ) {
        setCacheDuration( Integer.parseInt( element.attributeValue( ATTR_DURATION ).toString() ) );
      }

      success = true;

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }

    return success;
  }

  public boolean parseKeys() {
	boolean success = false;
	
    try {
    	cacheKey = new CacheKey();
    	
      @SuppressWarnings( "unchecked" )
      List<Element> keyNodes = element.selectNodes( "Key" ); //$NON-NLS-1$

      if ( keyNodes != null ) {
        for ( Element keyNode : keyNodes ) {
          buildKeyValuePair( keyNode );
        }
      }
      success = true;

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }

    return success;
  }

  private void buildKeyValuePair( Element keyNode ) {

    // minimum required: name and value
    if ( keyNode != null && contains( keyNode, ATTR_KEY_NAME ) && contains( keyNode, ATTR_KEY_VALUE ) ) {

      String key = keyNode.attributeValue( ATTR_KEY_NAME ).toString();

      String value = FormulaEvaluator.replaceFormula( keyNode.attributeValue( ATTR_KEY_VALUE ).toString() );

      // if no value was fetched from formula AND user defined a default-value, we apply it
      if ( StringUtils.isEmpty( value ) || value.trim().equals( "null" )
        && keyNode.attributeValue( ATTR_KEY_DEFAULT_VALUE ) != null ) {
        value = keyNode.attributeValue( ATTR_KEY_DEFAULT_VALUE ).toString();
      }

      cacheKey.addKeyValuePair( key, value );
    }
  }

  private boolean contains( Element elem, String attr ) {
    return elem != null && !StringUtils.isEmpty( elem.attributeValue( attr ) );
  }

  private boolean isValidPositiveInteger( String value ) {
    try {
      return Integer.parseInt( value ) > -1;
    } catch ( Exception e ) {
      return false;
    }
  }

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public void setCacheEnabled( boolean cacheEnabled ) {
    this.cacheEnabled = cacheEnabled;
  }

  public Integer getCacheDuration() {
    return cacheDuration;
  }

  public void setCacheDuration( Integer cacheDuration ) {
    this.cacheDuration = cacheDuration;
  }

  public CacheKey getCacheKey() {
    return cacheKey;
  }
}