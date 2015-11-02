package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;


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
    private static final String[] tagsList = new String[] { "wso2", "greg", "pay", "fb", "twitter", "money", "json",
            "js", "amason", "uber", "people", "android", "mac", "instagram", "dollers" };
    private static String rootpath = "";

    private static WSRegistryServiceClient initialize() throws Exception {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        if (System.getProperty("carbon.home").equals("../../../../")) {
            rootpath = "../";
        }
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

            System.out.println("\nREST services found for categorization ... \n");

            ArrayList<String> restServicesList = new ArrayList<String>();
            try {
                BufferedReader bufferedReader = new BufferedReader(
                        new FileReader(rootpath + "resources/restservice_list.txt"));
                String artifactName;
                while ((artifactName = bufferedReader.readLine()) != null) {
                    restServicesList.add(artifactName);
                }
                int i = 0;
                for (GenericArtifact artifact : restServices) {
                    if (restServicesList.contains(artifact.getQName().getLocalPart())) {
                        String category = categories[i % 5];
                        artifact.setAttribute("overview_category", category);
                        artifactManager1.updateGenericArtifact(artifact);
                        String path = artifact.getPath();
                        gov.applyTag(path, tagsList[i % 15]);
                        i++;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not read restservice list");
            }

            Thread.sleep(5 * 1000);

            GenericArtifactManager artifactManager2 =
                    new GenericArtifactManager(gov, "soapservice");

            GenericArtifact[] soapServices = artifactManager2.getAllGenericArtifacts();

            if (soapServices.length == 0) {
                System.out.println("No soap services found ..");
            }

            System.out.println("\nSOAP services found for categorization ... \n");

            ArrayList<String> soapServicesList = new ArrayList<String>();
            try {
                BufferedReader bufferedReader = new BufferedReader(
                        new FileReader(rootpath + "resources/soapservice_list.txt"));
                String artifactName;
                while ((artifactName = bufferedReader.readLine()) != null) {
                    soapServicesList.add(artifactName);
                }
                int j = 0;
                for (GenericArtifact artifact : soapServices) {
                    if (soapServicesList.contains(artifact.getQName().getLocalPart())) {
                        String category = categories[j % 5];
                        artifact.setAttribute("overview_category", category);
                        artifactManager2.updateGenericArtifact(artifact);
                        String path = artifact.getPath();
                        gov.applyTag(path, tagsList[j%15]);
                        j++;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not read soapservice list");
            }

        } finally {
            PaginationContext.destroy();
        }
        System.exit(1);

    }
}
