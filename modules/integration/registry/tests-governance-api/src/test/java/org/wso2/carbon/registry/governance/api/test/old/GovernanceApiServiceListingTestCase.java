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

package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Covers the public JIRA https://wso2.org/jira/browse/CARBON-12871 ,
 * https://wso2.org/jira/browse/REGISTRY-723
 */


public class GovernanceApiServiceListingTestCase {
    private static final Log log = LogFactory.getLog(GovernanceApiServiceListingTestCase.class);
    private ServiceManager serviceManager;
    private Registry governanceRegistry;


    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"})
    public void addService() throws InterruptedException, RemoteException,
            MalformedURLException, RegistryException {
        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governanceRegistry);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"})
    public void testAddServices() throws GovernanceException {
        for (int serviceNo = 0; serviceNo < 9; serviceNo++) {
            Service service2 = serviceManager.newService(new QName("http://my.service.ns" + serviceNo,
                    "MyService" + serviceNo));
            serviceManager.addService(service2);
            log.info("Added service" + service2.getPath());
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"}, dependsOnMethods = "testAddServices")
    public void testListServices() throws Exception {
        Service[] serviceList = serviceManager.getAllServices();
        for (int index = 0; index < serviceList.length; index++) {
            String serviceName = serviceList[index].getQName().getLocalPart();
            String number = serviceName.replace("MyService", "");

            if (!(index == Integer.parseInt(number))) {
                Assert.fail("Service list is not sorted");
            }
        }
    }


//    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"},dependsOnMethods = "testListServices")
//    public void testCheckVersioning() throws Exception {
//        try {
//            Service versionService1 = serviceManager.newService(new QName("http://wso2.com", "versionService"));
//            serviceManager.addService(versionService1);
//            versionService1.addAttribute("Owner", "Financial Department");
//            serviceManager.updateService(versionService1);
//            serviceManager.getService(versionService1.getId()).createVersion();
//        } catch (Exception e) {
//            String msg = "Failed to create version of service";
//            Assert.assertFalse(true, msg);
//            throw new Exception(msg, e);
//        }
//    }


    @AfterClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts() throws RegistryException {
        for (Service serviceList : serviceManager.getAllServices()) {
            if (serviceList.getQName().toString().contains("MyService")) {
                serviceManager.removeService(serviceList.getId());
            }
        }

        governanceRegistry = null;
        serviceManager = null;
    }


}
