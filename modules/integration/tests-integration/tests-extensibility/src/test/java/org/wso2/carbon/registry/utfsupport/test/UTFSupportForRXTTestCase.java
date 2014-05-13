package org.wso2.carbon.registry.utfsupport.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class UTFSupportForRXTTestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

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

        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);

    }

    @Test(groups = "wso2.greg", description = "Add resource")
    public void testAddRXT()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException,

                   RegistryException, FileNotFoundException, LoginAuthenticationExceptionException,
                   LogoutAuthenticationExceptionException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG/rxt" + File.separator + "testutf.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(resourcePath));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/utf.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/utf.rxt"),
                   "rxt resource doesn't exists");

    }

    //
    @Test(groups = "wso2.greg", description = "Add resource", enabled = false)
    public void testAddfullRXT()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException,

                   RegistryException, FileNotFoundException, LoginAuthenticationExceptionException,
                   LogoutAuthenticationExceptionException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "rxt" +
                              File.separator + "testutf_full.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(resourcePath));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/utf.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/utf.rxt"),
                   "rxt resource doesn't exists");

    }

    @AfterClass
    public void clean() throws RegistryException {

        governance.delete("repository/components/org.wso2.carbon.governance/types/utf.rxt");

        governance=null;
        registryProviderUtil=null;
    }
}
