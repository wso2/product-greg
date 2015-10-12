package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

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

            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie,
                    serverURL, configContext);
            String restServiceRxtPath = serviceRxtPath + "restservice.rxt";
            resourceServiceClient.delete(restServiceRxtPath);
            DataHandler dh1 = new DataHandler(new URL("file://"+projectPath+"/resources/restservice.rxt"));
            resourceServiceClient.addResource(restServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh1, null, null);
            Thread.sleep(5 * 1000);

            String soapServiceRxtPath = serviceRxtPath + "soapservice.rxt";
            resourceServiceClient.delete(soapServiceRxtPath);
            DataHandler dh2 = new DataHandler(new URL("file://"+projectPath+"/resources/soapservice.rxt"));
            resourceServiceClient.addResource(soapServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh2, null, null);
            Thread.sleep(5 * 1000);

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }
}
