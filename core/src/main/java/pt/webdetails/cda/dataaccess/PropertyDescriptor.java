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
