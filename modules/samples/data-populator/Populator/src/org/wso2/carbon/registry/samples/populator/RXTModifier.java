package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.StringBuilder;

public class RXTModifier {
    private static String cookie;
    private static final String username = "admin";
    private static final String password = "admin";
    private static final String serverURL = "https://localhost:9443/services/";
    private static final String serviceRxtPath = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";

    private static void setSystemProperties() {
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
    }

    public static void main(String[] args) {
        try {

            setSystemProperties();
            String projectPath = System.getProperty("user.dir");
            String axis2Configuration = System.getProperty("carbon.home") + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(
                    serverURL, username,
                    password, configContext) {
                public void setCookie(String cookie) {
                    RXTModifier.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            backUpRXTs(registry,serviceRxtPath + "restservice.rxt", "restserviceExisting.rxt");
            backUpRXTs(registry,serviceRxtPath + "soapservice.rxt", "soapserviceExisting.rxt");
            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie,
                    serverURL, configContext);
            String restServiceRxtPath = serviceRxtPath + "restservice.rxt";
            resourceServiceClient.delete(restServiceRxtPath);
            DataHandler dh1 = new DataHandler(new URL("file://"+projectPath+"/resources/restservice.rxt"));
            resourceServiceClient.addResource(restServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh1, null, null);
            Thread.sleep(5 * 1000);
            System.out.println("Successfully added categorization field to Rest Service RXT");

            String soapServiceRxtPath = serviceRxtPath + "soapservice.rxt";
            resourceServiceClient.delete(soapServiceRxtPath);
            DataHandler dh2 = new DataHandler(new URL("file://"+projectPath+"/resources/soapservice.rxt"));
            resourceServiceClient.addResource(soapServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh2, null, null);
            Thread.sleep(5 * 1000);
            System.out.println("Successfully added categorization field to Soap Service RXT");

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void backUpRXTs(Registry registry, String path, String fileName) throws RegistryException{
        Resource resource = registry.get(path);
        try {
            RXTContentToFile(resource.getContentStream(), fileName);
        } catch (FileNotFoundException e){
            System.out.println("Could not read rxt content");
        }
    }

    private static void RXTContentToFile(InputStream is, String filename) throws FileNotFoundException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

        } catch (IOException e) {
            System.out.println("Could not read rxt content");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Could not close input stream");
                }
            }
        }
        PrintWriter out = new PrintWriter("resources/" + filename);
        out.println(sb.toString());
        out.flush();
        out.close();

    }
}
