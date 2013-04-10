package pt.webdetails.cda.formula;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.ContextEvaluationException;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.LocalizationContext;
import org.pentaho.reporting.libraries.formula.function.FunctionRegistry;
import org.pentaho.reporting.libraries.formula.operators.OperatorFactory;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.TypeRegistry;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

public class DefaultSessionFormulaContext implements
		ICdaCoreSessionFormulaContext {

	private DefaultFormulaContext df;
	private Map<String, ICdaParameterProvider> providers = new HashMap<String, ICdaParameterProvider>();
	
	
	public DefaultSessionFormulaContext(Map<String, ICdaParameterProvider> providers) {
		this.df = new DefaultFormulaContext();
		if (providers == null || providers.size() == 0) {
			ISessionUtils utils = CdaEngine.getEnvironment().getSessionUtils();
			if (utils != null) {
				this.providers.put("security:", new CdaSecurityParameterProvider(utils));
				this.providers.put("session:", new CdaSessionParameterProvider(utils));
			}
			this.providers.put("system:", new CdaSystemParameterProvider());
		} else {
			this.providers = providers;
		}
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
    public Object resolveReference(final Object name)
    {
      if (name instanceof String)
      {
        String paramName = ((String) name).trim();
        for (String prefix : providers.keySet())
        {
          if (paramName.startsWith(prefix))
          {
        	//logger.debug("Found provider for prefix: " + prefix + " Provider: " + providers.get(prefix));
            paramName = paramName.substring(prefix.length());
            Object value = providers.get(prefix).getParameter(paramName);
            return value;
          }
        }
      }
      return df.resolveReference(name);
    }

	@Override
	public Object[] convertToArray(/* here is something missing */) {
		return new Object[0];
	}

	@Override
	public void setSession(IUserSession session) {
		// not sure if we need it?
	}

}
