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

package org.wso2.carbon.registry.metadata.test.policy;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Add Policy from File System and URL Functionality Tests
 */

public class PolicyAddTestCase extends GREGIntegrationBaseTest{

    private static final Log log = LogFactory.getLog(PolicyAddTestCase.class);
    private String policyPath = "/_system/governance/trunk/policies/";
    private ResourceAdminServiceClient resourceAdminServiceClient;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        String session = new LoginLogoutClient(automationContext).login();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl()
                        ,session);
    }


    /**
     * Add policy file from the file system
     */
    @Test(groups = {"wso2.greg"})
    public void addPolicyFromFile() throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourceName = "policy.xml";
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator
                + "policy" + File.separator + resourceName;
        resourceAdminServiceClient.addResource(policyPath + "1.0.0/" + resourceName,
                "application/policy+xml", "testPolicy", new DataHandler(new URL("file:///" + resource)));
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(policyPath + "1.0.0/" + resourceName));
    }

    /**
     * Add policy file from import URL functionality
     */
    @Test(groups = {"wso2.greg"})
    public void addPolicyFromURL() throws ResourceAdminServiceExceptionException, RemoteException {
        String resourceUrl = "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/policies/policy.xml";
        String resourceName = "RMpolicy3.xml";
        resourceAdminServiceClient.addPolicy(resourceName, "adding From URL", resourceUrl);
        Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(policyPath + "1.0.0/" + resourceName));
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "addPolicyFromFile")
    public void updatePolicyFromFile() throws RegistryException, IOException, ResourceAdminServiceExceptionException {
        String resContent = "<?xml version=\"1.0\"?>\n" +
                "<wsp:Policy\n" +
                "        xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                "    <wsrmp:RMAssertion\n" +
                "            xmlns:wsrmp=\"http://docs.oasis-open.org/ws-rx/wsrmp/200702\">\n" +
                "        <wsrmp:DeliveryAssurance>\n" +
                "            <wsp:Policy>\n" +
                "                <wsrmp:ExactlyOnce/>\n" +
                "            </wsp:Policy>\n" +
                "        </wsrmp:DeliveryAssurance>\n" +
                "    </wsrmp:RMAssertion>\n" +
                "</wsp:Policy>"; //to update
        resourceAdminServiceClient.updateTextContent(policyPath + "1.0.0/" + "policy.xml", resContent);
        Assert.assertEquals(resContent, resourceAdminServiceClient.getTextContent(policyPath + "1.0.0/" + "policy.xml"));
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "addPolicyFromURL")
    public void updatePolicyFromURL() throws RegistryException, IOException, ResourceAdminServiceExceptionException {
        String resContent = "<?xml version=\"1.0\"?>\n" +
                "\n" +
                "<wsp:Policy \n" +
                "  xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                "  <wsrmp:RMAssertion \n" +
                "    xmlns:wsrmp=\"http://docs.oasis-open.org/ws-rx/wsrmp/200702\"> \n" +
                "    <wsrmp:DeliveryAssurance> \n" +
                "      <wsp:Policy> \n" +
                "        <wsrmp:ExactlyOnce /> \n" +
                "      </wsp:Policy> \n" +
                "    </wsrmp:DeliveryAssurance> \n" +
                "  </wsrmp:RMAssertion> \n" +
                "</wsp:Policy>";
        resourceAdminServiceClient.updateTextContent(policyPath + "1.0.0/" + "RMpolicy3.xml", resContent);
        Assert.assertEquals(resContent, resourceAdminServiceClient.getTextContent(policyPath + "1.0.0/" + "RMpolicy3.xml"));
    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource(policyPath + "1.0.0/" + "policy.xml");
        resourceAdminServiceClient.deleteResource(policyPath + "1.0.0/" + "RMpolicy3.xml");
        resourceAdminServiceClient = null;
        policyPath = null;
    }
}
