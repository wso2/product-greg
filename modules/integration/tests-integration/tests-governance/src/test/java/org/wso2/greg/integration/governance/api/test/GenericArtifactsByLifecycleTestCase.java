package org.wso2.greg.integration.governance.api.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GenericArtifactsByLifecycleTestCase extends GREGIntegrationBaseTest{

    private String LIFE_CYCLE_NAME = "ServiceLifeCycle";
    private Registry governance;
    private GenericArtifactManager genericArtifactManager;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws IOException, LoginAuthenticationExceptionException,
            RegistryException, LifeCycleManagementServiceExceptionException, XPathExpressionException {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext.getProductGroup().getGroupName(),automationContext.getDefaultInstance().getName(),
                        automationContext.getConfigurationNode("//superTenant/tenant/@key").getNodeValue(),
                        automationContext.getSuperTenant().getTenantAdmin().getKey());
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext.getUser().getUserName());

        //Can't clean old resource since tests are executing parallel.
        //governance.delete("/trunk");

        genericArtifactManager = new GenericArtifactManager(governance, "service");
        GenericArtifact genericArtifact = genericArtifactManager.
                newGovernanceArtifact(new QName("https://www.wso2.com/greg/store", "GenericArtifactStoreService"));
        genericArtifactManager.addGenericArtifact(genericArtifact);
    }

    @Test(groups = {"wso2.greg"}, description = "Artifacts by LC")
    public void testAttachLifecycle() throws RegistryException {
        GenericArtifact genericArtifact = getAddedGenericArtifact();
        genericArtifact.attachLifecycle(LIFE_CYCLE_NAME);
        String lifecycleName = genericArtifact.getLifecycleName();
        String lifecycleState = genericArtifact.getLifecycleState();

        Assert.assertEquals(lifecycleName, LIFE_CYCLE_NAME, "Different lifecycle found");
        Assert.assertEquals(lifecycleState, "Development", "Different lifecycle state found");

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        GenericArtifact[] filterByLCName = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        LIFE_CYCLE_NAME.equals(artifact.getLifecycleName());
            }
        });
        Assert.assertEquals(filterByLCName.length, 1,
                "Wrong number of artifacts associated with the lifecycle found");
        Assert.assertEquals(filterByLCName[0].getPath(), "/trunk/services/com/wso2/www/greg/store/GenericArtifactStoreService",
                "Different artifact path found");

        GenericArtifact[] filterByLCNameAndStatus = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        "Development".equals(artifact.getLifecycleState());
            }
        });
        Assert.assertEquals(filterByLCNameAndStatus.length, 1,
                "Wrong number of artifacts associated with the lifecycle in the given lifecycle state found");
        Assert.assertEquals(filterByLCNameAndStatus[0].getPath(), "/trunk/services/com/wso2/www/greg/store/GenericArtifactStoreService",
                        "Different artifact path found");
    }

    @Test(groups = {"wso2.greg"}, description = "Artifacts by LC", dependsOnMethods = "testAttachLifecycle")
    public void testGetArtifactsAfterPromoting() throws RegistryException {
        GenericArtifact artifact = getAddedGenericArtifact();
        Map<String, String> map = new HashMap<String, String>();
        map.put("/_system/governance/trunk/services/com/wso2/www/greg/store/GenericArtifactStoreService", "2.3.5");
        artifact.invokeAction("Promote", map);

        GenericArtifact[] filterByLCName = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        LIFE_CYCLE_NAME.equals(artifact.getLifecycleName());
            }
        });
        GenericArtifact artifactInBranch, artifactInTrunk;
        if(filterByLCName[0].getPath().startsWith("/branches/testing/services")){
            artifactInBranch = filterByLCName[0];
            artifactInTrunk = filterByLCName[1];
        } else {
            artifactInBranch = filterByLCName[1];
            artifactInTrunk = filterByLCName[0];
        }

        Assert.assertEquals(filterByLCName.length, 2,
                "Wrong number of artifacts associated with the lifecycle found");
        Assert.assertEquals(artifactInTrunk.getPath(), "/trunk/services/com/wso2/www/greg/store/GenericArtifactStoreService",
                "Different artifact path found");
        Assert.assertEquals(artifactInBranch.getPath(), "/branches/testing/services/com/wso2/www/greg/store/2.3.5/GenericArtifactStoreService",
                        "Different artifact path found");

        GenericArtifact[] filterByLCNameAndStatus = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        "Testing".equals(artifact.getLifecycleState());
            }
        });
        Assert.assertEquals(filterByLCNameAndStatus.length, 1,
                        "Wrong number of artifacts associated with the lifecycle in the given lifecycle state found");
        Assert.assertEquals(filterByLCNameAndStatus[0].getPath(),
                "/branches/testing/services/com/wso2/www/greg/store/2.3.5/GenericArtifactStoreService",
                "Different artifact path found");
    }

    private GenericArtifact getAddedGenericArtifact() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        GenericArtifact[] genericArtifacts = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService");
            }
        });
        return genericArtifacts[0];
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        genericArtifactManager.removeGenericArtifact(getAddedGenericArtifact().getId());
    }

}
