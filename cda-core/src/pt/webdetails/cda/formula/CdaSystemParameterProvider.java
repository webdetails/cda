package pt.webdetails.cda.formula;

import java.util.Properties;

public class CdaSystemParameterProvider implements ICdaParameterProvider {

	Properties props = System.getProperties();

	@Override
	public Object getParameter(String name) {
		if (props.contains(name)) {
			return props.get(name);
		}
		return null;
	}
}
