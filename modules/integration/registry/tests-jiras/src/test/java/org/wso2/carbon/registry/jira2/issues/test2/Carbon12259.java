/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.registry.jira2.issues.test2;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class Carbon12259 {
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private InfoServiceAdminClient infoServiceAdminClient;
    private UserManagementClient userManagementClient;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws Exception {

        int userId = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                                            environment.getGreg().getSessionCookie());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());

        testAddRole();

    }


    @Test(groups = "wso2.greg", description = "Add a service including spaces to its name")
    public void testAddNameWithSpaces() throws IOException, XMLStreamException,
                                               ResourceAdminServiceExceptionException,
                                               RegistryException, RegistryExceptionException {
        SubscriptionInstance[] sb1 = null;

        boolean status = false;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "services" + File.separator + "carbon12259.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);

        String servicePath = "/_system/governance/trunk/services/com/a b c/a b c";

        ResourceData[] resourceDataArray = resourceAdminServiceClient.getResource(servicePath);
        for (ResourceData resourceData : resourceDataArray) {
            if (resourceData.getName().equals("a b c")) {
                status = true;
            }
        }
        assertTrue(status);

        SubscriptionBean bean = testMgtConsoleResourceSubscription(servicePath, "ResourceUpdated");
        Assert.assertTrue(bean.getSubscriptionInstances() != null);

        sb1 = infoServiceAdminClient.getSubscriptions(servicePath, environment.getGreg().getSessionCookie()).
                getSubscriptionInstances();
        assertEquals(userInfo.getUserNameWithoutDomain(), sb1[0].getOwner());

    }


    public SubscriptionBean testMgtConsoleResourceSubscription(String path, String updateType)
            throws RegistryException, RemoteException {
        return infoServiceAdminClient.subscribe(path, "work://SubscriptionTestRole",
                                                updateType, environment.getGreg().getSessionCookie());
    }

    public void testAddRole() throws Exception {

        userManagementClient.addRole("SubscriptionTestRole", new String[]{userInfo.getUserNameWithoutDomain()}, new String[]{""});
        Assert.assertTrue(userManagementClient.roleNameExists("SubscriptionTestRole"));

    }


    @AfterClass
    public void cleanUp() throws Exception {
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk");
        userManagementClient.deleteRole("RoleSubscriptionTest");

        resourceAdminServiceClient = null;
        environment = null;
        userInfo = null;
        infoServiceAdminClient = null;
        userManagementClient = null;
    }

}
