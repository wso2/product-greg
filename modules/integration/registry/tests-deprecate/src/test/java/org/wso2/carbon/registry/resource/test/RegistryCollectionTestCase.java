/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.registry.resource.test;

import static org.testng.Assert.*;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

/**
 * A test case which tests registry collection
 */
public class RegistryCollectionTestCase {
    /**
     * @goal testing add colleciton feature in registry
     */

    private static final Log log = LogFactory.getLog(RegistryCollectionTestCase.class);
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Registry collection test started");
        loggedInSessionCookie = util.login();
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {
        try {
            CollectionContentBean collectionContentBean = new CollectionContentBean();
            ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation")) {
                        resourceAdminServiceStub.delete("/TestAutomation");
                    }
                }
            }
            // adding normal collection
            String collectionPath = resourceAdminServiceStub.addCollection("/", "TestAutomation", "", "");
            // Changing media type
            log.debug("collection added to " + collectionPath);
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "wso2.esb",
                    "application/vnd.wso2.esb", "application/vnd.wso2.esb media type collection");
            log.debug("Media type application/vnd.apache.axis2 collection added to " + collectionPath);
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "apache.synapse",
                    "application/vnd.apache.synapse", "application/vnd.apache.synapse media type collection");
            log.debug("Media type application/vnd.apache.synapse collection added to " + collectionPath);
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "apache.axis2",
                    "application/vnd.apache.axis2", "application/vnd.apache.axis2 media type collection");
            log.debug("Media type application/vnd.apache.axis2 collection added to " + collectionPath);
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "wso2.wsas",
                    "application/vnd.wso2.wsas", "application/vnd.wso2.wsas media type collection");
            log.debug("Media type application/vnd.wso2.wsas collection added to " + collectionPath);

            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/TestAutomation");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation/wso2.esb")) {
                        log.debug("/TestAutomation/wso2.esb resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/apache.synapse")) {
                        log.debug("/TestAutomation/apache.synapse resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/apache.axis2")) {
                        log.debug("/TestAutomation/apache.axis2 resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/wso2.wsas")) {
                        log.debug("/TestAutomation/wso2.wsas resource found");
                    } else {
                        log.error("Resource didn't found in : " + childPath[i]);
                        fail("Resource didn't found in : " + childPath[i]);
                    }

                }
            }
            // Renaming collections
            resourceAdminServiceStub.renameResource("/TestAutomation", "/TestAutomation/wso2.esb", "new_wso2.esb");
            resourceAdminServiceStub.renameResource("/TestAutomation", "/TestAutomation/apache.synapse", "new_apache.synapse");
            resourceAdminServiceStub.renameResource("/TestAutomation", "/TestAutomation/apache.axis2", "new_apache.axis2");
            resourceAdminServiceStub.renameResource("/TestAutomation", "/TestAutomation/wso2.wsas", "new_wso2.wsas");
            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/TestAutomation");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation/new_wso2.esb")) {
                        log.debug("/TestAutomation/new_wso2.esb resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_apache.synapse")) {
                        log.debug("/TestAutomation/new_apache.synapse resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_apache.axis2")) {
                        log.debug("/TestAutomation/new_apache.axis2 resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_wso2.wsas")) {
                        log.debug("/TestAutomation/new_wso2.wsas resource found");
                    } else {
                        log.error("Resource didn't rename : " + childPath[i]);
                        fail("Resource didn't rename : " + childPath[i]);
                    }

                }
            }

            // move collections
            resourceAdminServiceStub.addCollection("/TestAutomation", "movedCollections", "",
                    "Collections which contain moved sample collections");
            resourceAdminServiceStub.moveResource("/TestAutomation", "/TestAutomation/new_wso2.esb", "/TestAutomation/movedCollections", "new_wso2.esb");
            resourceAdminServiceStub.moveResource("/TestAutomation", "/TestAutomation/new_apache.synapse", "/TestAutomation/movedCollections", "new_apache.synapse");
            resourceAdminServiceStub.moveResource("/TestAutomation", "/TestAutomation/new_apache.axis2", "/TestAutomation/movedCollections", "new_apache.axis2");
            resourceAdminServiceStub.moveResource("/TestAutomation", "/TestAutomation/new_wso2.wsas", "/TestAutomation/movedCollections", "new_wso2.wsas");
            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/TestAutomation/movedCollections");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation/movedCollections/new_wso2.esb")) {
                        log.debug("/TestAutomation/movedCollections/new_wso2.esb resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/movedCollections/new_apache.synapse")) {
                        log.debug("/TestAutomation/movedCollections/new_apache.synapse resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/movedCollections/new_apache.axis2")) {
                        log.debug("/TestAutomation/movedCollections/new_apache.axis2 resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/movedCollections/new_wso2.wsas")) {
                        log.debug("/TestAutomation/movedCollections/new_wso2.wsas resource found");
                    } else {
                        log.error("Resource didn't move : " + childPath[i]);
                        fail("Resource didn't move : " + childPath[i]);
                    }

                }
            }

            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/TestAutomation");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation/new_wso2.esb")) {
                        log.error("/TestAutomation/new_wso2.esb resource found in original path after move ");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_apache.synapse")) {
                        log.error("/TestAutomation/new_apache.synapse resource found in original path after move");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_apache.axis2")) {
                        log.error("/TestAutomation/new_apache.axis2 resource found in original path after move");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_wso2.wsas")) {
                        log.error("/TestAutomation/new_wso2.wsas resource found in original path after move");
                    }
                }
            }

            //copy collections
            resourceAdminServiceStub.copyResource("/TestAutomation/movedCollections", "/TestAutomation/movedCollections/new_wso2.esb", "/TestAutomation", "new_wso2.esb");
            resourceAdminServiceStub.copyResource("/TestAutomation/movedCollections", "/TestAutomation/movedCollections/new_apache.synapse", "/TestAutomation", "new_apache.synapse");
            resourceAdminServiceStub.copyResource("/TestAutomation/movedCollections", "/TestAutomation/movedCollections/new_apache.axis2", "/TestAutomation", "new_apache.axis2");
            resourceAdminServiceStub.copyResource("/TestAutomation/movedCollections", "/TestAutomation/movedCollections/new_wso2.wsas", "/TestAutomation", "new_wso2.wsas");

            resourceAdminServiceStub.delete("/TestAutomation/movedCollections");

            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/TestAutomation");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation/new_wso2.esb")) {
                        log.debug("/TestAutomation/new_wso2.esb resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_apache.synapse")) {
                        log.debug("/TestAutomation/new_apache.synapse resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_apache.axis2")) {
                        log.debug("/TestAutomation/new_apache.axis2 resource found");
                    } else if (childPath[i].equalsIgnoreCase("/TestAutomation/new_wso2.wsas")) {
                        log.debug("/TestAutomation/new_wso2.wsas resource found");
                    } else {
                        log.error("Resource didn't found after copoied : " + childPath[i]);
                        fail("Resource didn't rename after copoied : " + childPath[i]);
                    }
                }
            }


            collectionBoundaryTest(resourceAdminServiceStub);
        } catch (Exception e) {
            log.error("Exception thrown while running Registry collection test : " + e.getMessage());
            fail("Exception thrown while running Registry collection test : " + e.getMessage());
        }

    }

    String collectionPath = null;

    public void collectionBoundaryTest(ResourceAdminServiceStub resourceAdminServiceStub) {

        // some characters may fail due to the CARBON-8331
        String[] charBuffer = {"~", "!", "@", "#", "%", "^", "*", "+", "=", "{", "}", "|", "\\", "<", ">", "\"", "\'", ";"};
        for (int i = 0; i < charBuffer.length; i++) {
            try {
                System.out.println(charBuffer[i]);
                collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "wso2." + charBuffer[i],
                        "application/vnd.wso2.esb", "application/vnd.wso2.esb media type collection");
                if (!collectionPath.equals(null)) {
                    log.error("Invalid collection added with illigal character " + charBuffer[i]);
                    resourceAdminServiceStub.delete("/TestAutomation");
                    fail("Invalid collection added with illigal character " + charBuffer[i]);
                }

            } catch (AxisFault e) {
                if (collectionPath == null) {
                    log.info("Successfully rejected invalidly named collection add operation..!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                //  if (!e.getMessage().contains("contains one or more illegal characters (~!@#$;%^*()+={}[]|\\<>\"',)" )) {
                if (!e.getMessage().contains("contains one or more illegal characters (~!@#$;%^*()+={}|\\<>\"',)")) {
                    log.error("Invalid collection added with illigal character " + charBuffer[i]);
                    fail("Invalid collection added with illigal character " + charBuffer[i]);

                }
            }
        }
    }

}
