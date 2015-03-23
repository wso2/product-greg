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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.ManagementConsoleSubscription;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class Registry1170TestCase extends GREGIntegrationBaseTest {
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private String session;

    private static final String SERVICE_PATH =
            "/_system/governance/trunk/services/com/abb/1.0.0-SNAPSHOT/IntergalacticService";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        session = getSessionCookie();

        infoServiceAdminClient =
                new InfoServiceAdminClient(backendURL, session);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, session);
    }


    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
            AddServicesServiceRegistryExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException, InterruptedException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService.metadata.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);
        Thread.sleep(30000);
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
        assertTrue(ManagementConsoleSubscription.init(SERVICE_PATH, "ResourceUpdated", automationContext));
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
                infoServiceAdminClient.getSubscriptions(SERVICE_PATH, session);
        Assert.assertNull(bean.getSubscriptionInstances());
    }
}
