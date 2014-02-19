/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.registry.metadata.test.wsdl.old;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class WSDLValidationTestCase {
    //private static final Log log = LogFactory.getLog(WSDLValidationTestCase.class);

    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);

        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);
    }

    @Test(groups = {"wso2.greg"})
    public void addWSDL() throws Exception {
        boolean isFound = false;

        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdls/sample.wsdl",
                                               RegistryConsts.APPLICATION_WSDL_XML, "txtDesc", new DataHandler(new URL("file:///" + resource)));
        resourceAdminServiceClient.importResource("/_system/governance/trunk/wsdls", "WeatherForecastService.wsdl",
                                                  RegistryConsts.APPLICATION_WSDL_XML, "txtDesc",
                                                  "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wsdl/WeatherForecastService.wsdl", null);
        ResourceTreeEntryBean searchFileOne = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool");
        ResourceTreeEntryBean searchFileTwo = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01");
        String[] resourceChildOne = searchFileOne.getChildren();
        String[] resourceChildTwo = searchFileTwo.getChildren();
        for (int childCount = 0; childCount <= resourceChildOne.length; childCount++) {
            if (resourceChildOne[childCount].equalsIgnoreCase("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
        for (int childCount = 0; childCount <= resourceChildTwo.length; childCount++) {
            if (resourceChildTwo[childCount].equalsIgnoreCase("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);

    }


    /**
     * wsdlValidationTestCase having two different of test-cases.adding wsdl from local file system and adding wsdl from global url.
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addWSDL"})
    public void wsdlValidationTestCase()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        PropertiesBean propertiesBean = propertiesAdminServiceClient.getProperty("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "yes");
        Property[] property = propertiesBean.getProperties();
        for (int i = 0; i <= property.length - 1; i++) {
            if (property[i].getKey().equalsIgnoreCase("WSDL Validation")) {
                assertTrue(property[i].getValue().equalsIgnoreCase("valid"), "WSDL validation not matched with expected result");
            }
        }

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "wsdlValidationTestCase")
    public void addPropertyTest()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        propertiesAdminServiceClient.setProperty("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "TestProperty", "sample-value");
        PropertiesBean propertiesBean = propertiesAdminServiceClient.getProperty("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "yes");
        Property[] property = propertiesBean.getProperties();
        for (int i = 0; i <= property.length - 1; i++) {
            if (property[i].getKey().equalsIgnoreCase("TestProperty")) {
                assertTrue(property[i].getValue().equalsIgnoreCase("sample-value"), "Newly added property not found");
            }
        }

    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {
        Endpoint[] endpoints = null;
        Endpoint[] endPointsOther = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            } else if (wsdl.getQName().getLocalPart().equals("WeatherForecastService.wsdl")) {
                endPointsOther = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }

        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl");

        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        for (Endpoint path : endPointsOther) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }

        for (Service service : serviceManager.getAllServices()) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            } else if (service.getQName().getLocalPart().equals("WeatherForecastService")) {
                serviceManager.removeService(service.getId());
            }
        }

        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/arrays/WeatherForecastService.svc.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/net/restfulwebservices/www/datacontracts/_2008/_01/WeatherForecastService1.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService2.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/WeatherForecastService3.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/faultcontracts/gotlservices/_2008/_01/WeatherForecastService4.xsd");


    }
}
