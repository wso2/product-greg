/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.governance.api.test.old;


import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;

public class ServiceAndEndpointTestCase {
    String service_namespace = "http://test.endpoint.com";
    String service_name = "EndpointTestService";
    String wsdlUrl = "http://people.wso2.com/~evanthika/wsdls/SimpleStockQuoteService.wsdl";
    public static ServiceManager serviceManager;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        int userId = 1;
        WSRegistryServiceClient wsRegistry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
    }

    @Test(groups = {"wso2.greg"}, description = "add service with wsdl and check endpoints")
    public void testEndpoint() throws GovernanceException {
        Service service = serviceManager.newService(new QName(service_namespace, service_name));
        service.addAttribute("interface_wsdlURL", wsdlUrl);
        serviceManager.addService(service);
        Service service1 = serviceManager.getService(service.getId());

        String[] endpoints = service1.getAttributes("endpoints_entry");
        Assert.assertTrue(endpoints.length > 0, "Endpoints doesn't exist");
        //remove wsdl url
        service1.setAttribute("interface_wsdlURL", "");
        serviceManager.updateService(service1);
        Service updatedService = serviceManager.getService(service1.getId());
        String[] updatedEndpoints = updatedService.getAttributes("endpoints_entry");
        Assert.assertTrue(updatedEndpoints.length == 0, "Endpoints doest removed added via the wsdl");


    }
    @AfterClass
    public void cleanArtifacts() throws RegistryException {
        serviceManager=null;
    }

}
