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

package org.wso2.greg.integration.resources.resource.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ActivityAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry activity search operation
 */
public class ActivitySearchTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(ActivitySearchTestCase.class);

    private static String WSDL_PATH ="/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/";
    private static String RESOURCE_NAME = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private String loggedInSessionCookie = "";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");

        loggedInSessionCookie = getSessionCookie();
        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                        automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());
        activityAdminServiceClient = new ActivityAdminServiceClient(getBackendURL(),
                automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());
    }

//    @Test(groups = {"wso2.greg"})
//    public void runSuccessCase() {
//
//        addResource();
//        searchActivity();
//    }

    @Test(groups = {"wso2.greg"})
    public void addResource()
            throws InterruptedException, MalformedURLException, ResourceAdminServiceExceptionException,
            RemoteException, XPathExpressionException {

        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                + "GREG" + File.separator + "wsdl" + File.separator + RESOURCE_NAME;

        resourceAdminServiceClient.addResource(WSDL_PATH + RESOURCE_NAME,
                "application/wsdl+xml", "test resource", new DataHandler(new URL("file:///" + resource)));

        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);

        assertTrue(resourceAdminServiceClient.getResource(WSDL_PATH+RESOURCE_NAME)[0].getAuthorUserName()
                .contains(automationContext.getContextTenant().getContextUser().getUserName()));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void searchActivity()
            throws RegistryExceptionException, RemoteException, ResourceAdminServiceExceptionException {

        ActivityBean bean = activityAdminServiceClient.getActivities(loggedInSessionCookie,
                "admin", WSDL_PATH + RESOURCE_NAME, "", "", "", 0);
        assertNotNull(bean.getActivity(), "Activity search failed");
    }

    //cleanup code
    @AfterClass
    public void cleanup()
            throws Exception {

        resourceAdminServiceClient.deleteResource(WSDL_PATH + RESOURCE_NAME);
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/" +
                                                                    "eu/dataaccess/footballpool/1.0.0-SNAPSHOT/Info");
        resourceAdminServiceClient = null;
        activityAdminServiceClient = null;
    }
}
