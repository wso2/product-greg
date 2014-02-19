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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.activities.stub.ActivityAdminServiceStub;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import static org.testng.Assert.*;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

/**
 * A test case which tests registry activity search operation
 */
public class ActivitySearchTestCase {
    private static final Log log = LogFactory.getLog(ActivitySearchTestCase.class);

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private ActivityAdminServiceStub activityAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
        activityAdminServiceStub = TestUtils.getActivityAdminServiceStub(loggedInSessionCookie);

    }

//    @Test(groups = {"wso2.greg"})
//    public void runSuccessCase() {
//
//        addResource();
//        searchActivity();
//    }

    @Test(groups = {"wso2.greg"})
    public void addResource() {
        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(wsdlPath + resourceName,
                    "application/wsdl+xml", "test resource", new DataHandler(new URL("file:///" + resource)), null, null);
            // wait for sometime until the resource has been added. The activity logs are written
            // every 10 seconds, so you'll need to wait until that's done.
            Thread.sleep(20000);
        } catch (Exception e) {
            fail("Unable to add resource: " + e);
            log.error("Unable to add resource: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void searchActivity() {

        try {
            ActivityBean bean = activityAdminServiceStub.getActivities("admin", wsdlPath + resourceName, "", "", "",
                    "", loggedInSessionCookie);
            if (bean.getActivity() == null) {
                fail("Activity search failed");
            }
            resourceAdminServiceStub.delete(wsdlPath + resourceName);
        } catch (Exception e) {
            fail("Activity search failed: " + e);
            log.error("Activity search failed: " + e.getMessage());
            String msg = "Failed to get search results from the Activity search service. " +
                    e.getMessage();
            log.error(msg, e);
        }

    }


}
