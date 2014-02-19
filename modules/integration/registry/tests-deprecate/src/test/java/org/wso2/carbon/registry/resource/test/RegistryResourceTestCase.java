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

import static org.testng.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

/**
 * A test case which tests registry resource
 */
public class RegistryResourceTestCase {
    /**
     * @goal testing registry resource functionalities in registry
     */
    private static final Log log = LogFactory.getLog(RegistryResourceTestCase.class);
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
            CollectionContentBean collectionContentBean = new CollectionContentBean();
            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation")) {
                        resourceAdminServiceStub.delete("/TestAutomation");
                    }
                }
            }
            String collectionPath = resourceAdminServiceStub.addCollection("/", "TestAutomation", "", "");
            log.info("collection added to " + collectionPath);
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "wso2", "", "");
            addResourceFromLocalFile(resourceAdminServiceStub);
            addResourceFromURL(resourceAdminServiceStub);
            addTextContent(resourceAdminServiceStub);
            resourceBoundaryTest(resourceAdminServiceStub);
            resourceAdminServiceStub.delete("/TestAutomation");
        } catch (Exception e) {
            fail("error occured while running Registry resource test " + e);
            log.error(" error occured while running Registry resource test " + e.getMessage());

        }


    }

    private void addResourceFromLocalFile(ResourceAdminServiceStub resourceAdminServiceStub) {
        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + "sampleText.txt";
            resourceAdminServiceStub.addResource("/TestAutomation/wso2/sampleText.txt", "text/html", "txtDesc",
                    new DataHandler(new URL("file:///" + resource)), null, null);
            String textContent = resourceAdminServiceStub.getTextContent("/TestAutomation/wso2/sampleText.txt");

            if (!textContent.equals("This is a simple text file.")) {
                log.error("Added resource not found");
                fail("Added resource not found");
            } else {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            }
        } catch (Exception e) {
            log.error("error occured while upload resource from file  " + e.getMessage());
            fail("error occured while upload resource from file");

        }
    }

    private void addResourceFromURL(ResourceAdminServiceStub resourceAdminServiceStub) {
        boolean isFound = false;
        try {
            resourceAdminServiceStub.addResource("/TestAutomation/wso2/org.wso2.carbon.registry.profiles.ui-3.0.0.jar",
                    "application/java-archive", "resource added from external URL",
                    new DataHandler(new URL(
                            "http://dist.wso2.org/maven2/org/wso2/carbon/org.wso2.carbon.registry.profiles.ui/3.0.0/org.wso2.carbon.registry.profiles.ui-3.0.0.jar")),
                    null, null);
            ResourceTreeEntryBean resourceTreeEntryBean = resourceAdminServiceStub.getResourceTreeEntry("/TestAutomation/wso2");
            String[] resourceChild = resourceTreeEntryBean.getChildren();
            for (int childCount = 0; childCount <= resourceChild.length; childCount++) {
                if (resourceChild[childCount].equalsIgnoreCase(
                        "/TestAutomation/wso2/org.wso2.carbon.registry.profiles.ui-3.0.0.jar")) {
                    isFound = true;
                    break;
                }
            }
            if (isFound = false) {
                log.error("uploaded resource not found in /TestAutomation/wso2/");
                fail("uploaded resource not found in /TestAutomation/wso2/");
            }
        } catch (Exception e) {
            log.error("error occured while upload resource from URL  " + e.getMessage());
            fail("error occured while upload resource from URL");

        }
    }

    private void addTextContent(ResourceAdminServiceStub resourceAdminServiceStub) {
        try {
            resourceAdminServiceStub.addTextResource("/TestAutomation/wso2/", "Hello", "text/plain", "sample", "Hello world");
            String textContent = resourceAdminServiceStub.getTextContent("/TestAutomation/wso2/Hello");

            if (!textContent.equals("Hello world")) {
                log.error("Added resource not found");
                fail("Added resource not found");
            } else {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            }

        } catch (Exception e) {
            log.error("error occured while adding text content  " + e.getMessage());
            fail("error occured while adding text content");
        }

    }

    public void resourceBoundaryTest(ResourceAdminServiceStub resourceAdminServiceStub) {

        // some characters may fail due to the CARBON-8352
        String[] charBuffer = {"~", "!", "@", "#", "%", "^", "*", "+", "=", "{", "}", "|", "\\", "<", ">", "\"", "\'", ";"};
        for (int i = 0; i < charBuffer.length; i++) {
            try {
                System.out.println(charBuffer[i]);
                resourceAdminServiceStub.addTextResource("/TestAutomation/wso2/", "Hello" + charBuffer[i], "text/plain", "sample", "Hello world");
                log.error("Invalid resource added with illigal character " + charBuffer[i]);
                resourceAdminServiceStub.delete("/TestAutomation");
                fail("Invalid resource added with illigal character " + charBuffer[i]);
            } catch (Exception e) {
                e.printStackTrace();
                //  if (!e.getMessage().contains("contains one or more illegal characters (~!@#$;%^*()+={}[]|\\<>\"',)" )) {
                if (!e.getMessage().contains(
                        "contains one or more illegal characters (~!@#;%^*()+={}|\\<>\"',)" +
                                "" +
                                "")) {
                    log.error("Invalid resource added with illigal character " + charBuffer[i]);
                    fail("Invalid resource added with illigal character " + charBuffer[i]);

                }
            }
        }
    }
}
//ToDo create custom content testcase need to be added