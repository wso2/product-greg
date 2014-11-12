package org.wso2.carbon.registry.governance.api.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.util.HashMap;
import java.util.Map;

public class GenericArtifactsByLifecycleTestCase extends GREGIntegrationBaseTest {
    private String LIFE_CYCLE_NAME = "ServiceLifeCycle";
    private Registry governance;
    private GenericArtifactManager genericArtifactManager;
    private GenericArtifact genericArtifact;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);

        //Can't clean old resource since tests are executing parallel.
        //governance.delete("/trunk");

        genericArtifactManager = new GenericArtifactManager(governance, "service");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">");
        stringBuilder.append("<overview><name>");
        stringBuilder.append("GenericArtifactStoreService");
        stringBuilder.append("</name><namespace>");
        stringBuilder.append("https://www.wso2.com/greg/store");
        stringBuilder.append("</namespace><version>1.0.0-SNAPSHOT</version></overview>");
        stringBuilder.append("</serviceMetaData>");
        String content = stringBuilder.toString();
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        genericArtifact =
                genericArtifactManager.newGovernanceArtifact(XMLContent);
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

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifact[] filterByLCName = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        LIFE_CYCLE_NAME.equals(artifact.getLifecycleName());
            }
        });
        Assert.assertEquals(filterByLCName.length, 1,
                "Wrong number of artifacts associated with the lifecycle found");
        Assert.assertEquals(filterByLCName[0].getPath(), "/trunk/services/com/wso2/www/greg/store/1.0.0-SNAPSHOT/GenericArtifactStoreService",
                "Different artifact path found"); //new addition: version of artifacts are added to the path.

        GenericArtifact[] filterByLCNameAndStatus = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        "Development".equals(artifact.getLifecycleState());
            }
        });
        Assert.assertEquals(filterByLCNameAndStatus.length, 1,
                "Wrong number of artifacts associated with the lifecycle in the given lifecycle state found");
        Assert.assertEquals(filterByLCNameAndStatus[0].getPath(), "/trunk/services/com/wso2/www/greg/store/1.0.0-SNAPSHOT/GenericArtifactStoreService",
                "Different artifact path found");
    }

    @Test(groups = {"wso2.greg"}, description = "Artifacts by LC", dependsOnMethods = "testAttachLifecycle")
    public void testGetArtifactsAfterPromoting() throws RegistryException {
        GenericArtifact artifact = getAddedGenericArtifact();
        Map<String, String> map = new HashMap<String, String>();
        map.put("/_system/governance/trunk/services/com/wso2/www/greg/store/1.0.0-SNAPSHOT/GenericArtifactStoreService", "2.3.5");
        artifact.invokeAction("Promote", map);

        GenericArtifact[] filterByLCName = genericArtifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getQName().getLocalPart().equals("GenericArtifactStoreService") &&
                        LIFE_CYCLE_NAME.equals(artifact.getLifecycleName());
            }
        });
        GenericArtifact artifactInBranch, artifactInTrunk;
        if (filterByLCName[0].getPath().startsWith("/branches/testing/services")) {
            artifactInBranch = filterByLCName[0];
            artifactInTrunk = filterByLCName[1];
        } else {
            artifactInBranch = filterByLCName[1];
            artifactInTrunk = filterByLCName[0];
        }

        Assert.assertEquals(filterByLCName.length, 2,
                "Wrong number of artifacts associated with the lifecycle found");
        Assert.assertEquals(artifactInTrunk.getPath(), "/trunk/services/com/wso2/www/greg/store/1.0.0-SNAPSHOT/GenericArtifactStoreService",
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
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
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
