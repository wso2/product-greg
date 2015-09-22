
package org.wso2.carbon.registry.capp.deployment.test;

import org.wso2.carbon.registry.capp.deployment.test.utils.CAppTestUtils;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RevertFaultyCarTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient wsRegistry;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    private final String cAppName = "faulty-1.0.0";
    private final String wsdl1Path = "/_system/governance/trunk/wsdls/com/cdyne/ws/weatherws/1.0.0/weatherws.wsdl";
    private final String wsdl2Path = "/_system/governance/trunk/wsdls/net/webservicex/www/1.0.0/globalweatherwsdl.wsdl";


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

        adminServiceApplicationAdmin = new ApplicationAdminClient(backEndUrl,
                sessionCookie);
        cAppUploader = new CarbonAppUploaderClient(backEndUrl,
                sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
    }

    @Test(description = "Upload CApp having Text Resources", enabled = false)
    public void uploadFaultyCapp()
            throws MalformedURLException, RemoteException, InterruptedException,
            ApplicationAdminExceptionException {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "faulty-1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("faulty-1.0.0.car",
                new DataHandler(new URL("file:///" + filePath)));

        Assert.assertFalse(CAppTestUtils.isCAppDeployed(sessionCookie,
                cAppName, adminServiceApplicationAdmin), "Deployed CApplication not in CApp List");

    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadFaultyCapp"}, enabled = false)
    public void isResourcesExist() throws RegistryException {

        Assert.assertFalse(wsRegistry.resourceExists(wsdl1Path), wsdl1Path + " resource does not exist");
        Assert.assertFalse(wsRegistry.resourceExists(wsdl2Path), wsdl2Path + " resource does not exist");

    }

    @Test(description = "Verify Delete Carbon Application ", dependsOnMethods = {"isResourcesExist"}, enabled = false)
    public void isCappDeleted()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException {

        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie,
                cAppName, adminServiceApplicationAdmin), "Deployed CApplication still in CApp List");
    }

    @Test(description = "Verify Resource Deletion", dependsOnMethods = {"isCappDeleted"}, enabled = false)
    public void isResourcesDeleted() throws RegistryException {

        Assert.assertFalse(wsRegistry.resourceExists(wsdl1Path), wsdl1Path + " resource does not exist");
        Assert.assertFalse(wsRegistry.resourceExists(wsdl2Path), wsdl2Path + " resource does not exist");

    }

    @AfterClass
    public void destroy()
            throws ApplicationAdminExceptionException, InterruptedException, RemoteException,
            RegistryException {

        if (!(CAppTestUtils.isCAppDeleted(sessionCookie,
                cAppName, adminServiceApplicationAdmin))) {
            adminServiceApplicationAdmin.deleteApplication(cAppName);
        }
        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        wsRegistry = null;
    }
}

