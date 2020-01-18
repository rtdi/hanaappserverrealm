package io.rtdi.hanaappserver.hanarealm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.realm.RealmBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class HanaRealm extends RealmBase {
    private static final Log log = LogFactory.getLog(HanaRealm.class);
    private String hanajdbcurl;
    private Map<String, HanaPrincipal> userdirectory = new HashMap<>();

	public HanaRealm() {
	}

	

	@Override
	public HanaPrincipal authenticate(String username, String credentials) {
		log.info("Authenticating user \"" + username + "\" with database \"" + hanajdbcurl + "\"");
		try {
			HanaPrincipal principal = new HanaPrincipal(username, credentials, hanajdbcurl); // this does throw a SQLException in case the login data is invalid
			userdirectory.put(username, principal);
			return principal;
		} catch (SQLException e) {
			log.error("failed to login with the provided credentials for \"" + username + "\" with database \"" + hanajdbcurl + "\" and exception " + e.getMessage());
			return null;
		}
	}

	@Override
	protected String getPassword(String username) {
		return null; // Do not expose the password. What is the side effect of that with md5 digest???
	}

	@Override
	protected HanaPrincipal getPrincipal(String username) {
		return userdirectory.get(username);
	}
	
	/*
	 * Properties
	 */

	public String getHanaJDBCURL() {
		return hanajdbcurl;
	}

	public void setHanaJDBCURL(String hanajdbcurl) {
		this.hanajdbcurl = hanajdbcurl;
	}
}
