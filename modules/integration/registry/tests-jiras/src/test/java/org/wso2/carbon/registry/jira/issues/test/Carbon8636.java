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

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

public class Carbon8636 {

    private Registry governance;
    int userId = 2;
    private ServiceManager serviceManager;
    private Service serviceDescriptionsWithEnterKeys;
    private Service serviceDescriptionWithOutEnterKeys;


    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void init()
            throws RegistryException, RemoteException, LoginAuthenticationExceptionException {
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
    }

    @Test(groups = {"wso2.greg"}, description = "description with having enter keys")
    public void descriptionWithEnterKeysTestCase() throws GovernanceException {
        serviceDescriptionsWithEnterKeys = serviceManager.newService(new QName("http://service.with.EnterKeys/mnm/",
                                                                               "serviceDescriptionsWithEnterKeys"));
        serviceDescriptionsWithEnterKeys.addAttribute("description", "This is Description&#xd;");
        serviceManager.addService(serviceDescriptionsWithEnterKeys);
        Assert.assertNotNull(serviceManager.getService(serviceDescriptionsWithEnterKeys.getId()));


    }

    @Test(groups = {"wso2.greg"}, description = "description with out having enter keys")
    public void descriptionWithoutEnterKeysTestCase() throws GovernanceException {
        serviceDescriptionWithOutEnterKeys = serviceManager.newService(new QName("http://service.with.EnteroutKeys/mnm/",
                                                                                 "serviceDescriptionsWithOutEnterKeys"));
        serviceDescriptionWithOutEnterKeys.addAttribute("description", "This is Description");
        serviceManager.addService(serviceDescriptionWithOutEnterKeys);
        Assert.assertNotNull(serviceManager.getService(serviceDescriptionWithOutEnterKeys.getId()));


    }

    @AfterClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void deleteServices() throws GovernanceException {
        serviceManager.removeService(serviceDescriptionsWithEnterKeys.getId());
        serviceManager.removeService(serviceDescriptionWithOutEnterKeys.getId());
        governance = null;
        serviceManager = null;
        serviceDescriptionsWithEnterKeys = null;
        serviceDescriptionWithOutEnterKeys = null;
    }
}
