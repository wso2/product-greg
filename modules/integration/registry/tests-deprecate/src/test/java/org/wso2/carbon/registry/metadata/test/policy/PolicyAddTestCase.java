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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import static org.testng.Assert.*;

/**
 * Add Policy from File System and URL Functionality Tests
 */

public class PolicyAddTestCase {

    private static final Log log = LogFactory.getLog(PolicyAddTestCase.class);
    private String policyPath = "/_system/governance/trunk/policies/";
    private ResourceAdminServiceStub resourceAdminServiceStub;

    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";

    @BeforeClass(groups = {"wso2.greg.policy.a"})
    public void init() throws Exception {
        log.info("Initializing Add/Update Policy Registry Tests");
        log.debug("Add/Update Policy Registry Test Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);

    }

//    @Test(groups = {"wso2.greg"})
//    public void runSuccessCase() {
//        log.debug("Running SuccessCase");
//        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
//        addPolicyFromFile();     // Add policy file from the file system
//        updatePolicyFromFile();
//        addPolicyFromURL();    // Add policy file from import URL functionality
//        updatePolicyFromURL();
//    }

    /**
     * Add policy file from the file system
     */
    @Test(groups = {"wso2.greg.policy.a"})
    public void addPolicyFromFile() {
        String resourceName = "sample_policy.xml";

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(policyPath + resourceName,
                    RegistryConsts.POLICY_XML, "testPolicy", new DataHandler(new URL("file:///" + resource)), null, null);

            String textContent = resourceAdminServiceStub.getTextContent(policyPath + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                Assert.fail("Unable to get text content");
            }
        } catch (Exception e) {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }
    }

    /**
     * Add policy file from import URL functionality
     */
    @Test(groups = {"wso2.greg.policy.a"})
    public void addPolicyFromURL() {
        String resourceUrl =
//                "http://ww2.wso2.org/~charitha/policy/RMpolicy3.xml";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/policies/policy.xml";

        String resourceName = "RMpolicy3.xml";

        try {
            resourceAdminServiceStub.importResource(policyPath + resourceName, resourceName,
                    RegistryConsts.POLICY_XML, "SamplePolicyFile", resourceUrl, null, null);

            String textContent = resourceAdminServiceStub.getTextContent(policyPath + resourceName);

            if (!textContent.equals(null)) {
                log.info("Policy File adding and content retrieving was successful");
            } else {
                log.error("Unable to retrieve policy file content");
                Assert.fail("Unable to retrieve policy file content");
            }
        } catch (Exception e) {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addPolicyFromFile"})
    public void updatePolicyFromFile() {

        String resourceName = "sample_policy.xml";
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

        try {

            /**
             *  update policy and check the content
             */
            resourceAdminServiceStub.updateTextContent(policyPath + resourceName, resContent);

            if (resourceAdminServiceStub.getTextContent(policyPath + resourceName).contains("RMAssertion")) {
                log.info("Policy file successfully updated");
            } else {
                log.error("Policy File has not been updated in the registry");
                Assert.fail("Policy File has not been updated in the registry");
            }

        } catch (Exception e) {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }

    }

    @Test(groups = {"wso2.greg.policy.a"}, dependsOnMethods = {"addPolicyFromURL"})
    public void updatePolicyFromURL() {
        String resourceName = "RMpolicy3.xml";
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

        try {

            /**
             *  update policy and check the content
             */
            resourceAdminServiceStub.updateTextContent(policyPath + resourceName, resContent);

            if (!resourceAdminServiceStub.getTextContent(policyPath + resourceName).contains("InactivityTimeout")) {
                log.info("Policy file successfully updated");
            } else {
                log.error("Policy File has not been updated in the registry");
                Assert.fail("Policy File has not been updated in the registry");
            }

        } catch (
                Exception e
                )

        {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content : " + e.getMessage());
        }

    }


}
