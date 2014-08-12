package pt.webdetails.cda.settings;

/**
 * Compound key for CDA settings consisting of user currently logged in and resource identifier.
 * 
 * @author Michael Spector
 */
public class SettingsKey {

	private String userName;
	private String settingsId;
	
	public SettingsKey(String userName, String settingsId) {
		this.userName = userName;
		this.settingsId = settingsId;
	}

	@Override
	public String toString() {
		return "SettingsKey [userName=" + userName + ", settingsId="
				+ settingsId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((settingsId == null) ? 0 : settingsId.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SettingsKey other = (SettingsKey) obj;
		if (settingsId == null) {
			if (other.settingsId != null)
				return false;
		} else if (!settingsId.equals(other.settingsId))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	};
}
