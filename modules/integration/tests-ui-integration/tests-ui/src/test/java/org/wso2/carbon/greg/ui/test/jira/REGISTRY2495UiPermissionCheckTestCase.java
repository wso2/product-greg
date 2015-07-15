package org.wso2.carbon.greg.ui.test.jira;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.resource.ResourceHome;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * This test case is to test the fix for https://wso2.org/jira/browse/REGISTRY-2495
 * The issue was that a readonly user was able to view action buttons such as delete move rename and
 * was able to see the add resource and add collection buttons even though the user does not have permission
 */
public class REGISTRY2495UiPermissionCheckTestCase extends GREGIntegrationUIBaseTest {

    private static final Log log = LogFactory.getLog(Registry1103EndpointAddTestCase.class);
    private WebDriver driver;
    private static UserManagementClient userManagementClient;
    private static ServerConfigurationManager serverConfigurationManager1;
    private static ServerConfigurationManager serverConfigurationManager2;
    private User userInfo;

    private static final String USER_NAME = "uipermissionuser";
    private static final String PASSWORD = "password";
    private static final String USER_ROLE = "uipermissionRole";
    public static final String[] USER_PERMISSION = { "/permission/admin/login", "/permission/admin/manage/resources",
            "/permission/admin/manage/resources/browse" };

    private String carbonHome;

    @BeforeClass(alwaysRun = true) public void setUp() throws Exception {
        setupManageEnvironments();
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
        // Get the carbon home
        carbonHome = CarbonUtils.getCarbonHome();
    }

    private void setupManageEnvironments() throws Exception {
        String datasourcePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" +
                File.separator + "mountconfigs" + File.separator + "master-datasources.xml";
        String registyConfigPath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "mountconfigs" + File.separator + "registry.xml";
        String datasourceTargetPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" +
                        File.separator + "datasources" + File.separator + "master-datasources.xml";

        userManagementClient = new UserManagementClient(getBackendURL(),getSessionCookie());
        userManagementClient.addUser(USER_NAME, PASSWORD, null, USER_NAME);
        userManagementClient.addRole(USER_ROLE, new String[] { USER_NAME }, USER_PERMISSION);

        serverConfigurationManager1 = new ServerConfigurationManager (automationContext);
        serverConfigurationManager2 = new ServerConfigurationManager (automationContext);

        serverConfigurationManager1
                .applyConfigurationWithoutRestart(new File(datasourcePath), new File(datasourceTargetPath), true);
        serverConfigurationManager2.applyConfiguration(new File(registyConfigPath));

    }

    @Test(groups = "wso2.greg", description = "Verify adding new endpoint") public void test() throws IOException {
        try {

            // Login to server
            LoginPage loginPage = new LoginPage(driver);
            loginPage.loginAs(USER_NAME, PASSWORD);
            ResourceHome resourceHome = new ResourceHome(driver);
            driver.findElement(By.id("uLocationBar")).clear();
            driver.findElement(By.id("uLocationBar")).sendKeys("/_system/governance");
            driver.findElement(By.xpath("//*[@id=\"resourceMain\"]/table/tbody/tr/td/input[2]")).click();
            driver.findElement(By.id("actionLink2")).click();
            assertTrue(driver.findElements(By.linkText("Rename")).isEmpty());

        } finally {
            driver.close();
        }

    }

    @AfterClass(alwaysRun = true, groups = { "wso2.greg" }) public void tearDown() throws RegistryException, AxisFault {
        driver.quit();
    }

}
