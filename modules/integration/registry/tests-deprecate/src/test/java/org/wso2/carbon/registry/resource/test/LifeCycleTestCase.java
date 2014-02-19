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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

/**
 * A test case which tests registry life cycle operation
 */
public class LifeCycleTestCase {
    /**
     * @goal testing dependency feature in registry
     */

    private static final Log log = LogFactory.getLog(DependencyTestCase.class);
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        loggedInSessionCookie = util.login();
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");

        try {

            ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
            LifeCycleManagementServiceStub lifeCycleManagementServiceStub = TestUtils.getLifeCycleManagementServiceStub(loggedInSessionCookie);
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
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "LifeCycleTestCase", "", "");
            log.info("collection added to " + collectionPath);
            addDefaultLC(lifeCycleManagementServiceStub);
            updateLifecycle(lifeCycleManagementServiceStub);
            deleteLifecycle(lifeCycleManagementServiceStub);
        } catch (Exception e) {
        }
    }

    private void addDefaultLC(LifeCycleManagementServiceStub lifeCycleManagementServiceStub) {
        try {
//            LifecycleBean lifecycleBean = new LifecycleBean();
//            String xmlConfig = ConfigHelper.getXMLConfig(frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
//                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
//                "resources" + File.separator + "custom_lifecycle.xml");
//            lifecycleBean = lifeCycleManagementServiceStub.parseConfiguration(xmlConfig);
//            lifeCycleManagementServiceStub.createLifecycle(xmlConfig);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error occured while adding lifecycle :" + e.getMessage());
            fail("Error occured while adding lifecycle :" + e.getMessage());
        }
    }

    private void updateLifecycle(LifeCycleManagementServiceStub lifeCycleManagementServiceStub) {
        try {
//            LifecycleBean lifecycleBean = new LifecycleBean();
//            lifecycleBean = lifeCycleManagementServiceStub.getLifecycleBean("CustomLifeCycle");
//            if (lifecycleBean.getName().equalsIgnoreCase("CustomLifeCycle")) {
//                lifecycleBean.setName("CustomLifeCycleChanged");
//            } else {
//                log.error("Lifecycle update failed. CustomLifeCycle not found in lifecycle list");
//                Assert.fail("Lifecycle update failed. CustomLifeCycle not found in lifecycle list");
//            }
//            lifeCycleManagementServiceStub.updateLifecycle("CustomLifeCycle", lifecycleBean);
//            lifecycleBean = lifeCycleManagementServiceStub.getLifecycleBean("CustomLifeCycleChanged");
//            if (!lifecycleBean.getName().equalsIgnoreCase("CustomLifeCycleChanged")) {
//                log.error("Lifecycle not updated");
//                Assert.fail("Lifecycle not updated");
//            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occured while updating lifecycle : " + e);
            fail("Exception occured while updating lifecycle : " + e);
        }
    }

    private void deleteLifecycle(LifeCycleManagementServiceStub lifeCycleManagementServiceStub) {
        try {
            lifeCycleManagementServiceStub.deleteLifecycle("CustomLifeCycleChanged");
            log.info("Lifecycle \"CustomLifeCycleChanged\" deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error orrcured while deleting lifecycle - CustomLifeCycleChanged ");
            fail("Error orrcured while deleting lifecycle - CustomLifeCycleChanged ");
        }
    }
}
