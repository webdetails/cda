/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda.dataaccess;


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
    
    OLAP4J("olap4j"),
    
    SCRIPTING("scripting.scripting"),
    
    XPATH("xpath.xPath"),
    
    KETTLE_TRANS_FROM_FILE("kettle.TransFromFile");
    
    private String type;
    public String getType() { return this.type; }
    ConnectionInstanceType(String type){
      this.type = type;
    }
    
    public static ConnectionInstanceType parseType(String type){
      for (ConnectionInstanceType connection : ConnectionInstanceType.values()) {
        if (connection.getType().equals(type)) {
          return connection;
        }
      }
      return null;
    } 
  }
  
}