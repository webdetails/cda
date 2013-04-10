package pt.webdetails.cda.formula;

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

public class CdaSessionParameterProvider implements ICdaParameterProvider {

	private ISessionUtils sessionUtils;
	
	public CdaSessionParameterProvider(ISessionUtils sessionUtils) {
		this.sessionUtils = sessionUtils;
	}

	@Override
	public Object getParameter(Object name) {
		IUserSession session = sessionUtils.getCurrentSession();
		if (session != null) {
			System.out.println("Found session: " + session);
			return session.getParameter(name);
		}
		return null;
	}
}
