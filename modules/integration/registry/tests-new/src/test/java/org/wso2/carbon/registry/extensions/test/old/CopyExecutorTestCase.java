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

package org.wso2.carbon.registry.extensions.test.old;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.*;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceRegistryExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class CopyExecutorTestCase {

    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private GenericServiceClient genericServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private static RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    private static final String ASPECT_NAME = "ESBLifeCycle2";
    private final String ACTION_PROMOTE = "Promote";

    private String servicePathTest;
    private String servicePathDev;


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        int userId = 1;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        listMetaDataServiceClient = new ListMetaDataServiceClient(environment.getGreg().
                getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        lifeCycleAdminService = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        genericServiceClient = new GenericServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        listMetaDataServiceClient = new ListMetaDataServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg()
                .getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg()
                .getBackEndUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(Integer.parseInt(userInfo.getUserId()),
                ProductConstant.GREG_SERVER_NAME);
        addService();

    }

    /**
     * Add a Life Cycle
     *
     * @throws IOException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws InterruptedException
     * @throws SearchAdminServiceRegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add new Life Cycle")
    public void createNewLifeCycle()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
            SearchAdminServiceRegistryExceptionException {

        String filePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "copyExecutorLifeCycle2.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration));
        lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        Assert.assertTrue(lifeCycleConfiguration.contains("aspect name=\"ESBLifeCycle2\""),
                "LifeCycleName Not Found in lifecycle configuration");

        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        Assert.assertNotNull(lifeCycleList);
        Assert.assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "Life Cycle list not contain newly added life cycle");

        //Metadata Search By Life Cycle Name
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle " +
                "Name or more record found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), ASPECT_NAME,
                    "Life Cycle Name mismatched :" + resource.getResourcePath());
            Assert.assertTrue(resource.getResourcePath().contains("lifecycles"),
                    "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    /**
     * add a life cycle to a service
     *
     * @throws RegistryException
     * @throws InterruptedException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add LifeCycle to a service", dependsOnMethods = {"createNewLifeCycle"})
    public void addLifeCycleToService() throws RegistryException, InterruptedException,
            CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
            RegistryExceptionException, ResourceAdminServiceExceptionException {

        ServiceBean sBean = listMetaDataServiceClient.listServices(null);
        for (String services : sBean.getPath()) {
            if (services.contains("IntergalacticService"))
                servicePathDev = "/_system/governance" + services;
        }
        wsRegistryServiceClient.associateAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = wsRegistryServiceClient.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Development",
                "LifeCycle State Mismatched");
    }

    /**
     * Promote service to Testing
     *
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     * @throws InterruptedException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote service to Testing", dependsOnMethods = {"addLifeCycleToService"})
    public void promoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
            RegistryException, InterruptedException, ResourceAdminServiceExceptionException {

        servicePathTest = "/_system/governance/wso2/branches/testing/services/com/abb/IntergalacticService";
        lifeCycleAdminService.invokeAspect(servicePathDev, ASPECT_NAME,
                ACTION_PROMOTE, null);
        Resource service = wsRegistryServiceClient.get(servicePathTest);
        Assert.assertNotNull(service, "Resource is not copied " + servicePathTest);

    }

    /**
     * delete added resources
     *
     * @throws RegistryException
     * @throws InterruptedException
     * @throws SearchAdminServiceRegistryExceptionException
     *
     * @throws RemoteException
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Delete resources", dependsOnMethods = {"promoteToTesting"})
    public void testDeleteResources() throws RegistryException, InterruptedException, SearchAdminServiceRegistryExceptionException, RemoteException, LifeCycleManagementServiceExceptionException {

        if (servicePathDev != null)
            wsRegistryServiceClient.delete(servicePathDev);

        if (servicePathTest != null)
            wsRegistryServiceClient.delete(servicePathTest);

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        ResourceData[] resultPaths = result.getResourceDataList();
        for (ResourceData resultPath : resultPaths) {
            if (resultPath.getResourcePath().contains(servicePathDev)
                    || resultPath.getResourcePath().contains(servicePathTest)) {
                Assert.fail("Life Cycle Record Found even if it is deleted");
            }
        }
        lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME);

    }

    @AfterClass(alwaysRun = true)
    public void deleteLifeCycle() throws RegistryException, LifeCycleManagementServiceExceptionException, RemoteException, ResourceAdminServiceExceptionException {
        if (wsRegistryServiceClient.resourceExists(servicePathDev))
            wsRegistryServiceClient.delete(servicePathDev);

        if (wsRegistryServiceClient.resourceExists(servicePathTest))
            wsRegistryServiceClient.delete(servicePathTest);

        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(ASPECT_NAME)) {
                lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME);
                break;
            }
        }

        SchemaBean schema = listMetaDataServiceClient.listSchemas();
        String schemaPathToDelete = "/_system/governance/" + schema.getPath()[0];
        wsRegistryServiceClient.delete(schemaPathToDelete);
        WSDLBean wsdl = listMetaDataServiceClient.listWSDLs();
        String wsdlPathToDelete = "/_system/governance/" + wsdl.getPath()[0];
        wsRegistryServiceClient.delete(wsdlPathToDelete);
    }

    /**
     * add a service
     *
     * @throws IOException
     * @throws XMLStreamException
     * @throws ManageGenericArtifactServiceRegistryExceptionException
     *
     */
    private void addService() throws IOException, XMLStreamException, ManageGenericArtifactServiceRegistryExceptionException {
        String servicePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                        File.separator + "GREG" + File.separator + "services" +
                        File.separator + "intergalacticService.metadata.xml";
        String serviceContent = FileManager.readFile(servicePath);
        OMElement service = AXIOMUtil.stringToOM(serviceContent);
        genericServiceClient.addArtifact(service, "service", null);

    }


    /**
     * return the state of a life cycle
     *
     * @param lifeCycle
     * @return state
     * @throws InterruptedException
     */
    public static String getLifeCycleState(LifecycleBean lifeCycle) throws InterruptedException {
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        Property[] props = lifeCycle.getLifecycleProperties();
        for (Property prop : props) {
            if ("registry.lifecycle.ESBLifeCycle2.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];
                break;
            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }
}
