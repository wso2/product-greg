package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.samples.populator.utils.PopulatorConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.governance.api.util.GovernanceUtils;

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
public class RXTModifier {
    private static String cookie;
    private static final String username = PopulatorConstants.username;
    private static final String password = PopulatorConstants.password;
    private static String port ;
    private static String host ;
    private static String serverURL;
    private static final String fileSeparator = File.separator + File.separator + File.separator;
    private static final String serviceRxtPath =
            "/_system/governance/repository/components/org.wso2.carbon.governance/types/";

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

    public static void main(String[] args) {
        try {
            port = args[0];
            if(port == null || port.length() ==0){
                port = "9443";
            }
            host =args [1];
            if(host == null || host.length() ==0){
                host = "localhost";
            }
            serverURL = "https://"+host+":"+port+"/services/";
            setSystemProperties();
            String projectPath = System.getProperty("user.dir");
            StringBuilder builder = new StringBuilder();
            builder.append(System.getProperty("carbon.home")).append(File.separator).append("repository")
                    .append(File.separator).append("conf").append(File.separator).append("axis2").append(File.separator)
                    .append("axis2_client.xml");
            String axis2Configuration = builder.toString();
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
            Registry govRegistry = GovernanceUtils.getGovernanceUserRegistry(registry, username);
            backUpRXTs(govRegistry, serviceRxtPath + "restservice.rxt", "restserviceExisting.rxt");
            backUpRXTs(govRegistry, serviceRxtPath + "soapservice.rxt", "soapserviceExisting.rxt");
            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie,
                    serverURL, configContext);
            String restServiceRxtPath = serviceRxtPath + "restservice.rxt";
            resourceServiceClient.delete(restServiceRxtPath);
            DataHandler dh1 = new DataHandler(new URL("file:" + fileSeparator + projectPath+ File
                    .separator + "resources" +File.separator + "restservice.rxt"));
            resourceServiceClient.addResource(restServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh1, null, null);
            //Thread.sleep(5 * 1000);
            System.out.println("Successfully added categorization field to Rest Service RXT");

            String soapServiceRxtPath = serviceRxtPath + "soapservice.rxt";
            resourceServiceClient.delete(soapServiceRxtPath);
            DataHandler dh2 = new DataHandler(new URL("file:" + fileSeparator + projectPath+ File
                    .separator + "resources" + File.separator + "soapservice.rxt"));
            resourceServiceClient.addResource(soapServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh2, null, null);
            Thread.sleep(3 * 1000);
            System.out.println("Successfully added categorization field to Soap Service RXT");

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     *This method is used to back up existing RXTs.
     *
     * @param registry      registry instance.
     * @param path          path of the rxt.
     * @param fileName      file name of backed up rxt files.
     * @throws RegistryException
     */
    private static void backUpRXTs(Registry registry, String path, String fileName) throws RegistryException{
        Resource resource = registry.get(path);
        try {
            RXTContentToFile(resource.getContentStream(), fileName);
        } catch (FileNotFoundException e){
            System.out.println("Could not read rxt content");
        }
    }

    /**
     *This method is used to write rxt content to text file.
     *
     * @param is        rxt content as a input stream
     * @param fileName  file name of backed up rxt file.
     * @throws FileNotFoundException
     */
    private static void RXTContentToFile(InputStream is, String fileName) throws FileNotFoundException {

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
        PrintWriter out = new PrintWriter("resources" + File.separator + fileName);
        out.println(sb.toString());
        out.flush();
        out.close();

    }
}
