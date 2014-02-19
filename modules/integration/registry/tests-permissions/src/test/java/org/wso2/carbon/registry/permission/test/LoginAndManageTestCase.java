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
import org.wso2.carbon.automation.api.clients.identity.UserProfileMgtServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ReportAdminServiceClient;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

public class LoginAndManageTestCase {

    private ReportAdminServiceClient nonAdminReportAdminServiceClient;
    private UserProfileMgtServiceClient userProfileMgtServiceClient;
    private UserProfileMgtServiceClient userProfileMgtServiceClient2;


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        PermissionTestUtil.setUpTestRoles();
        EnvironmentBuilder builderNonAdmin;
        ManageEnvironment nonAdminEnvironment;
        EnvironmentBuilder builderNonAdmin2;
        ManageEnvironment nonAdmin2Environment;

        builderNonAdmin = new EnvironmentBuilder().greg(2);
        nonAdminEnvironment = builderNonAdmin.build();

        builderNonAdmin2 = new EnvironmentBuilder().greg(3);
        nonAdmin2Environment = builderNonAdmin2.build();

        assert nonAdminEnvironment != null;
        nonAdminReportAdminServiceClient =
                new ReportAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                             nonAdminEnvironment.getGreg().getSessionCookie());
        userProfileMgtServiceClient =
                new UserProfileMgtServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                                nonAdminEnvironment.getGreg().getSessionCookie());
        userProfileMgtServiceClient2 =
                new UserProfileMgtServiceClient(nonAdmin2Environment.getGreg().getBackEndUrl(),
                                                nonAdmin2Environment.getGreg().getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Test whether a non admin can add a report")
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
        userProfileDTO.setProfileName("testuser2_profile1");
        UserFieldDTO[] fields = new UserFieldDTO[1];
        UserFieldDTO field = new UserFieldDTO();
        field.setFieldValue("aslkmsd");
        field.setDisplayOrder(1);
        field.setClaimUri("http://sample.uri");
        field.setDisplayName("");
        fields[0] = field;
        userProfileDTO.setFieldValues(fields);

        userProfileMgtServiceClient.setUserProfile("testuser2", userProfileDTO);
        assertNotNull(userProfileMgtServiceClient.getUserProfile("testuser2", "testuser2_profile1"));
        //should give exception -- org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException
        userProfileMgtServiceClient2.getUserProfile("testuser2", "testuser2_profile1");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        PermissionTestUtil.resetTestRoles();
        nonAdminReportAdminServiceClient.deleteSavedReport("dummyReport");
        userProfileMgtServiceClient.deleteUserProfile("testuser2", "testuser2_profile1");

        nonAdminReportAdminServiceClient = null;
        userProfileMgtServiceClient = null;
        userProfileMgtServiceClient2 = null;
    }
}
