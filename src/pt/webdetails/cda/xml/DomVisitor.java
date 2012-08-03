package pt.webdetails.cda.xml;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;

import pt.webdetails.cda.connections.AbstractConnection;
import pt.webdetails.cda.connections.kettle.TransFromFileConnection;
import pt.webdetails.cda.connections.kettle.TransFromFileConnectionInfo;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.connections.metadata.MetadataConnectionInfo;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.connections.scripting.ScriptingConnection;
import pt.webdetails.cda.connections.scripting.ScriptingConnectionInfo;
import pt.webdetails.cda.connections.sql.JdbcConnectionInfo;
import pt.webdetails.cda.connections.sql.SqlJndiConnectionInfo;
import pt.webdetails.cda.connections.xpath.XPathConnection;
import pt.webdetails.cda.connections.xpath.XPathConnectionInfo;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.CompoundDataAccess;
import pt.webdetails.cda.dataaccess.JoinCompoundDataAccess;
import pt.webdetails.cda.dataaccess.KettleDataAccess;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.dataaccess.SimpleDataAccess;
import pt.webdetails.cda.dataaccess.UnionCompoundDataAccess;

/**
 * This class implements the xml-generation for all cda elements
 *
 * @author mgie
 * 
 */
public class DomVisitor {

	/*
	 * Connections
	 */

	// All Abstract connections...
	public void visit(AbstractConnection con, Element ele) {

		// ... get their common traits handled
		Element conEle = ele.addElement("Connection");
		conEle.addAttribute("id", con.getId());
		conEle.addAttribute("type", con.getTypeForFile());

		// ... and then are dispatched for individual handling
		if (con instanceof MetadataConnection) {
			visit((MetadataConnection) con, conEle);
		} else if (con instanceof pt.webdetails.cda.connections.sql.JdbcConnection) {
			visit((pt.webdetails.cda.connections.sql.JdbcConnection) con, conEle);			
		} else if (con instanceof pt.webdetails.cda.connections.sql.JndiConnection) {
			visit((pt.webdetails.cda.connections.sql.JndiConnection) con, conEle);		
		} else if (con instanceof pt.webdetails.cda.connections.mondrian.JdbcConnection) {
			visit((pt.webdetails.cda.connections.mondrian.JdbcConnection) con, conEle);		
		} else if (con instanceof pt.webdetails.cda.connections.mondrian.JndiConnection) {
			visit((pt.webdetails.cda.connections.mondrian.JndiConnection) con, conEle);		
		} else if (con instanceof pt.webdetails.cda.connections.olap4j.DefaultOlap4jConnection) {
			visit((pt.webdetails.cda.connections.olap4j.DefaultOlap4jConnection) con, conEle);
		} else if (con instanceof ScriptingConnection) {
			visit((ScriptingConnection) con, conEle);
		} else if (con instanceof XPathConnection) {
			visit((XPathConnection) con, conEle);
		} else if (con instanceof TransFromFileConnection) {
			visit((TransFromFileConnection) con, conEle);
		};
	}

	// ...metadata.metadata
	private void visit(MetadataConnection con, Element ele) {
		final MetadataConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("XmiFile").addText(conInfo.getXmiFile());
		ele.addElement("DomainId").addText(conInfo.getDomainId());
	}
	
	// ...sql.jdbc
	private void visit(pt.webdetails.cda.connections.sql.JdbcConnection con, Element ele) {
		final JdbcConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("Driver").addText(conInfo.getDriver());
		ele.addElement("Url").addText(conInfo.getUrl());
		ele.addElement("User").addText(conInfo.getUser());
		ele.addElement("Pass").addText(nvl(conInfo.getPass()));

	}
	
	// ...sql.jndi
	private void visit(pt.webdetails.cda.connections.sql.JndiConnection con, Element ele) {
		final SqlJndiConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("Jndi").addText(conInfo.getJndi());
	}
	
	// ...mondrian.jdbc
	private void visit(pt.webdetails.cda.connections.mondrian.JdbcConnection con, Element ele) {
		final pt.webdetails.cda.connections.mondrian.JdbcConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("Driver").addText(conInfo.getDriver());
		ele.addElement("Url").addText(conInfo.getUrl());
		ele.addElement("User").addText(conInfo.getUser());
		ele.addElement("Pass").addText(nvl(conInfo.getPass()));
		ele.addElement("Catalog").addText(conInfo.getCatalog());
		ele.addElement("Cube").addText(conInfo.getCube());
	}
	
	// ...mondrian.jndi
	private void visit(pt.webdetails.cda.connections.mondrian.JndiConnection con, Element ele) {
		final MondrianJndiConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("Jndi").addText(conInfo.getJndi());
		ele.addElement("Catalog").addText(conInfo.getCatalog());
		ele.addElement("Cube").addText(conInfo.getCube());
	}
	
	// ...olap4j
	private void visit(pt.webdetails.cda.connections.olap4j.DefaultOlap4jConnection con, Element ele) {
		final pt.webdetails.cda.connections.olap4j.Olap4jConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("Driver").addText(conInfo.getDriver());
		ele.addElement("Url").addText(conInfo.getUrl());
		ele.addElement("User").addText(conInfo.getUser());
		ele.addElement("Pass").addText(conInfo.getPass());
		ele.addElement("Role").addText(conInfo.getRole());
		for (Object key : conInfo.getProperties().keySet()) {
			String k = key.toString();
			String v = conInfo.getProperties().getProperty(k);
			ele.addElement("Property").addAttribute("name", k).addText(v);	
		}
	}

	// ...scripting.scripting
	private void visit(ScriptingConnection con, Element ele) {
		final ScriptingConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("Language").addText(conInfo.getLanguage());
		ele.addElement("InitScript").addText(nvl(conInfo.getInitScript()));
	}
	
	// ...xpath.xPath
	private void visit(XPathConnection con, Element ele) {
		final  XPathConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("DataFile").addText(conInfo.getXqueryDataFile());
	}
	
	// ...kettle.TransFromFile
	private void visit(TransFromFileConnection con, Element ele) {
		final TransFromFileConnectionInfo conInfo = con.getConnectionInfo();
		ele.addElement("KtrFile").addText(conInfo.getTransformationFile());
		//for each variable
		final ParameterMapping[] variables = conInfo.getDefinedVariableNames();
		for (int i = 0; i < variables.length; i++) {
			ParameterMapping var = variables[i];
			Element vars = ele.addElement("variables").addAttribute("datarow-name",var.getName());
			if(!var.getName().equals(var.getAlias())){
				vars.addAttribute("variable-name",var.getAlias());
			}
		}
	}

	/*
	 * DataAccess
	 */

	public void visit(SimpleDataAccess da, Element daEle) {

		daEle.addAttribute("id", da.getId());
		daEle.addAttribute("connection", da.getConnectionId());
		daEle.addAttribute("type", da.getType());
		daEle.addAttribute("access", da.getAccess().name().toLowerCase());
		if(da.isCacheEnabled()) daEle.addAttribute("cache", new Boolean(da.isCacheEnabled()).toString());
		if(da.getCacheDuration()!=3600) daEle.addAttribute("cacheDuration", "" + da.getCacheDuration());
		daEle.addElement("Name").addText(da.getName());

		
		daEle.addElement("Query").addCDATA(da.getQuery());
		//daEle.addElement("Query").addText(da.getQuery());

	}

	// Compound data accesses
	public void visit(CompoundDataAccess da, Element daEle) {

		daEle.addAttribute("id", da.getId());
		daEle.addAttribute("type", da.getType());

		if(da instanceof UnionCompoundDataAccess){
			UnionCompoundDataAccess uDa = (UnionCompoundDataAccess) da;
			daEle.addElement("Top").addAttribute("id", uDa.getTopId());
			daEle.addElement("Bottom").addAttribute("id", uDa.getBottomId());	
		}
		else if (da instanceof JoinCompoundDataAccess){
			JoinCompoundDataAccess jDa = (JoinCompoundDataAccess) da;
			daEle.addElement("Left").addAttribute("id", jDa.getLeftId())
			.addAttribute("keys", StringUtils.join(jDa.getLeftKeys(),","));
			daEle.addElement("Right").addAttribute("id", jDa.getRightId())
			.addAttribute("keys", StringUtils.join(jDa.getRightKeys(),","));
		}

	}

	// Columns
	public void visit(ColumnDefinition columnDefinition, Element daEle) {

		Element colEle = null;

		if (columnDefinition.getType().equals(ColumnDefinition.TYPE.COLUMN)) {
			colEle = daEle.addElement("Column");
			colEle.addAttribute("idx", columnDefinition.getIndex().toString());
			colEle.addElement("Name").addText(columnDefinition.getName());
		} else {
			colEle = daEle.addElement("CalculatedColumn");
			colEle.addElement("Name").addText(columnDefinition.getName());
			colEle.addElement("Formula").addText(columnDefinition.getFormula());
		}
	}

	// Parameters
	public void visit(Parameter parameter, Element daEle) {
		Element paramEle = daEle.addElement("Parameter");
		paramEle.addAttribute("name", parameter.getName());
		paramEle.addAttribute("type", parameter.getTypeAsString());
		if (parameter.getPattern() != null)
			paramEle.addAttribute("pattern", parameter.getPattern());
		// Object getDefaultValue does not make sense!
		paramEle.addAttribute("default", parameter.getDefaultValue().toString());
	    paramEle.addAttribute("separator", parameter.getSeparator());
		if (parameter.getAccess().equals(Parameter.Access.PRIVATE)){
			paramEle.addAttribute("access", parameter.getAccess().name().toLowerCase());
		}
	}

	public static String nvl(String text){
		if(text==null){
			return "";
		}else{
			return text;
		}
	}

}
