package pt.webdetails.cda.formula;

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;

public class CdaSecurityParameterProvider implements ICdaParameterProvider {

	private ISessionUtils sessionUtils;
	
    private static String principalName = "principalName";
    private static String principalRoles = "principalRoles";
    private static String systemRoleNames = "systemRoleNames";
    private static String systemUserNames = "systemUserNames";
    
   	public CdaSecurityParameterProvider(ISessionUtils sessionUtils) {
		this.sessionUtils = sessionUtils;
	}
	
	@Override
	public Object getParameter(Object name) {
		if (name != null) {
			IUserSession session = sessionUtils.getCurrentSession();
			if (session != null) {
				if (principalName.equals(name)) {
					return session.getUserName();
				}
				if (principalRoles.equals(name)) {
					return convertArray(session.getAuthorities());
				}
			}
			if (systemUserNames.equals(name)) {
				return convertArray(sessionUtils.getSystemPrincipals());
			}
			if (systemRoleNames.equals(name)) {
				return convertArray(sessionUtils.getSystemAuthorities());
			}
		}
		return null;
	}
		
	private String convertArray(Object[] array) {
		if (array != null && array.length > 0) {
			String output = "";
			for (int i=0; i<array.length;i++) {
				output += (i > 0 ? "," + array[i] : array[i]);
			}
			return output;
		}
		return null;
	}
}
