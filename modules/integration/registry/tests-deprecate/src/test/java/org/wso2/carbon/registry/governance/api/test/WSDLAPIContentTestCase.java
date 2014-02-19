/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.governance.api.test;


import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.governance.api.test.util.FileManagerUtil;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class WSDLAPIContentTestCase {
    public static WsdlManager wsdlManager;
    public static EndpointManager endpointManager;
    public static SchemaManager schemaManager;
    private Registry governance;
    private String resourcePath;


    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        governance = TestUtils.getRegistry();
        wsdlManager = new WsdlManager(governance);
        endpointManager = new EndpointManager(governance);
        schemaManager = new SchemaManager(governance);

        resourcePath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." +
                File.separator + ".." + File.separator + "src" + File.separator + "test" + File.separator +
                "java" + File.separator + "resources";
    }
    @Test(groups = {"wso2.greg.api"}, description = "Testing AddWSDL with Inline content")
    public void testAddWSDLContentWithName() throws GovernanceException, IOException {
        try {
            //Starting add new WSDL file therefore clean again.
            TestUtils.cleanupResources(governance);

            String wsdlFileLocation = resourcePath + File.separator + "wsdl" + File.separator + "Automated.wsdl";
            Wsdl wsdl = wsdlManager.newWsdl(FileManagerUtil.readFile(wsdlFileLocation).getBytes(), "AutomatedSample.wsdl");
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            Assert.assertTrue(false, "Failed to add Automated.wsdl as a content");
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method " +
                    "which have Inline wsdl content and wsdl Name" + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }
    @Test(groups = {"wso2.greg.api"}, description = "Testing AddWSDL with Inline content",
            dependsOnMethods = "testAddWSDLContentWithName")
    public void testAddWSDLContentWithoutName() throws RegistryException, IOException {

        //Starting add new WSDL file therefore clean again.
        TestUtils.cleanupResources(governance);

        String wsdlFileLocation = resourcePath + File.separator + "wsdl" + File.separator + "Automated.wsdl";
        try {
            boolean isWSDLFound = false;
            Wsdl wsdl = wsdlManager.newWsdl(FileManagerUtil.readFile(wsdlFileLocation).getBytes());
            wsdlManager.addWsdl(wsdl);
            Wsdl[] wsdlArray = wsdlManager.getAllWsdls();
            for (Wsdl w : wsdlArray) {
                if (w.getQName().getNamespaceURI().equalsIgnoreCase("http://www.strikeiron.com")) {
                    isWSDLFound = true;
                }
            }
            assertTrue(isWSDLFound, "WsdlManager:newWsdl method doesn't not execute with inline wsdl content");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method " +
                    "which have Inline wsdl content" + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

}
