package org.wso2.carbon.registry.metadata.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class NameWithSpacesWsdlAdditionTestCase extends GREGIntegrationBaseTest {

    private final String associatedSchemaPath = "/_system/governance/trunk/schemas/org/bar/purchasing/1.0.0/purchasing.xsd";
    private final String associatedServicePath = "/_system/governance/trunk/services/com/foo/1.0.0/BizService";
    private final String associatedEndpointPath = "/_system/governance/trunk/endpoints/ep-com.wso2.people.services-BizService";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private final String wsdlPath = "/_system/governance/trunk/wsdls/com/foo/1.0.0/Wsdl with spaces in the name.wsdl";
    private String sessionCookie;

    @BeforeClass (groups = "wso2.greg", alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

    }

    @Test (groups = "wso2.greg", description = "Add WSDL name with spaces")
    public void testAddNameWithSpacesWSDL () throws RemoteException,
            ResourceAdminServiceExceptionException,
            RegistryExceptionException {

        resourceAdminServiceClient.addWSDL(
                "Wsdl with spaces in the name", "to check wsdl addtion with spaces in its name",
                "http://svn.wso2.org/repos/wso2/people/krishantha/wsdl/wsdl%20with%20spaces%20in%20the%20name.wsdl");

        resourceAdminServiceClient.setDescription(wsdlPath,
                "this wsdl has spaces in its name");

    }

    @Test (groups = "wso2.greg", description = "verify WSDL name with spaces",
            dependsOnMethods = "testAddNameWithSpacesWSDL")
    public void testVerifyNameWithSpacesWSDL () throws RemoteException,
            ResourceAdminServiceExceptionException {

        assertTrue(resourceAdminServiceClient.getMetadata(wsdlPath)
                .getDescription()
                .contentEquals("this wsdl has spaces in its name"));

    }

    @AfterClass (groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown () throws GovernanceException, RemoteException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.deleteResource(associatedServicePath);
        resourceAdminServiceClient.deleteResource(associatedSchemaPath);
        resourceAdminServiceClient.deleteResource(wsdlPath);
        resourceAdminServiceClient.deleteResource(associatedEndpointPath);
        resourceAdminServiceClient = null;

    }
}
