package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;

import java.io.IOException;

public class UTFSupportForUserNameTestCase {

    private String utfString;
    private UserManagementClient userManagementClient;

    @BeforeClass
    public void init() throws Exception {
        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());

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
