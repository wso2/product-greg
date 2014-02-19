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
package org.wso2.carbon.registry.jira2.issues.test2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.subscription.test.util.ManagementConsoleSubscription;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class Registry1170 {
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;

    private static final String SERVICE_PATH =
            "/_system/governance/trunk/services/com/abb/IntergalacticService";

    @BeforeClass()
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        int userID = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
    }

    /**
     * creating a service
     *
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException {

        String servicePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService.metadata.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);

        ResourceData[] resourceDataArray = resourceAdminServiceClient.
                getResource(SERVICE_PATH);

        assertNotNull(resourceDataArray, "Service not found");
    }

    /**
     * Create a subscription for a service and send notifications via Management
     * Console
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dependsOnMethods = "testCreateService")
    public void testConsoleSubscription() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(SERVICE_PATH, "ResourceUpdated", environment,
                                                      userInfo));
    }

    /**
     * delete a service and related wsdl and schema
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "delete service",
          dependsOnMethods = "testConsoleSubscription")
    public void testDeleteService() throws Exception {
        resourceAdminServiceClient.deleteResource(SERVICE_PATH);
    }

    /**
     * check subscription is deleted once the service is deleted
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "get subscriptions of a deleted service",
          dependsOnMethods = "testDeleteService")
    public void testCheckSubscription() throws Exception {
        SubscriptionBean bean =
                infoServiceAdminClient.getSubscriptions(SERVICE_PATH,
                                                        environment.getGreg()
                                                                .getSessionCookie());
        Assert.assertNull(bean.getSubscriptionInstances());
    }

    @AfterClass
    public void testCleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk");
        environment = null;
        userInfo = null;
        resourceAdminServiceClient = null;
        infoServiceAdminClient = null;
    }
}
