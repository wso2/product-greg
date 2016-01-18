package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.samples.populator.utils.UserManagementClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.net.URL;
import javax.activation.DataHandler;

/*
* This class is used to re deploy the existing soap and rest service rxts which were there before adding
* categorization field.
 */
public class RXTReDeploy {

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    private static String cookie;
    private static final String username = "admin";
    private static final String password = "admin";
    private static String port ;
    private static String host ;
    private static String serverURL;
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

            Registry registry = new WSRegistryServiceClient(serverURL, username, password, configContext) {

                public void setCookie(String cookie) {
                    RXTReDeploy.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie, serverURL, configContext);
            String restServiceRxtPath = serviceRxtPath + "restservice.rxt";
            resourceServiceClient.delete(restServiceRxtPath);
            DataHandler dh1 = new DataHandler(new URL("file://" + projectPath + "/resources/restserviceExisting.rxt"));
            resourceServiceClient.addResource(restServiceRxtPath, RXT_MEDIA_TYPE, null, dh1, null, null);
            Thread.sleep(5 * 1000);
            System.out.println("Successfully re deployed Rest Service RXT");

            String soapServiceRxtPath = serviceRxtPath + "soapservice.rxt";
            resourceServiceClient.delete(soapServiceRxtPath);
            DataHandler dh2 = new DataHandler(new URL("file://" + projectPath + "/resources/soapserviceExisting.rxt"));
            resourceServiceClient.addResource(soapServiceRxtPath, RXT_MEDIA_TYPE, null, dh2, null, null);
            Thread.sleep(5 * 1000);
            System.out.println("Successfully re deployed Soap Service RXT");

            deleteUsers("Tom", configContext);
            deleteUsers("Jerry", configContext);

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     *This method is used to delete a particular user
     *
     * @param userName
     * @param configContext
     * @throws Exception
     */
    private static void deleteUsers(String userName, ConfigurationContext configContext) throws Exception{
        UserManagementClient userManager =
                new UserManagementClient(cookie, serverURL, configContext);
        userManager.deleteUser(userName);
    }

}
