package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.artifacts.ArtifactHome;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class AddNewArtifactTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding new artifact is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ArtifactHome artifactHome = new ArtifactHome(driver);

        String artifact = "<artifactType type=\"application/policy+xml\" fileExtension=\"xml\" shortName=\"AutomatedArtifact\" singularLabel=\"Policy\" pluralLabel=\"Policies\" hasNamespace=\"false\" iconSet=\"30\">\n" +
                          "    <storagePath>/trunk/policies/@{name}</storagePath>\n" +
                          "    <content href=\"default\"/>\n" +
                          "</artifactType>";

        String artifactName = "AutomatedArtifact";

        artifactHome.addNewArtifact(artifact);
        artifactHome.checkOnUploadedArtifact(artifactName);

        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }


}






