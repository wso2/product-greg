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

import java.rmi.RemoteException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class Registry595 {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WSRegistryServiceClient registry;

    private static final String COLLECTION_NAME = "/wsTestCollection";
    private static final String RESOURCE_NAME = "/wsTestResource";

    @BeforeClass
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        int userID = ProductConstant.ADMIN_USER_ID;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder;
        builder = new EnvironmentBuilder().greg(userID);
        ManageEnvironment environment = builder.build();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userID, ProductConstant.GREG_SERVER_NAME);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
    }

    /**
     * create a collection, add a resource to the created collection and get the
     * child count using ws client
     *
     * @throws Exception error when adding collection children
     */
    @Test(groups = "wso2.greg", description = "get child count of a collection using ws client")
    public void testWSClientChildCount() throws Exception {
        Collection testCollection = registry.newCollection();
        registry.put(COLLECTION_NAME, testCollection);

        Resource testResource = registry.newResource();
        testResource.setContent("This is a test resource");
        registry.put(COLLECTION_NAME + RESOURCE_NAME, testResource);

        Collection collection = (Collection) registry.get(COLLECTION_NAME);
        Assert.assertTrue(collection.getChildCount() == 1);

    }

    @AfterClass()
    public void clean() throws RemoteException, ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.deleteResource(COLLECTION_NAME + RESOURCE_NAME);
        resourceAdminServiceClient.deleteResource(COLLECTION_NAME);
        registry = null;
        resourceAdminServiceClient = null;
    }
}
