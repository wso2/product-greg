/*
*Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.properties.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.rmi.RemoteException;

public class PropertiesAdminServiceTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(PropertiesAdminServiceTestCase.class);
    protected PropertiesAdminServiceClient propertiesAdminServiceClient;
    protected Registry governance;
    protected WSRegistryServiceClient registry;
    protected LogViewerClient logViewerClient;

    @BeforeClass(groups = {"wso2.greg.prop.admin.service"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        logViewerClient = new LogViewerClient(backendURL, sessionCookie);
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(backendURL, sessionCookie);
        log.debug("Running SuccessCase");
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = RemoteException.class)
    public void addHiddenPropertiesToResourceThrowException() throws Exception {
        propertiesAdminServiceClient.setProperty("/", "registry.somehidden.property_name",
                "somehidden.property_value");
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = RemoteException.class)
    public void updateHiddenPropertiesToResourceThrowException() throws Exception {
        propertiesAdminServiceClient.updateProperty("/", "registry.somehidden.property_name",
                "somehidden.property_value", "some.old.prop.name");
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = RemoteException.class)
    public void addDuplicatePropertiesToResourceThrowException() throws Exception {
        propertiesAdminServiceClient.setProperty("/", "add_property_name", "some.property_value");
        //trying to add an existing property name,thus, this should fail.
        propertiesAdminServiceClient.setProperty("/", "add_property_name", "some.property_value1");
    }

    @Test(groups = {"wso2.greg.prop.admin.service"}, expectedExceptions = RemoteException.class)
    public void updateDuplicatePropertiesToResourceThrowException() throws Exception{
        propertiesAdminServiceClient.setProperty("/", "update_property_name_1", "some.property_value1");
        propertiesAdminServiceClient.setProperty("/", "update_property_name_2", "some.property_value2");
        //trying to rename existing property(update_property_name_1) to another existing property(update_property_name_2)
        // and thus, this should fail.
        propertiesAdminServiceClient.updateProperty("/", "update_property_name_2", "some.property_value",
                "update_property_name_1");
    }

    @AfterClass(alwaysRun = true)
    public void removeProperties() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        PropertiesBean propertiesBean = propertiesAdminServiceClient.getProperties("/", "yes");
        if(propertiesBean.getProperties() != null)
            for(Property prop : propertiesBean.getProperties()) {
                propertiesAdminServiceClient.removeProperty("/", prop.getKey());
            }
    }
}
