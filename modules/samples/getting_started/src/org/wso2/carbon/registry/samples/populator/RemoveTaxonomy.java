package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.samples.populator.utils.PopulatorConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import javax.activation.DataHandler;

/*
* This class is used to back up existing rest and soap service rxt of the server, and add
* new rxts with categorization feild.
 */
public class RemoveTaxonomy {

    public static final String TAXO_MEDIA_TYPE = "application/taxo+xml";
    private static String cookie;
    private static final String username = PopulatorConstants.USERNAME;
    private static final String password = PopulatorConstants.PASSWORD;
    private static String port;
    private static String host;
    private static String serverURL;
    private static final String fileSeparator = File.separator + File.separator + File.separator;
    private static final String TAXONOMY_REG_PATH = "/_system/governance/repository/components/org.wso2.carbon.governance/taxonomy/";

    private static void setSystemProperties() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.getProperty("carbon.home")).append(File.separator).append("repository")
                .append(File.separator).append("resources").append(File.separator).append("security")
                .append(File.separator).append("wso2carbon.jks");
        String trustStore = builder.toString();
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
    }

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Started Removing Taxonomy XMLs");
            port = args[0];
            if (port == null || port.length() == 0) {
                port = "9443";
            }
            host = args[1];
            if (host == null || host.length() == 0) {
                host = "localhost";
            }
            serverURL = "https://" + host + ":" + port + "/services/";
            setSystemProperties();
            String projectPath = System.getProperty("user.dir");
            StringBuilder builder = new StringBuilder();
            builder.append(System.getProperty("carbon.home")).append(File.separator).append("repository")
                    .append(File.separator).append("conf").append(File.separator).append("axis2").append(File.separator)
                    .append("axis2_client.xml");
            String axis2Configuration = builder.toString();
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(serverURL, username, password, configContext) {

                public void setCookie(String cookie) {
                    RemoveTaxonomy.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie, serverURL, configContext);
            String servicesTaxoPath = TAXONOMY_REG_PATH + "Services";
            resourceServiceClient.delete(servicesTaxoPath);
            System.out.println("Successfully removed Services Taxonomy");

            String dataCentersTaxoPath = TAXONOMY_REG_PATH + "DataCenters";
            resourceServiceClient.delete(dataCentersTaxoPath);
            System.out.println("Successfully removed DataCenters Taxonomy");

            String teamsTaxoPath = TAXONOMY_REG_PATH + "Teams";
            resourceServiceClient.delete(teamsTaxoPath);
            System.out.println("Successfully removed Teams Taxonomy");

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }
}