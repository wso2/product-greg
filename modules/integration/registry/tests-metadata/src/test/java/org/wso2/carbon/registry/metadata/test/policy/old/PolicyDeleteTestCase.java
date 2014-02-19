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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Add Policy Test from File System and From URL Functionality Tests
 */

public class PolicyDeleteTestCase {

    private static final Log log = LogFactory.getLog(PolicyDeleteTestCase.class);
    private String policyPath = "/_system/governance/trunk/policies/";
    private String testRes1 = "RMpolicy3.xml";
    private String testRes2 = "policy.xml";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add/Remove Policy Registry Test");
        log.debug("Add/Remove Policy Registry Test Initialised");
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                        userInfo.getUserName(), userInfo.getPassword());
    }

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

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addPolicyFromFile", "addPolicyFromURL"})
    public void deletingTest() throws Exception, RemoteException {
        log.debug("Running SuccessCase");
        /**
         * delete the added resource
         */
        resourceAdminServiceClient.deleteResource(policyPath + testRes1);
        resourceAdminServiceClient.deleteResource(policyPath + testRes2);

        /**
         * check if the deleted file exists in registry
         */

        assertTrue(!isFileExist(policyPath, testRes2) && (!isFileExist(policyPath, testRes1)));
    }

    public boolean isFileExist(String resourcePath, String resourceName) throws Exception {
        boolean isResourceExist = false;
        CollectionContentBean collectionContentBean = null;
        collectionContentBean = resourceAdminServiceClient.getCollectionContent(resourcePath);
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(resourcePath + resourceName)) {
                    isResourceExist = true;
                }
            }
        }
        return isResourceExist;
    }
}
