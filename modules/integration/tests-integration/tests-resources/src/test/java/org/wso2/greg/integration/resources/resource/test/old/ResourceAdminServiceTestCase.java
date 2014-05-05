/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.greg.integration.resources.resource.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry resource admin service operation
 */

public class ResourceAdminServiceTestCase extends GREGIntegrationBaseTest{

    private static final Log log = LogFactory.getLog(ResourceAdminServiceTestCase.class);
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {

       super.init(TestUserMode.SUPER_TENANT_ADMIN);
        log.debug("Running SuccessCase");


        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                                               automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

        String collectionPath = resourceAdminServiceClient.addCollection("/", "Test", "", "");
        String authorUserName =
                resourceAdminServiceClient.getResource("/Test")[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName),
                   "/Test creation failure");
        log.debug("collection added to " + collectionPath);
        // resourceAdminServiceStub.addResource("/Test/echo_back.xslt", "application/xml", "xslt files", null,null);

        String content = "Hello world";
        resourceAdminServiceClient.addTextResource("/Test", "Hello", "text/plain", "sample",
                                                   content);
        String textContent = resourceAdminServiceClient.getTextContent("/Test/Hello");

        assertTrue(content.equalsIgnoreCase(textContent), "Text content does not match");

    }

    @AfterClass
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/Test");
        resourceAdminServiceClient=null;
    }
}
