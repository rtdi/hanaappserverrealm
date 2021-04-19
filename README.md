# SAP Hana Application Server Realm
_The security foundation for the SAP Hana Application Server_

A tomcat realm allows to configure various authentication methods and groups. 
This HanaRealm is using the SAP Hana database to authenticate, meaning the user is requested in the browser to enter his Hana login and password
and if valid, the list of assigned Hana groups is read. Thus tomcat can utilize these group names in the standard way to implement role level
security.

## Installation

Copy the [jar](https://github.com/rtdi/hanaappserverrealm/releases) file into tomcat's lib folder so that it can be used in the server configuration.

In the server.xml of the tomcat the realm is configured, with the parameter hanaJDBCURL as the only property to be specified.

		<Server>
		  <Service>
		    <Engine>
		      ...
		      <Realm className="org.apache.catalina.realm.LockOutRealm">
		        <Realm className="io.rtdi.appcontainer.hanarealm.HanaRealm" hanaJDBCURL="jdbc:sap://hanartdi:39015/HXE"/>
		      </Realm>
		      ...
		    </Engine>
		  </Service>
		</Server>

Alternatively, the environment variable HANAJDBCURL can be set as well. This is especially useful when building docker images. 