package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.File;


public class CategorizeArtifacts {
    private static ConfigurationContext configContext = null;

    private static final String CARBON_HOME = System.getProperty("carbon.home");
    private static final String axis2Repo = CARBON_HOME + File.separator + "repository" + File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf =
            ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private static final String username = "admin";
    private static final String password = "admin";
    private static final String serverURL = "https://localhost:9443/services/";
    private static final String [] categories = new String [] {"Engineering", "Finance", "HR", "Sales", "Marketing"};

    private static WSRegistryServiceClient initialize() throws Exception {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        return new WSRegistryServiceClient(serverURL, username, password, configContext);
    }

    public static void main(String[] args) throws Exception {
        try {

            final Registry registry = initialize();
            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);

            GenericArtifactManager artifactManager1 =
                    new GenericArtifactManager(gov, "restservice");

            GenericArtifact[] restServices = artifactManager1.getAllGenericArtifacts();

            if (restServices.length == 0) {
                System.out.println("No rest services found ..");
            }

            System.out.println("\nREST services found ... \n");
            int i = 0;
            for (GenericArtifact artifact : restServices) {
                String category = categories[i%5];
                artifact.setAttribute("overview_category", category);
                artifactManager1.updateGenericArtifact(artifact);
                i++;
            }

            Thread.sleep(5 * 1000);

            GenericArtifactManager artifactManager2 =
                    new GenericArtifactManager(gov, "soapservice");

            GenericArtifact[] soapServices = artifactManager2.getAllGenericArtifacts();

            if (soapServices.length == 0) {
                System.out.println("No soap services found ..");
            }

            System.out.println("\nSOAP services found ... \n");
            int j = 0;
            for (GenericArtifact artifact : soapServices) {
                String category = categories[j%5];
                artifact.setAttribute("overview_category", category);
                artifactManager2.updateGenericArtifact(artifact);
                j++;
            }

        } finally {
            PaginationContext.destroy();
        }
        System.exit(1);

    }
}
