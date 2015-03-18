/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cda.dataaccess;

public class PropertyDescriptor {

  public static enum Type {

    STRING, ARRAY, BOOLEAN, NUMERIC
  }

  public static enum Placement {

    ATTRIB, CHILD
  }

  public static enum Source {

    CONNECTION, DATAACCESS
  }

  public static enum ArrayType {

    INLINE, CHILDREN
  }

  private String name;
  private Type type;
  private Source source;
  private Placement placement;
  private String value;

  public PropertyDescriptor( final String name, final Type type, final Placement placement ) {
    this.name = name;
    this.type = type;
    this.placement = placement;
  }

  public String getName() {
    return name;
  }

  public void setName( final String name ) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType( final Type type ) {
    this.type = type;
  }

  public Source getSource() {
    return source;
  }

  public void setSource( final Source source ) {
    this.source = source;
  }

  public String getValue() {
    return value;
  }

  public void setValue( final String value ) {
    this.value = value;
  }

  public Placement getPlacement() {
    return this.placement;
  }

  public void setPlacement( Placement placement ) {
    this.placement = placement;
  }
}
