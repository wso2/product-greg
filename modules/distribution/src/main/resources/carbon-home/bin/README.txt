${product.name} v${product.version}


This file explains the usages of all the scipts contained within
this directory.

1. chpasswd.sh & chpasswd.bat
    - Utiliy for changing the passwords of users registered in the CARBON user
      database

	This script is designed to be used with any databases. Tested with H2,MSSQL and Derby. H2 database is embedded with CARBON. 
	Open a console/shell and run the script from "home/wso2/bin" directory.

	Usage:
	# chpasswd.bat/sh --db-url jdbc:h2:/home/wso2/repository/database/WSO2CARBON_DB

	If administrator wants to use other databases, he should configure the "registry.xml" and "user-mgt.xml", which are in "/home/wso2/repository/conf" directory and 
	he needs to keep the drivers of the database inside the "home/wso2/lib" directory or in the "home/wso2/repository/lib" directory.

	For eg,
	If user uses MSSQL as his DB,
	a. Put the MSSQL driver inside the "/home/wso2/repository/lib" directory.
	b. Edit the registry.xml file with  your database's  url,username,password and drivername details. 

	eg.
        <dbConfig name="wso2registry">
                <url>jdbc:jtds:sqlserver://10.100.1.68:1433/USERDB</url>
                <userName>USER</userName>
                <password>USER</password>        
                <driverName>net.sourceforge.jtds.jdbc.Driver</driverName>
                <maxActive>80</maxActive>
                <maxWait>60000</maxWait>
                <minIdle>5</minIdle>
         </dbConfig>

	c. Edit the "user-mgt.xml" file with your database's  url,username,password and drivername details. 

	eg.
        <Configuration>
            <Property name="url">jdbc:jtds:sqlserver://10.100.1.68:1433/USERDB</Property>
            <Property name="userName">USER</Property>
            <Property name="password">USER</Property>
            <Property name="driverName">net.sourceforge.jtds.jdbc.Driver</Property>
            <Property name="maxActive">30</Property>
            <Property name="maxWait">60000</Property>
            <Property name="minIdle">5</Property>
        </Configuration>

	d. Open a console/shell and run the script from "home/wso2/bin" directory having shutdown the server.
	
	Usage:
	# chpasswd.bat/sh --db-url jdbc:jtds:sqlserver://10.100.1.68:1433/USERDB --db-driver net.sourceforge.jtds.jdbc.Driver --db-username USER --db-password USER

	e. Now you can access the admin console with your new password.

	NOTE:- To create your own database, you need to put the driver into the "home/wso2/repository/components/extensions" directory.
        Then, start the server with "-Dsetup" option. If you have changed the webapp context of carbon from ROOT to foo, you will have 
        to use -Dsetup=foo instead.

	Usage:
	# wso2server.bat/sh -Dsetup
	It will create the tables. Thereafter shutdown the server and open a console/shell and run the script as enumerated above.
	You may delete the driver, which is inside the "home/wso2/repository/components/extensions" directory, as it is no more required.
 

2. daemon.sh
    - Runs CARBON within the Java Service Wrapper.
      Suitable for starting and stopping wrapped Java applications on
      UNIX platforms. See http://wrapper.tanukisoftware.org

      By placing the symbolic link in /etc/init.d, you could run CARBON
      as a daemon as follows:
                                 
      Usage:
      /etc/init.d/wso2server {console | start | stop | restart | status | dump}


3. README.txt
    - This file

4. repowriter.sh & repowriter.bat
    - Tool for writing the Axis2 repository metadata into the Registry

5. version.txt
    - A simple text file used for storing the version

6. wso2server.sh & wso2server.bat
    - The main script file used for running the server.

    Usage: wso2server.sh [commands] [system-properties]

            commands:
                --debug <port>  Start the server in remote debugging mode.
                                port: The remote debugging port.

                --run           Run the server.
                --start         Start Carbon as a Unix daemon in the background.
                --console       Start Carbon as a Unix daemon in the foreground.
                --stop          Stop the Carbon Unix daemon.
                --status        Get the status of the Carbon Unix daemon.
                --dump          Print a thread dump of the Carbon Unix daemon.
                --restart       Restart the Carbon Unix daemon.
                --cleanRegistry Clean registry space.
                                [CAUTION] All Registry data will be lost..
                --version       The version of the product you are running.

            system-properties:
                -DhttpPort      Overrides the HTTP port defined in the
                                transports.xml file.
                -DhttpsPort     Overrides the HTTPS port defined in the
                                transports.xml file.

                -DosgiConsole=[port]
                                Start Carbon with Equinox OSGi console.
                                If the optional 'port' parameter is provided, a
                                telnet port will be opened.

                -DosgiDebugOptions=[options-file]
                                Start Carbon with OSGi debugging enabled.
                                If the optional 'options-file' is provided, the OSGi
                                debug options will be loaded from it.

                -Dsetup         Clean the Registry and other configuration,
                                recreate DB, re-populate the configuration,
                                and start Carbon.

                                Note: If you have changed the webapp context of
                                carbon from ROOT to foo, you will have to use
                                -Dsetup=foo instead.

                -Dcarbon.registry.root
                                The root of the Registry used by
                                this Carbon instance.

                -Dwso2.transports.xml
                                Location of the mgt-transports.xml file.

                -Dweb.location  The directory into which the UI artifacts
                                included in an Axis2 AAR file are extracted to.
		-Dcarbon.config.dir.path=[path]
				Overwrite the conf directory path where we keep all 
				configuration files like carbon.xml, axis2.xml etc.
		-Dcarbon.logs.path=[path]
				Define the path to keep Log files.
		-Daxis2.repository=[path]
				Overwrite the default location we keep axis2 client/service
				artifacts.
		-Dcomponents.repository=[path]
				Overwrite the default location we keep all the OSGi bundles. 

7. wsdl2java.sh & wsdl2java.bat - Tool for generating Java code from WSDLs

8. install.bat                  - Install/Uninstall Carbon Windows NT service

9. java2wsdl.sh & java2wsdl.bat - Tool for generating WSDL from Java code

10. ciphertool.sh & ciphertool.bat - Tool for encrypting and decrypting simple texts such as passwords.
    The arguments that are inputs to this tool with their meanings are shown bellow.

	keystore        - If keys are in a store , it's location
	storepass       - Password for access keyStore
	keypass         - To get private key
	alias           - Alias to identify key owner
	storetype       - Type of keyStore , Default is JKS
	keyfile         - If key is in a file
	opmode          - encrypt or decrypt , Default is encrypt
	algorithm       - encrypt or decrypt algorithm , Default is RSA
	source          - Either cipher or plain text as an in-lined form
	outencode       - Currently base64 and use for encode result
	inencode        - Currently base64 and use to decode input
	trusted         - Is KeyStore a trusted store? If presents this, consider as a trusted store
	passphrase      - if a simple symmetric encryption using a pass phrase shall be used

11. build.xml - Build configuration for the ant command. Running the ant command in this directory, will copy the libraries that are required 
    to run remote registry clients in to the repository/lib directory.

12. checkin-client.sh & checkin-client.bat - Tool to checkin and checkout resources between the registry and the file-system.
