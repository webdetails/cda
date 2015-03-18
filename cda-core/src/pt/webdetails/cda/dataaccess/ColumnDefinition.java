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
