package io.rtdi.appcontainer.hanarealm;

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

import io.rtdi.appcontainer.realm.IAppContainerPrincipal;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the Hana database version.
 *
 */
public class HanaPrincipal extends GenericPrincipal implements IAppContainerPrincipal {

	private static final long serialVersionUID = 4658263939892656292L;
	private String hanajdbcurl;
	private String hanaversion;
	private String hanauser;
	private String password;

	public HanaPrincipal(String name, String password, String hanajdbcurl) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl));
		this.hanajdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	public HanaPrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal);
		this.hanajdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	public HanaPrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal,
			LoginContext loginContext) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal, loginContext);
		this.hanajdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	public HanaPrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal,
			LoginContext loginContext, GSSCredential gssCredential) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal, loginContext, gssCredential);
		this.hanajdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	private void setSupportData(String name, String password, String hanajdbcurl) throws SQLException {
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
	 * @throws SQLException in case the roles cannot be read
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
	@Override
	public String getJDBCURL() {
		return hanajdbcurl;
	}
	
	@Override
	public Connection createNewConnection() throws SQLException {
		return getDatabaseConnection(super.getName(), password, hanajdbcurl);
	}
	
	static Connection getDatabaseConnection(String user, String passwd, String jdbcurl) throws SQLException {
        try {
            Class.forName("com.sap.db.jdbc.Driver");
            return DriverManager.getConnection(jdbcurl, user, passwd);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No Hana JDBC driver library found");
        }
	}
	
	@Override
	public String getDriverURL() {
		return "com.sap.db.jdbc.Driver";
	}


	/**
	 * @return the version string of the connected Hana database as retrieved at login
	 */
	@Override
	public String getDBVersion() {
		return hanaversion;
	}

	/**
	 * @return the exact Hana user, e.g. the loginuser might by user1 but the actual database user name is "USER1"
	 */
	@Override
	public String getDBUser() {
		return hanauser;
	}

	@Override
	public String getPassword() {
		return password;
	}

}
