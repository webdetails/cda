package pt.webdetails.cda;

import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.ContextEvaluationException;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.LocalizationContext;
import org.pentaho.reporting.libraries.formula.function.FunctionRegistry;
import org.pentaho.reporting.libraries.formula.operators.OperatorFactory;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.TypeRegistry;

import pt.webdetails.cpf.session.IUserSession;

public class DefaultSessionFormulaContext implements
		ICdaCoreSessionFormulaContext {

	private DefaultFormulaContext df;
	private IUserSession session;
	
	public DefaultSessionFormulaContext() {
		this.df = new DefaultFormulaContext();
	}
	
	@Override
	public Configuration getConfiguration() {
		return df.getConfiguration();
	}

	@Override
	public FunctionRegistry getFunctionRegistry() {
		return df.getFunctionRegistry();
	}

	@Override
	public LocalizationContext getLocalizationContext() {
		return df.getLocalizationContext();
	}

	@Override
	public OperatorFactory getOperatorFactory() {
		return df.getOperatorFactory();
	}

	@Override
	public TypeRegistry getTypeRegistry() {
		return df.getTypeRegistry();
	}

	@Override
	public boolean isReferenceDirty(Object name) throws ContextEvaluationException {
		return df.isReferenceDirty(name);
	}

	@Override
	public Type resolveReferenceType(Object name) throws ContextEvaluationException {
		return df.resolveReferenceType(name);
	}

	@Override
	public Object resolveReference(Object name) {
		return df.resolveReference(name);
	}

	@Override
	public Object[] convertToArray(/* here is something missing */) {
		return new Object[0];
	}

	@Override
	public void setSession(IUserSession session) {
		this.session = session;

	}

}
