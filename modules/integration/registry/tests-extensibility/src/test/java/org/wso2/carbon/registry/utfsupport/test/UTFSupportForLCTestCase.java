package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;

import java.io.File;
import java.rmi.RemoteException;

public class UTFSupportForLCTestCase {

    private final String LC_NAME = "ÀÁÂÃÄÅÆÇÈÉ";
    private LifeCycleManagementClient lifeCycleManagementClient;

    @BeforeClass
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());

    }


    @Test(groups = {"wso2.greg"}, description = "create LC")
    public void testCreateLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));
    }

    @Test(groups = {"wso2.greg"}, description = "edit LC", dependsOnMethods = "testCreateLifecycle")
    public void testEditLifecycle() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
