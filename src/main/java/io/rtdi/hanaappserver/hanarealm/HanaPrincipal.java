package io.rtdi.hanaappserver.hanarealm;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginContext;

import org.apache.catalina.realm.GenericPrincipal;
import org.ietf.jgss.GSSCredential;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the Hana database version.
 *
 */
public class HanaPrincipal extends GenericPrincipal {

	private static final long serialVersionUID = 4658263939892656292L;
	private String hanajdbcurl;
	private String hanaversion;
	private String hanauser;

	public HanaPrincipal(String name, String password, String hanajdbcurl) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl));
		this.hanajdbcurl = hanajdbcurl;
	}

	public HanaPrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal);
		this.hanajdbcurl = hanajdbcurl;
	}

	public HanaPrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal,
			LoginContext loginContext) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal, loginContext);
		this.hanajdbcurl = hanajdbcurl;
	}

	public HanaPrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal,
			LoginContext loginContext, GSSCredential gssCredential) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal, loginContext, gssCredential);
		this.hanajdbcurl = hanajdbcurl;
		try (Connection c = getDatabaseConnection(name, password, hanajdbcurl)) {
			try (PreparedStatement stmt = c.prepareStatement("select version, current_user from m_database"); ) {
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					this.hanaversion = rs.getString(1);
					this.hanauser = rs.getString(2);
				}
			}
		}
	}

	/**
	 * @param name Hana user name
	 * @param password Hana password
	 * @param hanajdbcurl Hana JDBC connection URL
	 * @return the list of Hana role names the user has assigned, direct or indirect
	 * @throws SQLException
	 */
	public static List<String> queryRoles(String name, String password, String hanajdbcurl) throws SQLException {
		try (Connection c = getDatabaseConnection(name, password, hanajdbcurl)) {
			try (PreparedStatement stmt = c.prepareStatement("select role_name from effective_roles where user_name = current_user"); ) {
				List<String> roles = new ArrayList<String>();
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					roles.add(rs.getString(1));
				}
				return roles;
			}
		}
	}
	
	/**
	 * @return the database connection JDBC URL used
	 */
	public String getHanaJDBCURL() {
		return hanajdbcurl;
	}
	
	public Connection createNewHanaConnection() throws SQLException {
		return getDatabaseConnection(super.getName(), super.getPassword(), hanajdbcurl);
	}
	
	static Connection getDatabaseConnection(String user, String passwd, String jdbcurl) throws SQLException {
        try {
            Class.forName("com.sap.db.jdbc.Driver");
            return DriverManager.getConnection(jdbcurl, user, passwd);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No Hana JDBC driver library found");
        }
	}

	/**
	 * @return the version string of the connected Hana database as retrieved at login
	 */
	public String getHanaversion() {
		return hanaversion;
	}

	/**
	 * @return the exact Hana user, e.g. the loginuser might by user1 but the actual database user name is "USER1"
	 */
	public String getHanaUser() {
		return hanauser;
	}

}
