package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.io.File;
import java.rmi.RemoteException;

public class UTFSupportForLCTestCase extends GREGIntegrationBaseTest {

    private final String LC_NAME = "ÀÁÂÃÄÅÆÇÈÉ";
    private LifeCycleManagementClient lifeCycleManagementClient;

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

        lifeCycleManagementClient =
                new LifeCycleManagementClient(backEndUrl, sessionCookie);

    }


    @Test(groups = {"wso2.greg"}, description = "create LC")
    public void testCreateLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));
    }

    @Test(groups = {"wso2.greg"}, description = "edit LC", dependsOnMethods = "testCreateLifecycle")
    public void testEditLifecycle() throws Exception {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "PromoteLC2.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);


        lifeCycleManagementClient.editLifeCycle(LC_NAME, lifeCycleContent);
        boolean lcEdited = true;
        String[] lifecycles = lifeCycleManagementClient.getLifecycleList();
        for (String lifecycle : lifecycles) {
            if (lifecycle.equals(LC_NAME)) {
                lcEdited = false;
            }
        }
        Assert.assertTrue(lcEdited);
    }


    @AfterClass
    public void testDeleteLC()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        lifeCycleManagementClient.deleteLifeCycle("ƀƁƂƃƄƅƆƇƈƉ");
        boolean lcDeleted = true;
        String[] lifecycles = lifeCycleManagementClient.getLifecycleList();
        for (String lifecycle : lifecycles) {
            if (lifecycle.equals(LC_NAME)) {
                lcDeleted = false;
            }
        }
        Assert.assertTrue(lcDeleted);

        lifeCycleManagementClient = null;

    }


}
