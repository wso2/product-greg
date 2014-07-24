/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.lifecycle.test.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LifeCycleUtils {

    public static String addService(String nameSpace, String serviceName, Registry governance)
            throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;

        String content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">" +
                "<overview><name>" + serviceName + "</name><namespace>" + nameSpace +
                "</namespace><version>1.0.0-SNAPSHOT</version></overview>" +
                "</serviceMetaData>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);

        service = serviceManager.newService(XMLContent);
        serviceManager.addService(service);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName) && service.getPath().contains("trunk")) {

                return service.getPath();
            }

        }
        throw new Exception("Getting Service path failed");

    }

    //overloading method
    public static String addService(String nameSpace, String serviceName, String version,
                                    Registry governance)
            throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        service.addAttribute("overview_version", version);
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName) && service.getPath().contains("trunk")) {

                return service.getPath();
            }

        }
        throw new Exception("Getting Service path failed");

    }


    public static String[] getLifeCycleProperty(Property[] properties, String key) {
        Assert.assertTrue((properties.length > 0), "LifeCycle properties missing some properties");
        String[] values = null;
        boolean propertyFound = false;
        for (Property prop : properties) {
            if (key.equalsIgnoreCase(prop.getKey())) {
                propertyFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                values = prop.getValues();

            }
        }
        Assert.assertTrue(propertyFound, key + " property not found");
        return values;
    }

    public static String getLifeCycleState(LifecycleBean lifeCycle) {
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.ServiceLifeCycle.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }


    public static void createNewLifeCycle(String lifeCycleName
            , LifeCycleManagementClient lifeCycleManagerAdminService)
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                          File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                          "customLifeCycle.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath).replace("IntergalacticServiceLC", lifeCycleName);
        Assert.assertTrue(lifeCycleManagerAdminService.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagerAdminService.getLifecycleConfiguration(lifeCycleName);
        Assert.assertTrue(lifeCycleConfiguration.contains("aspect name=\"" + lifeCycleName + "\""),
                          "LifeCycleName Not Found in lifecycle configuration");

        String[] lifeCycleList = lifeCycleManagerAdminService.getLifecycleList();
        Assert.assertNotNull(lifeCycleList);
        Assert.assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (lifeCycleName.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "Life Cycle list does not contain newly added life cycle");

    }

    public static String addPolicy(String policyName, Registry governance)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                                File.separator + "GREG" + File.separator + "policy" + File.separator + "UTPolicy.xml";
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath).getBytes(), policyName);
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        return policy.getPath();

    }

    public static String updatePolicy(String policyName, Registry governance)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                                File.separator + "GREG" + File.separator + "policy" + File.separator + "UTPolicy.xml";
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath).getBytes(), policyName);
        policyManager.updatePolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        return policy.getPath();

    }

    public static String addWSDL(String name, Registry governance)
            throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);

        String wsdlFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        Wsdl wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), name);
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());

        return wsdl.getPath();
    }

    public static String addWSDL(String name, Registry governance, String serviceName)
            throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        String wsdlFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        Wsdl wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath)
                                                .replaceFirst("wsdl:service name=\"echoyuSer1\"",
                                                              "wsdl:service name=\"" + serviceName + "\"").getBytes(), name);
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());

        return wsdl.getPath();
    }

    public static String addSchema(String name, Registry governance)
            throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                                File.separator + "GREG" + File.separator + "schema" + File.separator + "Person.xsd";
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath).getBytes(), name);
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        return schema.getPath();

    }

    public static void createLifeCycleMultiplePromoteDemote(String lifeCycleName
            , LifeCycleManagementClient lifeCycleManagerAdminService)
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                          File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                          "MultiplePromoteDemoteLCViewVersionsTrue.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath).replace("DiffEnvironmentLC", lifeCycleName);
        Assert.assertTrue(lifeCycleManagerAdminService.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagerAdminService.getLifecycleConfiguration(lifeCycleName);
        Assert.assertTrue(lifeCycleConfiguration.contains("aspect name=\"" + lifeCycleName + "\""),
                          "LifeCycleName Not Found in lifecycle configuration");

        String[] lifeCycleList = lifeCycleManagerAdminService.getLifecycleList();
        Assert.assertNotNull(lifeCycleList);
        Assert.assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (lifeCycleName.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "Life Cycle list does not contain newly added life cycle");

    }

    public static void deleteLifeCycleIfExist(String lifeCycleName,
                                              LifeCycleManagementClient lifeCycleManagerAdminService)
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        String[] lifeCycleList = lifeCycleManagerAdminService.getLifecycleList();
        if (lifeCycleList != null && lifeCycleList.length > 0) {
            for (String lc : lifeCycleList) {
                if (lifeCycleName.equalsIgnoreCase(lc)) {
                    lifeCycleManagerAdminService.deleteLifeCycle(lifeCycleName);
                }
            }
        }
    }

    public static void deleteLcUsageResources(SearchAdminServiceClient searchAdminServiceClient,
                                              WSRegistryServiceClient wsRegistry, String LCName)
            throws SearchAdminServiceRegistryExceptionException,
                   RemoteException, RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("registry.LC.name");

        paramBean.setRightPropertyValue(LCName);
        paramBean.setRightOperator("eq");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        if (result != null) {
            if (result.getResourceDataList() != null) {
                for (ResourceData resource : result.getResourceDataList()) {
                    wsRegistry.delete(resource.getResourcePath());
                }
            }
        }
    }

    public static String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }
}
