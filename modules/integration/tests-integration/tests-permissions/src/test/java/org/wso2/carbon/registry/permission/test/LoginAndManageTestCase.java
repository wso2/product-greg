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

package org.wso2.carbon.registry.permission.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;
import org.wso2.greg.integration.common.clients.ReportAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

public class LoginAndManageTestCase extends GREGIntegrationBaseTest{

    private ReportAdminServiceClient nonAdminReportAdminServiceClient;
    private UserProfileMgtServiceClient userProfileMgtServiceClient;
    private UserProfileMgtServiceClient userProfileMgtServiceClient2;
    private String userProfileName = "testuser2_profile1";


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        PermissionTestUtil.setUpTestRoles(automationContext);

        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                GREGTestConstants.SUPER_TENANT_DOMAIN_KEY,
                PermissionTestConstants.NON_ADMIN_ROLE_USER_1);

        AutomationContext automationContextUser2 = new AutomationContext("GREG", "greg001",
                GREGTestConstants.SUPER_TENANT_DOMAIN_KEY,
                PermissionTestConstants.NON_ADMIN_ROLE_USER_2);

        nonAdminReportAdminServiceClient =
                new ReportAdminServiceClient(automationContextUser1.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser1).login());

        userProfileMgtServiceClient =
                new UserProfileMgtServiceClient(automationContextUser1.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser1).login());

        userProfileMgtServiceClient2 =
                new UserProfileMgtServiceClient(automationContextUser2.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser2).login());
    }

    @Test(groups = "wso2.greg", description = "Test whether a non admin can add a report", dependsOnMethods = "testProfile")
    public void testAddReport() throws Exception {
        ReportConfigurationBean reportConfigurationBean = new ReportConfigurationBean();
        reportConfigurationBean.setName("dummyReport");
        reportConfigurationBean.setType("pdf");
        nonAdminReportAdminServiceClient.saveReport(reportConfigurationBean);
        assertNotNull(nonAdminReportAdminServiceClient.getSavedReport("dummyReport"));
    }

    @Test(groups = "wso2.greg", description = "Test whether a non admin can view others profiles",
          expectedExceptions = org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException.class)
    public void testProfile() throws UserProfileMgtServiceUserProfileExceptionException, RemoteException {
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setProfileName(userProfileName);
        UserFieldDTO[] fields = new UserFieldDTO[1];
        UserFieldDTO field = new UserFieldDTO();
        field.setFieldValue("aslkmsd");
        field.setDisplayOrder(1);
        field.setClaimUri("http://sample.uri");
        field.setDisplayName("");
        fields[0] = field;
        userProfileDTO.setFieldValues(fields);

        userProfileMgtServiceClient.setUserProfile(PermissionTestConstants.NON_ADMIN_ROLE_USER_1, userProfileDTO);
        assertNotNull(userProfileMgtServiceClient.getUserProfile(PermissionTestConstants.NON_ADMIN_ROLE_USER_1, userProfileName));
        //should give exception -- org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException
        userProfileMgtServiceClient2.getUserProfile(PermissionTestConstants.NON_ADMIN_ROLE_USER_1, userProfileName);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        PermissionTestUtil.resetTestRoles(automationContext);
        nonAdminReportAdminServiceClient.deleteSavedReport("dummyReport");
        userProfileMgtServiceClient.deleteUserProfile(PermissionTestConstants.NON_ADMIN_ROLE_USER_1, userProfileName);

        nonAdminReportAdminServiceClient = null;
        userProfileMgtServiceClient = null;
        userProfileMgtServiceClient2 = null;
    }
}
