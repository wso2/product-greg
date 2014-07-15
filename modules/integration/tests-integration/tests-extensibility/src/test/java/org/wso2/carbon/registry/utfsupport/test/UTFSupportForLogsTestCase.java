package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.AuthenticatorClient;
import org.wso2.greg.integration.common.clients.LogViewerClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForLogsTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private UserManagementClient userManagementClient;
    private LogViewerClient logViewerClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private WSRegistryServiceClient wsRegistryServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);
        logViewerClient =
                new LogViewerClient(backEndUrl,
                                    sessionCookie);
        userManagementClient =
                new UserManagementClient(backEndUrl,
                                         sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testReadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    //https://wso2.org/jira/browse/STRATOS-2445
    @Test(groups = {"wso2.greg"}, description = "create user and test system logs", dependsOnMethods = "testReadFile")
    public void testSystemLogs() throws Exception {

        String[] roles = {"testRole123"};
        String[] permissions = {"/permission/admin/configure/",
                                "/permission/admin/login",
                                "/permission/admin/manage/",
                                "/permission/admin/monitor",
                                "/permission/protected"};

        if (!userManagementClient.userNameExists(roles[0], utfString)) {
            if (!userManagementClient.roleNameExists(roles[0])) {
                userManagementClient.addRole(roles[0], null, permissions);
                resourceAdminServiceClient.addResourcePermission("/", roles[0], "3", "1");
                resourceAdminServiceClient.addResourcePermission("/", roles[0], "2", "1");
                resourceAdminServiceClient.addResourcePermission("/", roles[0], "4", "1");
                resourceAdminServiceClient.addResourcePermission("/", roles[0], "5", "1");
            }
            userManagementClient.addUser(utfString, "abcdef2", roles, utfString);
        }

        boolean userAdded = userManagementClient.userNameExists(roles[0], utfString);
        Assert.assertTrue(userAdded);

        AuthenticatorClient loginClient = new AuthenticatorClient(backEndUrl);
        String sessionCookie = loginClient.login(utfString, "abcdef2",
                                                 automationContext.getInstance().getHosts().get("default"));
        //create collection with new user
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);

        resourceAdminServiceClient.addCollection("/", "test_collection_2", "other", "desc");

        //search logs
        Thread.sleep(1000);
        logViewerClient = new LogViewerClient(backEndUrl,
                                              sessionCookie);




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
        delete("/test_collection_2");
        userManagementClient = new UserManagementClient(backEndUrl,
                                                        sessionCookie);
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
