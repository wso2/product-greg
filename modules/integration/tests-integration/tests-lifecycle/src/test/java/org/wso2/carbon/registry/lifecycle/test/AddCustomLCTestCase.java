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

package org.wso2.carbon.registry.lifecycle.test;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ActivityAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Calendar;

import static org.testng.Assert.*;

public class AddCustomLCTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private SearchAdminServiceClient searchAdminService;
    private final String ASPECT_NAME = "IntergalacticServiceLC";
    private String servicePathDev;
    private String userName1WithoutDomain;

    @BeforeClass (alwaysRun = true)
    public void init () throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        String sessionCookie = getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        activityAdminServiceClient = new ActivityAdminServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        searchAdminService = new SearchAdminServiceClient(backendURL, sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        String serviceName = "CustomLifeCycleTestService";
        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);
        Thread.sleep(3000);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@")) {
            userName1WithoutDomain = userName.substring(0, userName.indexOf('@'));
        } else{
            userName1WithoutDomain = userName;
        }

    }

    /**
     * @throws java.io.IOException
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Add new Life Cycle")
    public void createNewLifeCycle ()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
            SearchAdminServiceRegistryExceptionException {

        String filePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle Failed");
        Thread.sleep(6000);
        lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(lifeCycleConfiguration.contains("aspect name=\"IntergalacticServiceLC\""),
                "LifeCycleName Not Found in lifecycle configuration");
        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        assertNotNull(lifeCycleList);
        assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertTrue(found, "Life Cycle list not contain newly added life cycle");
        //Metadata Search By Life Cycle Name
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        Thread.sleep(60000);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList(), "No Record Found");
        assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle " +
                "Name or more record found");
        for (ResourceData resource : result.getResourceDataList()) {
            assertEquals(resource.getName(), ASPECT_NAME,
                    "Life Cycle Name mismatched :" + resource.getResourcePath());
            assertTrue(resource.getResourcePath().contains("lifecycles"),
                    "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Add LifeCycle to a service", dependsOnMethods = "createNewLifeCycle")
    public void addLifeCycleToService () throws Exception {

        wsRegistry.associateAspect(servicePathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on registry" + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        assertEquals(getLifeCycleState(lifeCycle), "Commencement",
                "LifeCycle State Mismatched");
        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), 
        		"registry.custom_lifecycle.checklist.option."+ ASPECT_NAME +".0.item")[1],
                "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), 
        		"registry.custom_lifecycle.checklist.option."+ ASPECT_NAME +".1.item")[1],
                "name:Document Requirements", "Document Requirements Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), 
        		"registry.custom_lifecycle.checklist.option."+ ASPECT_NAME +".2.item")[1],
                "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), 
        		"registry.custom_lifecycle.checklist.option."+ ASPECT_NAME +".3.item")[1],
                "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), 
        		"registry.custom_lifecycle.checklist.option."+ ASPECT_NAME +".4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), 
        		"registry.custom_lifecycle.checklist.option."+ ASPECT_NAME +".5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObj = activityAdminServiceClient.getActivities(getSessionCookie(),
                userName1WithoutDomain, servicePathDev, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ASSOCIATE_ASPECT, 1);
        assertNotNull(activityObj, "Activity object null for Associate Aspect");
        assertNotNull(activityObj.getActivity(), "Activity list object null for Associate Aspect");
        assertTrue((activityObj.getActivity().length > 0), "Activity list object null");
        String activity = activityObj.getActivity()[0];
        assertTrue(activity.contains(userName1WithoutDomain), "User name not found on activity last activity. " +
                activity);
        assertTrue(activity.contains("associated the aspect IntergalacticServiceLC"),
                "associated the aspect ServiceLifeCycle not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "current time not found on activity. " + activity);
    }

    /**
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test (groups = "wso2.greg", description = "delete LifeCycle when there is usage", dependsOnMethods = "addLifeCycleToService")
    public void deleteLifeCycleWhenHavingUsage ()
            throws LifeCycleManagementServiceExceptionException, RemoteException {

        assertTrue(lifeCycleManagementClient.isLifecycleNameInUse(ASPECT_NAME),
                "No Usage Found for Life Cycle");
        try {
            assertFalse(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                    "Life Cycle Deleted even if there is a usage");
            fail("Life Cycle Deleted even if there is a usage");
        } catch (AxisFault e) {
            assertEquals(e.getMessage(), "Lifecycle could not be deleted, since it is already in use!",
                    "Message mismatched");
        }
    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Delete used resources", dependsOnMethods = "deleteLifeCycleWhenHavingUsage")
    public void deleteResources ()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
            RemoteException, InterruptedException,
            SearchAdminServiceRegistryExceptionException {

        if (servicePathDev != null) {
            wsRegistry.delete(servicePathDev);
        }
        assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                "Life Cycle Deleted failed");
        Thread.sleep(2000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");

    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @AfterClass ()
    public void cleanup () throws RegistryException, LifeCycleManagementServiceExceptionException,
            RemoteException {

        if (wsRegistry.resourceExists(servicePathDev)) {
            wsRegistry.delete(servicePathDev);
        }
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
    }

    /**
     * @param lifeCycle LifeCycle to check the state
     * @return state of the lifeCycle
     */
    public static String getLifeCycleState (LifecycleBean lifeCycle) {

        assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.IntergalacticServiceLC.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }

}
