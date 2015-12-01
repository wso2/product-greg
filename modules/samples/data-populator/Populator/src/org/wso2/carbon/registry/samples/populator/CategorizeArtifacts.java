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
import org.wso2.carbon.registry.samples.populator.utils.LifeCycleManagementClient;
import org.wso2.carbon.registry.samples.populator.utils.UserManagementClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.lang.System;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/*
* This class add categories,tags to rest and soap services, and create publisher and
* store user roles.
 */
public class CategorizeArtifacts {

    private static String cookie;
    private static ConfigurationContext configContext = null;
    private static LifeCycleManagementClient lifeCycleManagementClient;
    private static final String CARBON_HOME = System.getProperty("carbon.home");
    private static final String axis2Repo =
            CARBON_HOME + File.separator + "repository" + File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf = ServerConfiguration.getInstance()
            .getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private static final String username = "admin";
    private static final String password = "admin";
    private static String port;
    private static String host;
    private static String serverURL;
    private static final String[] categories = new String[] { "Engineering", "Finance", "HR", "Sales", "Marketing" };
    private static final String[] tagsList = new String[] { "wso2", "greg", "pay", "money", "json", "js", "people",
            "registry", "apps", "services" };
    private static String rootpath = "";
    private static String governancePath = "/_system/governance";

    private static WSRegistryServiceClient initialize() throws Exception {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);
        if (System.getProperty("carbon.home").equals("../../../../")) {
            rootpath = "../";
        }
        return new WSRegistryServiceClient(serverURL, username, password, configContext) {

            public void setCookie(String cookie) {
                CategorizeArtifacts.cookie = cookie;
                super.setCookie(cookie);
            }
        };
    }

    public static void main(String[] args) throws Exception {
        try {
            port = args[0];
            if (port == null || port.length() == 0) {
                port = "9443";
            }
            host = args[1];
            if (host == null || host.length() == 0) {
                host = "localhost";
            }
            serverURL = "https://" + host + ":" + port + "/services/";
            final Registry registry = initialize();
            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie, serverURL, configContext);
            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);

            GenericArtifactManager artifactManager1 = new GenericArtifactManager(gov, "restservice");

            GenericArtifact[] restServices = artifactManager1.getAllGenericArtifacts();
            lifeCycleManagementClient = new LifeCycleManagementClient(cookie, serverURL, configContext);

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
                        gov.applyTag(path, tagsList[i % 10]);
                        changeLcState((i % 4), path);
                        if (i % 3 == 0) {
                            addAnonymousViewToAssets(resourceServiceClient, artifact);
                        }
                        i++;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not read restservice list");
            }

            Thread.sleep(5 * 1000);

            GenericArtifactManager artifactManager2 = new GenericArtifactManager(gov, "soapservice");

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
                        gov.applyTag(path, tagsList[j % 10]);
                        changeLcState((j % 4), path);
                        if (j % 3 == 0) {
                            addAnonymousViewToAssets(resourceServiceClient, artifact);
                        }
                        j++;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not read soapservice list");
            }
            addUsers(configContext);
        } finally {
            PaginationContext.destroy();
        }
        System.exit(1);

    }
    /**
     *This method assign life cycle state to asset based on state.
     *
     * @param state     random number which determines LC state
     * @param path      path of the resource.
     * @throws Exception
     */
    private static void changeLcState(int state, String path) throws Exception {
        path = governancePath + path;
        String items[] = { "false", "false", "false" };
        if (state == 0) {
            lifeCycleManagementClient.invokeAspect(path, "ServiceLifeCycle", "Promote", items);
        } else if (state == 1) {
            items[0] = "true";
            lifeCycleManagementClient.invokeAspect(path, "ServiceLifeCycle", "itemClick", items);
        } else if (state == 2) {
            items[0] = "true";
            items[1] = "true";
            lifeCycleManagementClient.invokeAspect(path, "ServiceLifeCycle", "itemClick", items);
        } else {
            lifeCycleManagementClient.invokeAspect(path, "ServiceLifeCycle", "Promote", items);
            lifeCycleManagementClient.invokeAspect(path, "ServiceLifeCycle", "Promote", items);
        }

    }

    /**
     *This method add anonymous view permission to resources, its dependencies and dependants.
     *
     * @param resourceServiceClient     client used to call the admin service.
     * @param artifact                  generic artifact which permission should b allowed
     * @throws Exception
     */
    private static void addAnonymousViewToAssets(ResourceServiceClient resourceServiceClient, GenericArtifact artifact)
            throws Exception {
        String path = governancePath + artifact.getPath();
        resourceServiceClient.addRolePermission(path, "system/wso2.anonymous.role", "2", "1");

        for (GovernanceArtifact dependency : artifact.getDependencies()) {
            String dependencyPath = governancePath + dependency.getPath();
            resourceServiceClient.addRolePermission(dependencyPath, "system/wso2.anonymous.role", "2", "1");
        }
    }

    /**
     *This method create publisher and store user.
     *
     * @param configContext
     * @throws Exception
     */
    private static void addUsers(ConfigurationContext configContext) throws Exception {
        UserManagementClient userManager = new UserManagementClient(cookie, serverURL, configContext);
        String publisherUser = "Tom";
        String publisherUserPassword = "publisher@123";
        String[] publisherRole = { "internal/publisher" };
        userManager.addUser(publisherUser, publisherUserPassword, publisherRole, new ClaimValue[0], null);

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("Publisher Role created. Log in with following credentials to view added assets");
        System.out.println("User Name : Tom");
        System.out.println("Password : publisher@123");

        String storeUserName = "Jerry";
        String storeUserPassword = "store@123";
        String[] storeRole = { "internal/store" };
        userManager.addUser(storeUserName, storeUserPassword, storeRole, new ClaimValue[0], null);

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("Store Role created. Log in with following credentials to view added assets");
        System.out.println("User Name : Jerry");
        System.out.println("Password : store@123");

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("Or Login as admin to both Publisher and Store to view added assets ");
        System.out.println("User Name : admin");
        System.out.println("Password : admin");

    }
}
