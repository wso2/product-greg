package org.wso2.carbon.registry.ws.client.sample;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SampleWSRegGovernanceClient {


    private static ConfigurationContext configContext = null;

    private static final String CARBON_HOME = ".." + File.separator + ".." + File.separator;
    private static final String axis2Repo = CARBON_HOME + File.separator + "repository" +
            File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf = ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private static final String username = "admin";
    private static final String password = "admin";
    private static final String serverURL = "https://localhost:9443/services/";
    private static Registry registry;
    private static long startingTime;
    private static long clientTimeoutInMilliSeconds = 100000;   //This can be use to set the client timeout

    public static void setSystemProperties() throws AxisFault {
        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator +
                "repository" + File.separator + "resources" + File.separator +
                "security" + File.separator +"wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);
    }

    private static WSRegistryServiceClient initialize() throws Exception {
        startingTime = System.currentTimeMillis();
        return new WSRegistryServiceClient(serverURL, username, password, clientTimeoutInMilliSeconds ,configContext);
    }

    public static void main(String[] args) throws Exception {
        try {
            setSystemProperties();
            registry = initialize();
            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);
            //marginal time is approx. maximum time for execution
            long marginalTime = 1000;

            ServiceManager serviceManager = new ServiceManager(gov);
            List<Service> failedServiceList = new LinkedList<Service>();
            for (int i=0; i< 100; i++) {

                //Following part can be use to add artifacts without session timeout interruption.
                if ((System.currentTimeMillis() - startingTime) >= (clientTimeoutInMilliSeconds - marginalTime)) {
                    System.out.println("Session timed out login again");
                    registry = initialize();
                }

                Service service = serviceManager.newService(
                        new QName("https://www.sample.com/sample"+ i, "Service" + i));
                try {
                    System.out.println("Adding service " + service.getQName().getLocalPart());
                    serviceManager.addService(service);
                } catch (Exception e) {
                    failedServiceList.add(service);
                    System.out.println("Service adding failed :" +
                            service.getQName().getLocalPart() + " due to " + e.getMessage());
                }
            }

            //Printing list of failed services
            System.out.println("-------Failed Service List------");
            for (Service service : failedServiceList) {
                System.out.println(service.getQName().getLocalPart());
            }
            System.out.println("Total failed : " + failedServiceList.size());

        } finally {
            PaginationContext.destroy();
            //Close the session (logging out)
            ((WSRegistryServiceClient)registry).logut();
            System.exit(0);
        }

    }
}
