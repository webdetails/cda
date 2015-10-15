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

package pt.webdetails.cda.dataaccess;


import java.util.Arrays;

public class DataAccessEnums {


  public enum DATA_ACCESS_TYPE {
    SIMPLE_DATA_ACCESS,
    COMPOUND_DATA_ACCESS
  }


  public enum ACCESS_TYPE {
    PRIVATE,
    PUBLIC
  }
  
  /**
   * DataAccess instanciation from type.
   */
  public enum DataAccessInstanceType {
    
    DENORMALIZED_MDX("denormalizedMdx"),
    DENORMALIZED_OLAP4J("denormalizedOlap4j"),
    JOIN("join"),
    KETTLE("kettle"),
    MDX("mdx"),
    MQL("mql"),
    OLAP4J("olap4j"),
    REFLECTION("reflection"),
    JSON_SCRIPTABLE("jsonScriptable"),
    SCRIPTABLE("scriptable"),
    SQL("sql"),
    UNION("union"),
    XPATH("xPath");
    
    private String type;
    public String getType() { return this.type; }
    DataAccessInstanceType(String type){
      this.type = type;
    }
    
    public static DataAccessInstanceType parseType(String type){
      for (DataAccessInstanceType dataAccess : DataAccessInstanceType.values()) {
        if (dataAccess.getType().equals(type)) {
          return dataAccess;
        }
      }
      return null;
    }
  }
  
  public enum ConnectionInstanceType {
    
    Metadata("metadata.metadata"),
    
    SQL_JDBC("sql.jdbc"),
    SQL_JNDI("sql.jndi"),
    
    MONDRIAN_JDBC("mondrian.jdbc"),
    MONDRIAN_JNDI("mondrian.jndi"),
    
    OLAP4J( new String[]{ "olap4j" , "olap4j.defaultolap4j" } ),
    
    SCRIPTING("scripting.scripting"),
    
    XPATH("xpath.xPath"),
    
    KETTLE_TRANS_FROM_FILE("kettle.TransFromFile");
    
    private String[] types;
    public String[] getTypes() { return this.types; }
    ConnectionInstanceType( String type ){
      this( new String[]{ type } );
    }
    
    ConnectionInstanceType( String[] types ){
      this.types = types;
    }

    public static ConnectionInstanceType parseType( String type ){
      return parseType( new String[]{ type } );
    }

    public static ConnectionInstanceType parseType( String[] types ){
      for (ConnectionInstanceType connection : ConnectionInstanceType.values()) {
        for( String type : types ) {
          if ( Arrays.asList( connection.getTypes() ).contains( type ) ) {
            return connection;
          }
        }
      }
      return null;
    } 
  }
  
}
