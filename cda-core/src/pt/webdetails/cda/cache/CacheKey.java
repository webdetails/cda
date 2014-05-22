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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class CacheKey implements Serializable {

  private static final long serialVersionUID = -1273843592305584696L;

  private ArrayList<KeyValuePair> keyValuePairs = new ArrayList<KeyValuePair>();

  public CacheKey() {
  }

  public CacheKey( String key, String value ) {
    addKeyValuePair( key, value );
  }

  public ArrayList<KeyValuePair> getKeyValuePairs() {
    if ( keyValuePairs == null ) {
      keyValuePairs = new ArrayList<KeyValuePair>();
    }
    return keyValuePairs;
  }

  public void addKeyValuePair( String key, String value ) {
    if ( !StringUtils.isEmpty( key ) && getByKey( key ) == null ) {
      getKeyValuePairs().add( new KeyValuePair( key, value ) );
    }
  }

  public void removeByKey( String key ) {
    if ( !StringUtils.isEmpty( key ) ) {
      for ( KeyValuePair pair : getKeyValuePairs() ) {
        if ( key.equals( pair.getKey() ) ) {
          getKeyValuePairs().remove( pair );
        }
      }
    }
  }

  public KeyValuePair getByKey( String key ) {
    if ( !StringUtils.isEmpty( key ) ) {
      for ( KeyValuePair pair : getKeyValuePairs() ) {
        if ( key.equals( pair.getKey() ) ) {
          return pair;
        }
      }
    }
    return null;
  }

  @Override
  public boolean equals( final Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final CacheKey other = (CacheKey) o;

    if ( keyValuePairs != null ? !keyValuePairs.equals( other.keyValuePairs ) : other.keyValuePairs != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 7;

    for ( KeyValuePair pair : getKeyValuePairs() ) {
      result = 7 * result + ( pair != null ? pair.hashCode() : 0 );
    }

    return result;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer();

    for ( KeyValuePair pair : getKeyValuePairs() ) {
      sb.append( pair != null ? pair.toString() : "null" );
    }

    return CacheKey.class.getName() + " [" + hashCode() + "]\n" + sb.toString();
  }

  @Override
  public CacheKey clone() {
    CacheKey clone = new CacheKey();
    for( KeyValuePair pair : getKeyValuePairs() ){
      clone.addKeyValuePair( pair.getKey() , pair.getValue() );
    }
    return clone;
  }

  public class KeyValuePair implements Serializable {

    private static final long serialVersionUID = -6692451693938049364L;

    private String key;
    private String value;

    public KeyValuePair( String key, String value ) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public void setKey( String key ) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue( String value ) {
      this.value = value;
    }

    public boolean equals( final Object o ) {

      if ( this == o ) {
        return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }

      final KeyValuePair other = (KeyValuePair) o;

      if ( this.key == null ? other.key != null : !this.key.equals( other.key ) ) {
        return false;
      }

      if ( this.value == null ? other.value != null : !this.value.equals( other.value ) ) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = 7;
      result = 31 * result + ( key != null ? key.hashCode() : 0 );
      result = 31 * result + ( value != null ? value.hashCode() : 0 );
      return result;
    }

    @Override
    public String toString() {
      return this.getClass().getName() + " [" + hashCode() + "]\n\tKey:[" + getKey() + "]\n\tValue:[" + getValue()
        + "]\n";
    }

  /*
   * Serializable classes that require special handling during the serialization and deserialization process should
   * implement the following methods: writeObject(java.io.ObjectOutputStream stream),
   * readObject(java.io.ObjectInputStream stream)
   *
   * @see http://docs.oracle.com/javase/6/docs/api/java/io/ObjectInputStream.html
   */

    private void readObject( java.io.ObjectInputStream in ) throws IOException, ClassNotFoundException {
      key = (String) in.readObject();
      value = (String) in.readObject();
    }

    private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
      out.writeObject( key );
      out.writeObject( value );
    }
  }
}