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

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.rmi.RemoteException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

public class SymlinkTestCase {

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;

    private static final String ROOT = "/";
    private static final String COLL_NAME1 = "c1";
    private static final String COLL_NAME11 = "c11";
    private static final String COLL_NAME111 = "c111";
    private static final String COLL_NAME2 = "c2";
    private static final String COLL_NAME22 = "c22";
    private static final String COPY_COLL_NAME = "copyOfc22";
    private static final String SYMLINK1 = "s1";
    private static final String SYMLINK2 = "Root";
    private static final String COLL_DESC = "A test collection";
    private static int userId = ProductConstant.ADMIN_USER_ID;


    @DataProvider(name = "SymlinkDataProvider")
    public Object[][] sdp() {
        return new Object[][]{
                new Object[]{ROOT + SYMLINK2, ROOT + COLL_NAME2},
                new Object[]{
                        ROOT + COLL_NAME2 + File.separator + COLL_NAME22 +
                        File.separator + SYMLINK1,
                        ROOT + COLL_NAME1 + File.separator + COLL_NAME11}};
    }

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void initialize() throws LoginAuthenticationExceptionException, RemoteException,
                                    RegistryException {
        userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

    }

    /**
     * create symlinks for set of collections
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Create symlink")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testCreateSymlinks() throws Exception {
        createCollections();
        resourceAdminClient.addSymbolicLink(ROOT + COLL_NAME2 + File.separator + COLL_NAME22,
                                            SYMLINK1, ROOT + COLL_NAME1 + File.separator +
                                                      COLL_NAME11);
        resourceAdminClient.addSymbolicLink(ROOT, SYMLINK2, ROOT + COLL_NAME2);
        String authorUserName =
                resourceAdminClient.getResource(ROOT + SYMLINK2)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Symlink creation failure");
        authorUserName =
                resourceAdminClient.getResource(ROOT + COLL_NAME2 + File.separator +
                                                COLL_NAME22 + File.separator + SYMLINK1)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Symlink creation failure");
        restartServer();

        //reinitialize environment after server restart
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
    }

    /**
     * Check the created symlinks are not broken after restarting the server and
     * get the actual path of the symlinks
     *
     * @param symlink symlink location
     * @param poinsTo path to which the symlink points
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get the actual path of a symlink",
          dataProvider = "SymlinkDataProvider", dependsOnMethods = "testCreateSymlinks")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testgetActualPath(String symlink, String poinsTo) throws Exception {
        Property[] properties =
                propertiesAdminServiceClient.getProperty(symlink, "true")
                        .getProperties();
        String actualPath = "";
        for (Property property : properties) {
            if (property.getKey().equals("registry.actualpath")) {
                actualPath = property.getValue();
                break;
            }
        }
        Assert.assertTrue(actualPath.equals(poinsTo));

    }

    /**
     * Copy a collection which has symlinks
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     */
    @Test(groups = "wso2.greg", description = "Copy a collection which has symlinks",
          dependsOnMethods = "testCreateSymlinks")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testCopyCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.copyResource(ROOT + COLL_NAME2, ROOT + COLL_NAME2 + File.separator +
                                                            COLL_NAME22, ROOT, COPY_COLL_NAME);

        String authorUserName =
                resourceAdminClient.getResource(ROOT + COLL_NAME1)[0].getAuthorUserName();

        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Collection copying failure");
        assertTrue(resourceAdminClient.getResource(ROOT + COPY_COLL_NAME + File.separator +
                                                   SYMLINK1)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));

    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void clean() throws RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminClient.deleteResource(ROOT + SYMLINK2);
        resourceAdminClient.deleteResource(ROOT + COLL_NAME1);
        resourceAdminClient.deleteResource(ROOT + COLL_NAME2);
        resourceAdminClient.deleteResource(ROOT + COPY_COLL_NAME);
        environment = null;
        userInfo = null;
        resourceAdminClient = null;
        propertiesAdminServiceClient = null;
    }

    /**
     * create set of collections
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    private void createCollections()
            throws RemoteException, ResourceAdminServiceExceptionException {
        String fileType = "other";
        resourceAdminClient.addCollection(ROOT, COLL_NAME1, fileType, COLL_DESC);
        resourceAdminClient.addCollection(ROOT + COLL_NAME1 + File.separator, COLL_NAME11,
                                          fileType, COLL_DESC);
        resourceAdminClient.addCollection(ROOT + COLL_NAME1 + File.separator + COLL_NAME11 +
                                          File.separator, COLL_NAME111, fileType, COLL_DESC);
        resourceAdminClient.addCollection(ROOT, COLL_NAME2, fileType, COLL_DESC);
        resourceAdminClient.addCollection(ROOT + COLL_NAME2 + File.separator, COLL_NAME22,
                                          fileType, COLL_DESC);
    }


    /**
     * restart server
     *
     * @throws Exception
     */
    private void restartServer() throws Exception {
        ServerAdminClient serverAdminClient =
                new ServerAdminClient(
                        environment.getGreg().getProductVariables().getBackendUrl(),
                        environment.getGreg().getSessionCookie());
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);

    }

}
