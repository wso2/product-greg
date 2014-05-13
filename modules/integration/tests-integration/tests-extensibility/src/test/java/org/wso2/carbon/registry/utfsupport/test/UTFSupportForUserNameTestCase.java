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
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "create user", dependsOnMethods = "testreadFile")
    public void testCreateUser() throws Exception {
        String[] roles = {"testRole"};
        Assert.assertTrue(userManagementClient.roleNameExists("testRole"));
        userManagementClient.addUser(utfString, "abcdef2", roles, utfString);
        boolean userAdded = userManagementClient.userNameExists("testRole", utfString);
        Assert.assertTrue(userAdded);
    }

    @AfterClass
    public void clean() throws Exception {
        userManagementClient.deleteUser(utfString);
        utfString = null;
        userManagementClient=null;
    }


}
