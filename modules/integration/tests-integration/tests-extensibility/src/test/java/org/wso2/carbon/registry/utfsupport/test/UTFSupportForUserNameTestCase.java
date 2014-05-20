package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.io.IOException;

public class UTFSupportForUserNameTestCase extends GREGIntegrationBaseTest {

    private String utfString;
    private UserManagementClient userManagementClient;

    private String sessionCookie;
    private String backEndUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();

        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "create user", dependsOnMethods = "testreadFile")
    public void testCreateUser() throws Exception {

        String[] roles = {"testRole567"};

        if (!userManagementClient.roleNameExists(roles[0])) {
            userManagementClient.addRole(roles[0],null, null);
            userManagementClient.addUser(utfString, "abcdef2", roles, utfString);
        }

        boolean userAdded = userManagementClient.userNameExists(roles[0], utfString);
        Assert.assertTrue(userAdded);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        userManagementClient.deleteUser(utfString);
        utfString = null;
        userManagementClient=null;
    }

}
