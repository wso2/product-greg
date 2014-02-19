/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.tck.cmis;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * A test class to run the CMIS TCK as a TestNG module
 */

public class CMISTck {
   
    public static final String CMIS_PATH = "cmis.jar.path";
    public static final String LOG_PATH = "logging.jar.path";

    public RemoteRegistry registry;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private UserInfo userInfo;

    private String cmisJarPath = "";
    private String loggingJarPath = "";

    @BeforeClass(groups = {"wso2.greg"})
    public void init()
            throws MalformedURLException, RegistryException, LoginAuthenticationExceptionException,
                   RemoteException {
        int userId = 1;
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        userInfo = UserListCsvReader.getUserInfo(userId);
        builder = new EnvironmentBuilder().greg(1);
        environment = builder.build();

        registry = new RegistryProviderUtil().getRemoteRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test(groups = {"wso2.greg"})
    public void runTckTest() throws RegistryException, InterruptedException, IOException, Exception {
	
	PrintStream ps = new PrintStream(new FileOutputStream("system.properties"));
	String fs = File.separator;
	String localRepo =  System.getProperty("local.repo");
		
	loggingJarPath = localRepo + fs + "commons-logging" + fs + "commons-logging" + fs + "1.1.1" + fs;
	cmisJarPath =  localRepo + fs + "org" + fs + "apache" + fs + "chemistry" + fs + "opencmis" + fs;  

	ps.println(CMIS_PATH + "=" + cmisJarPath);
	ps.println(LOG_PATH + "=" + loggingJarPath);
	
 	Process p = Runtime.getRuntime().exec("ant cmis");
        int stat = p.waitFor();
	
	if (stat == 0) {
	   System.out.println("----------------- CMIS TCK ran successfully, see the tck-results (default) for the results -------------------");	
	} else {
	   throw new Exception ("------------------ Failed to run CMIS TCK --------------------");	
	}
    }
}
