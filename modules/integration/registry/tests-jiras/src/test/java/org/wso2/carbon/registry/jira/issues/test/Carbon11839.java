/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.jira.issues.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axiom.util.base64.Base64Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import static org.testng.Assert.assertNotNull;

public class Carbon11839 {

    private ManageEnvironment environment;
    private final int userID = ProductConstant.ADMIN_USER_ID;
    private UserInfo userInfo;
    private EnvironmentBuilder builder;

    private static final String IMPACT_ANALYSIS_PATH =
            "resource/_system/config/repository/dashboards/gadgets/impact-analysis.xml";
    private static final String LIFE_CYCLE_PATH =
            "resource/_system/config/repository/dashboards/gadgets/life-cycle-info.xml";
    private static final String IMPACT_PATH = "/carbon/impactAnalysis/impact.xml";

    @DataProvider(name = "pathDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{IMPACT_ANALYSIS_PATH},
                              new Object[]{LIFE_CYCLE_PATH}};
    }

    @BeforeClass
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();

    }

    /**
     * Access a resource via http client
     *
     * @param path path of the resource
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Access a resource", dataProvider = "pathDataProvider")
    public void testAccessResource(String path) throws Exception {

        FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        String registryUrl;
        if (builder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            registryUrl = ProductUrlGeneratorUtil.getRemoteRegistryURLOfStratos(properties.getProductVariables().
                    getHttpsPort(), properties.getProductVariables().getHostName(), properties, userInfo);
        } else {
            registryUrl = ProductUrlGeneratorUtil.getRemoteRegistryURLOfProducts(properties.getProductVariables().
                    getHttpsPort(), properties.getProductVariables().getHostName(), properties.getProductVariables().getWebContextRoot());
        }


        URL url = new URL(registryUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        String userPassword = userInfo.getUserName() + ":" + userInfo.getPassword();
        String encodedAuthorization = Base64Utils.encode(userPassword.getBytes());
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        connection = waitForConnection(connection);
        assertNotNull(connection, "connection is not established to gadget xml");
        InputStream inputStream = connection.getInputStream();
        Assert.assertNotNull(inputStream);
    }

    /**
     * Get content of a resource via http client
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get content of a resource", dependsOnMethods = "testAccessResource")
    public void testVerifyResource() throws Exception {

        URL url =
                new URL("http://" + environment.getGreg().getProductVariables().getHostName() +
                        ":" + environment.getGreg().getProductVariables().getHttpPort() +
                        IMPACT_PATH);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        String userPassword = userInfo.getUserName() + ":" + userInfo.getPassword();
        String encodedAuthorization = Base64Utils.encode(userPassword.getBytes());
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        // force wait otherwise connection.connect returns NPE
        connection = waitForConnection(connection);
        assertNotNull(connection, "connection is not established to gadget xml");
        InputStream inputStream = connection.getInputStream();
        Assert.assertNotNull(inputStream);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        environment = null;
        userInfo = null;
    }

    private HttpURLConnection waitForConnection(HttpURLConnection connection) {
        long timeOut = 30000;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeOut) {
            try {
                Thread.sleep(2000);
                connection.connect();
                return connection;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
