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

package org.wso2.carbon.registry.metadata.test.policy.old;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Add Policy from File System and URL Functionality Tests
 */

public class PolicyAddTestCase {

    private static final Log log = LogFactory.getLog(PolicyAddTestCase.class);
    private String policyPath = "/_system/governance/trunk/policies/";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add/Update Policy Registry Tests");
        log.debug("Add/Update Policy Registry Test Initialised");
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                        userInfo.getUserName(), userInfo.getPassword());
    }


    /**
     * Add policy file from the file system
     */
    @Test(groups = {"wso2.greg"})
    public void addPolicyFromFile() throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourceName = "policy.xml";
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator
                + "policy" + File.separator + resourceName;
        resourceAdminServiceClient.addResource(policyPath + resourceName,
                "application/policy+xml", "testPolicy", new DataHandler(new URL("file:///" + resource)));
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(policyPath + resourceName));
    }

    /**
     * Add policy file from import URL functionality
     */
    @Test(groups = {"wso2.greg"})
    public void addPolicyFromURL() throws ResourceAdminServiceExceptionException, RemoteException {
        String resourceUrl = "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/policies/policy.xml";
        String resourceName = "RMpolicy3.xml";
        resourceAdminServiceClient.addPolicy(resourceName, "adding From URL", resourceUrl);
        Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(policyPath + resourceName));
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
        resourceAdminServiceClient.updateTextContent(policyPath + "policy.xml", resContent);
        Assert.assertEquals(resContent, resourceAdminServiceClient.getTextContent(policyPath + "policy.xml"));
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
        resourceAdminServiceClient.updateTextContent(policyPath + "RMpolicy3.xml", resContent);
        Assert.assertEquals(resContent, resourceAdminServiceClient.getTextContent(policyPath + "RMpolicy3.xml"));
    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource(policyPath + "policy.xml");
        resourceAdminServiceClient.deleteResource(policyPath + "RMpolicy3.xml");
        resourceAdminServiceClient = null;
        environment = null;
        policyPath = null;
        userInfo = null;
    }
}
