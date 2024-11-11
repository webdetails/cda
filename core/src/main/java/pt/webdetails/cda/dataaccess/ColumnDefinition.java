/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.dataaccess;

import org.dom4j.Element;

import pt.webdetails.cda.xml.DomVisitor;

public class ColumnDefinition {

  public enum TYPE {
    COLUMN, CALCULATED_COLUMN
  }


  private TYPE type;
  private Integer index;
  private String name;
  private String formula;

  public ColumnDefinition() {
  }

  public ColumnDefinition( final Element p ) {

    this();

    setName( p.selectSingleNode( "Name" ).getText() );

    if ( p.getName().equals( "CalculatedColumn" ) ) {
      setType( TYPE.CALCULATED_COLUMN );
      setFormula( p.selectSingleNode( "Formula" ).getText() );
    } else {
      setType( TYPE.COLUMN );
      setIndex( Integer.parseInt( p.attributeValue( "idx" ) ) );
    }

  }

  public TYPE getType() {
    return type;
  }

  public void setType( final TYPE type ) {
    this.type = type;
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex( final Integer index ) {
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public void setName( final String name ) {
    this.name = name;
  }

  public String getFormula() {
    return formula;
  }

  public void setFormula( final String formula ) {
    this.formula = formula;
  }

  public void accept( DomVisitor xmlVisitor, Element daEle ) {
    xmlVisitor.visit( this, daEle );
  }

}
