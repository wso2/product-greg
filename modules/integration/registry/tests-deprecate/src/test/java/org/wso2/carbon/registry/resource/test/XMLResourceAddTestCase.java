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
package org.wso2.carbon.registry.resource.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

/**
 * A test case which tests registry XML resource add operation
 */
public class XMLResourceAddTestCase {
    private static final Log log = LogFactory.getLog(XMLResourceAddTestCase.class);
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();

    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");

        try {

            ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
            //add a collection to the registry
            String collectionPath = resourceAdminServiceStub.addCollection("/_system/config/", "ResFiles", "", "contains ResFiles");
            log.info("collection added to " + collectionPath);
            // Changing media type
            collectionPath = resourceAdminServiceStub.addCollection("/_system/config/", "ResFiles", "application/vnd.wso2.esb", "application/vnd.wso2.esb media type collection");

            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + "synapse.xml";

            resourceAdminServiceStub.addResource("/ResFiles/synapse.xml", "application/xml", "resDesc",
                    new DataHandler(new URL("file:///" + resource)), null, null);

            String textContent = resourceAdminServiceStub.getTextContent("/ResFiles/synapse.xml");

            //check the resource content existance
            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");

            } else {
                log.error("Unable to get text content");
                Assert.fail("Unable to get text content");

            }

            //delete the added resource
            resourceAdminServiceStub.delete("/ResFiles/synapse.xml");

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, "/ResFiles", "synapse.xml")) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                Assert.fail("Resource not deleted from the registry");
            }

        } catch (Exception e) {
            Assert.fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());

        }


    }

    private boolean isResourceExist(String sessionCookie, String resourcePath, String resourceName) throws Exception {
        boolean isResourceExist = false;
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);
        CollectionContentBean collectionContentBean = new CollectionContentBean();
        collectionContentBean = resourceAdminServiceStub.getCollectionContent(resourcePath);
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(resourcePath + "/" + resourceName)) {
                    ;
                }
                isResourceExist = true;
            }
        }
        return isResourceExist;
    }

}
