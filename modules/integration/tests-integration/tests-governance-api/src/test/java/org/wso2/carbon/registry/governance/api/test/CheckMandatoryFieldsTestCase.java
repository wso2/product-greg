package org.wso2.carbon.registry.governance.api.test;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * test class for validating mandatory field check of governance API
 * https://wso2.org/jira/browse/REGISTRY-3080
 */
public class CheckMandatoryFieldsTestCase extends GREGIntegrationBaseTest {

    private static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    private Registry governance;
    private static final Log log = LogFactory.getLog(GenericArtifactTestCase.class);
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file")
    public void testAddNewRxtFile() throws Exception {
        Resource resource = governance.newResource();
        String rxtLocation = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";

        String rxtFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                             File.separator + "GREG" + File.separator + "rxt" + File.separator;

        resource.setContent(FileManager.readFile(rxtFilePath + "event.rxt"));

        resource.setMediaType(RXT_MEDIA_TYPE);
        wsRegistry.put(rxtLocation + "event.rxt", resource);
        assertTrue(wsRegistry.resourceExists(rxtLocation + "event.rxt"),
                   "rxt resource doesn't exists");

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance,
                                                GovernanceUtils.findGovernanceArtifactConfigurations(governance));


        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.findGovernanceArtifactConfiguration("events", governance);

        assertTrue(governanceArtifactConfiguration.getMediaType().contains("application/vnd" +
                                                                           ".wso2-events+xml"));
        assertTrue(governanceArtifactConfiguration.getContentDefinition().toString().contains
                ("<table name=\"Rules\">\n" +
                 "                <field type=\"options\">\n" +
                 "                    <name>Gender</name>\n" +
                 "                    <values>\n" +
                 "                        <value>male</value>\n" +
                 "                        <value>female</value>\n" +
                 "                    </values>\n" +
                 "                </field>\n" +
                 "                <field type=\"text-area\" required=\"true\">\n" +
                 "                    <name>Description</name>\n" +
                 "                </field>\n" +
                 "                <field type=\"text\">\n" +
                 "                    <name>Auther</name>\n" +
                 "                </field>\n" +
                 "            </table>"));

        assertTrue(governanceArtifactConfiguration.getPathExpression().contains("/events/@{details_name}"));
        assertTrue(governanceArtifactConfiguration.getArtifactElementNamespace().
                contains("http://www.wso2.org/governance/metadata"));
        assertTrue(governanceArtifactConfiguration.getKey().equals("events"));
    }

    @Test(groups = {"wso2.greg"}, description = "add an artefact with multiple longer lines",
          enabled = true, dependsOnMethods = "testAddNewRxtFile", expectedExceptions = GovernanceException.class,
          expectedExceptionsMessageRegExp =
                  "Description is a required field, Please provide a value for this parameter.")
    public void testMandatoryFieldValidation() throws RegistryException {
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");

        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                                  ("multiLineEvent2"));
        artifact.setAttribute("details_venue", "Colombo");
        artifact.setAttribute("details_date", "12/12/2012");
        artifact.setAttribute("details_name", "code");
        artifact.setAttribute("details_author", "testAuthor");

        /* attribute details_description is mandatory, but it is not provided here.
         * Therefore this execution results in a GovernanceException with message
         * "Description is a required field, Please provide a value for this parameter."
         * */

         artifactManager.addGenericArtifact(artifact);
    }

    @AfterClass()
    public void endGame() throws RegistryException {

        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact genericArtifact : artifacts) {
            artifactManager.removeGenericArtifact(genericArtifact.getId());
        }
        String rxtLocation = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";
        wsRegistry.delete(rxtLocation + "event.rxt");

        wsRegistry = null;
        governance = null;
    }
}
