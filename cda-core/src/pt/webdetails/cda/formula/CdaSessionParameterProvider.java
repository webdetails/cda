package pt.webdetails.cda.formula;

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

public class CdaSessionParameterProvider implements ICdaParameterProvider {

	private ISessionUtils sessionUtils;
	
	public CdaSessionParameterProvider(ISessionUtils sessionUtils) {
		this.sessionUtils = sessionUtils;
	}

	@Override
	public Object getParameter(String name) {
		IUserSession session = sessionUtils.getCurrentSession();
		if (session != null) {
			return session.getParameter(name);
		}
		return null;
	}
}
