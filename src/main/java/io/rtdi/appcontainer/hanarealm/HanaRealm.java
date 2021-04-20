package io.rtdi.appcontainer.hanarealm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.realm.RealmBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * The Hana Realm is an authentication method for tomcat to use the SAP Hana database as authenticator.
 * In addition to that, the Principal returned by this Realm has all the Hana roles the user has assigned to as well.
 * 
 * There are two ways to set the hanajdbcurl
 * 1. In the server.xml as property &lt;Realm className="io.rtdi.appcontainer.hanarealm.HanaRealm" hanaJDBCURL="jdbc:sap://hanawd:39015/HXE"/&gt;
 * 2. As environment variable JDBCURL
 *
 */
public class HanaRealm extends RealmBase {
    private static final Log log = LogFactory.getLog(HanaRealm.class);
    private String hanajdbcurl;
    private Map<String, HanaPrincipal> userdirectory = new HashMap<>();

	public HanaRealm() {
	}

	@Override
	public HanaPrincipal authenticate(String username, String credentials) {
		if (hanajdbcurl == null) {
			hanajdbcurl = System.getenv("JDBCURL");
			if (hanajdbcurl == null) {
				log.debug("No jdbc-url configured, neither as property in the server.xml nor as environment variable JDBCURL");
				return null;
			}
		}
		log.debug("Authenticating user \"" + username + "\" with database \"" + hanajdbcurl + "\"");
		try {
			HanaPrincipal principal = userdirectory.get(username);
			if (principal == null ) { 
				principal = new HanaPrincipal(username, credentials, hanajdbcurl); // this does throw a SQLException in case the login data is invalid
				userdirectory.put(username, principal);
			}
			return principal;
		} catch (SQLException e) {
			log.debug("failed to login with the provided credentials for \"" + username + "\" with database \"" + hanajdbcurl + "\" and exception " + e.getMessage());
			return null;
		}
	}

	/**
	 * Actually returns null for security reasons
	 */
	@Override
	protected String getPassword(String username) {
		return null; // Do not expose the password. What is the side effect of that with md5 digest???
	}

	/**
	 * Get the HanaPrincipal associated with the specified user
	 * @return HanaPrincipal
	 */
	@Override
	protected HanaPrincipal getPrincipal(String username) {
		return userdirectory.get(username);
	}
	
	/**
	 * @return JDBC URL of the used database
	 */
	public String getJDBCURL() {
		return hanajdbcurl;
	}

	/**
	 * @param hanajdbcurl the JDBC URL to be used
	 */
	public void setJDBCURL(String hanajdbcurl) {
		this.hanajdbcurl = hanajdbcurl;
	}
}
