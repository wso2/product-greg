package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForLogsTestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private ManageEnvironment environment;
    private UserManagementClient userManagementClient;
    private LogViewerClient logViewerClient;
    private EnvironmentBuilder builder;
    int userId = ProductConstant.ADMIN_USER_ID;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private WSRegistryServiceClient wsRegistryServiceClient;
    private UserInfo userInfo;
    private UserInfo newUser;

    @BeforeClass
    public void init() throws Exception {

        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId,
                                                                     ProductConstant.GREG_SERVER_NAME);
        logViewerClient =
                new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                    environment.getGreg().getSessionCookie());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                         environment.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testReadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    //https://wso2.org/jira/browse/STRATOS-2445
    @Test(groups = {"wso2.greg"}, description = "create user and test system logs", dependsOnMethods = "testReadFile")
    public void testSystemLogs() throws Exception {
        String[] roles = {ProductConstant.DEFAULT_PRODUCT_ROLE};
        Assert.assertTrue(userManagementClient.roleNameExists(ProductConstant.DEFAULT_PRODUCT_ROLE));
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkSettings framework = env.getFrameworkSettings();

        if (framework.getEnvironmentSettings().is_runningOnStratos()) {
            newUser = new UserInfo(utfString + '@' + userInfo.getDomain(), "abcdef2", userInfo.getDomain());
        } else {
            newUser = new UserInfo(utfString, "abcdef2", userInfo.getDomain());
        }


        userManagementClient.addUser(utfString, "abcdef2", roles, utfString);

        boolean userAdded = userManagementClient.userNameExists(ProductConstant.DEFAULT_PRODUCT_ROLE, utfString);
        Assert.assertTrue(userAdded);

        AuthenticatorClient loginClient = new AuthenticatorClient(environment.getGreg().getBackEndUrl());
        String sessionCookie = loginClient.login(newUser.getUserName(), "abcdef2", environment.getGreg().getProductVariables().getHostName());
        //create collection with new user
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               sessionCookie);

        resourceAdminServiceClient.addCollection("/", "test_collection", "other", "desc");

        //search logs
        Thread.sleep(1000);
        logViewerClient = new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        boolean status = false;
        LogEvent[] logEvents = logViewerClient.getLogs("INFO", utfString, "", "");
        if (logEvents != null) {
                for (LogEvent event : logEvents) {
                if (event.getMessage().contains(utfString)) {
                    status = true;
                    break;
                }
            }
        }
        Assert.assertTrue(status, "User name not found in the system logs");

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        delete("/test_collection");
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());
        userManagementClient.deleteUser(utfString);

        utfString = null;
        userManagementClient = null;
        resourceAdminServiceClient = null;
        logViewerClient = null;
        wsRegistryServiceClient = null;

    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}
