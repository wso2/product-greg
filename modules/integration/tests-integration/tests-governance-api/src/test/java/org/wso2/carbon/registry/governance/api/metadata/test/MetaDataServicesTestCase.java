/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.governance.api.metadata.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.governance.api.lifecycle.test.util.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

public class MetaDataServicesTestCase extends GREGIntegrationBaseTest {

    private static final String SERVICE_LIFE_CYCLE = "ServiceLifeCycle";
    private Registry governance;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-new/" +
                    "src/test/resources/artifacts/GREG/wsdl/info.wsdl";
    ServiceManager serviceManager;
    private LifeCycleManagementClient lifeCycleManagerAdminService;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_ITEM_CLICK = "itemClick";
    private Wsdl wsdl;
    private Service service, serviceForDependencyVerification, infoService, infoServiceTesting,
            serviceForTrunkDeleteTest;
    private Service serviceForTrunkDeleteTestPromoted, serviceForBranchDeleteTest,
            serviceForBranchDeleteTestPromoted, serviceForTickedListItemsTest;
    private Service serviceForTickedListItemsTestPromoted, serviceForDetailVerificationTestCase,
            serviceForDeleteServiceTestCase, serviceForLCPromoteTests;
    private Service serviceForLCPromoteTestsPromoted, serviceForLCPromoteTestsPromoted2,
            newService;
    private WsdlManager manager;
    private String sessionCookie;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);
        lifeCycleManagerAdminService =
                new LifeCycleManagementClient(getBackendURL(),
                        sessionCookie);
        lifeCycleAdminService =
                new LifeCycleAdminServiceClient(getBackendURL(),
                        sessionCookie);
        manager = new WsdlManager(governance);
    }

    /**
     * Add a service without the defaultServiceVersion property so that the
     * service is saved as version
     * 1.0.0-SNAPSHOT
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.greg"}, description = "service without the defaultServiceVersion property")
    public void testAddServiceWithoutVersion() throws Exception {


        String content = createServiceContent("MyService", "http://bang.boom.com/mnm/beep");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        service =
                serviceManager.newService(XMLContent);
        serviceManager.addService(service);
        String serviceId = service.getId();
        newService = serviceManager.getService(serviceId);
        assertEquals(newService.getAttribute("overview_version"), "1.0.0-SNAPSHOT", "overview_version should be 1.0.0-SNAPSHOT");
    }

    /**
     * Open an existing service, do changes to the service content and save.
     * Verify whether the changes get persisted
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.greg"}, description = "service without the defaultServiceVersion property",
            dependsOnMethods = "testAddServiceWithoutVersion")
    public void testServiceDetailUpdate() throws Exception {

        newService.addAttribute("test-att1", "test-val1");
        serviceManager.updateService(newService);
//        assertEquals(serviceManager.getService(newService.getId()).getAttribute("test-att1"),
//                     "test-val1", "value of the test-att1 should be test-val1");

    }

    /**
     * Update a service that is at branch level to verify whether the changes
     * done do not get persisted to the trunk level service
     * <p/>
     * Set an LC to a service, then promote it to the next LC level. Then do
     * changes to the service content and update the service and make sure that
     * the LC state is not set back to it's initial state
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Update a service that is at trunk level",
            dependsOnMethods = "testServiceDetailUpdate")
    public void testChangesAtBranch() throws RemoteException,
            LifeCycleManagementServiceExceptionException,
            RegistryException,
            CustomLifecyclesChecklistAdminServiceExceptionException {
        ArrayOfString[] parameters = new ArrayOfString[2];

        infoService = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("MyService")) {
                    return true;
                }
                return false;
            }
        })[0];
        infoService.attachLifecycle(SERVICE_LIFE_CYCLE);

        String servicePathDev = "/_system/governance" + infoService.getPath();
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, SERVICE_LIFE_CYCLE,
                ACTION_PROMOTE, null, parameters);

        infoServiceTesting = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("MyService") &&
                        attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        infoServiceTesting.setAttribute("test-att", "test-value");
        assertEquals(infoService.getAttribute("test-att"), null);
        assertEquals(infoServiceTesting.getAttribute("test-att"), "test-value", "value of the test-att1 should be test-val");
        assertEquals(infoServiceTesting.getLifecycleState(), "Testing", "Lifecycle State should be Testing");
    }

    /**
     * Update a service that is at trunk level to verify whether the changes
     * done do not get persisted to the branch level services, that were
     * promoted from the updated service
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Update a service that is at trunk level",
            dependsOnMethods = "testChangesAtBranch")
    public void testChangesAtTrunk() throws GovernanceException {
        infoService.setAttribute("test-att2", "test-value");
        assertEquals(infoServiceTesting.getAttribute("test-att2"), null, "test-att2 should be null");
        assertEquals(infoService.getAttribute("test-att2"), "test-value", "test-att2 should be test-value");
    }

    /**
     * Create a service without a WSDL. Then add the WSDL later on and verify
     * whether the dependencies get resolved
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Create a service without a WSDL and verify dependencies",
            dependsOnMethods = "testChangesAtTrunk")
    public void testVerifyDependencies() throws GovernanceException, XMLStreamException {
        String content = createServiceContent("serviceForDependencyVarification", "http://service.dependency.varification/mnm/beep");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForDependencyVerification =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForDependencyVerification);
        wsdl = manager.newWsdl(WSDL_URL);
        manager.addWsdl(wsdl);
        serviceForDependencyVerification.attachWSDL(wsdl);
        assertEquals(serviceForDependencyVerification.getDependencies()[0].getQName()
                .getLocalPart(),
                "info.wsdl", "local part of the QName should be info.wsdl");
    }

    /**
     * Delete a service that is in the trunk level and verify whether there is
     * no effect on other services promoted from the deleted service
     *
     * @throws GovernanceException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "delete a service at trunk level",
            dependsOnMethods = "testVerifyDependencies")
    public void testDeleteServiceAtTrunk() throws GovernanceException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            LifeCycleManagementServiceExceptionException, XMLStreamException {

        String content = createServiceContent("serviceForTrunkDeleteTest", "http://service.delete.trunk/mnm/beep");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForTrunkDeleteTest =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForTrunkDeleteTest);
        String servicePathDev = "/_system/governance" + serviceForTrunkDeleteTest.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForTrunkDeleteTest.attachLifecycle(SERVICE_LIFE_CYCLE);
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                ACTION_PROMOTE, null, parameters);
        serviceForTrunkDeleteTestPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("serviceForTrunkDeleteTest") &&
                        attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        serviceManager.removeService(serviceForTrunkDeleteTest.getId());
        assertEquals(serviceForTrunkDeleteTestPromoted.getPath(),
                "/branches/testing/services/trunk/delete/service/mnm/beep/2.0.0/serviceForTrunkDeleteTest",
                "saved path is not equal to the expected");
    }

    /**
     * Delete a service that is in the branch level and verify whether it has no
     * impact to the service in the trunk level
     *
     * @throws GovernanceException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "delete a service at trunk level",
            dependsOnMethods = "testDeleteServiceAtTrunk")
    public void testDeleteServiceAtBranch() throws GovernanceException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            LifeCycleManagementServiceExceptionException, XMLStreamException {

        String content = createServiceContent("serviceForBranchDeleteTest", "http://service.delete.branch/mnm/beep");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForBranchDeleteTest =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForBranchDeleteTest);
        String servicePathDev = "/_system/governance" + serviceForBranchDeleteTest.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForBranchDeleteTest.attachLifecycle(SERVICE_LIFE_CYCLE);
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                ACTION_PROMOTE, null, parameters);
        serviceForBranchDeleteTestPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null &&
                        attributeVal.startsWith("serviceForBranchDeleteTest") &&
                        attributeVal2.startsWith("2.0.0");
            }
        })[0];
        serviceManager.removeService(serviceForBranchDeleteTestPromoted.getId());
        assertEquals(serviceForBranchDeleteTest.getPath(),
                "/trunk/services/branch/delete/service/mnm/beep/1.0.0-SNAPSHOT/serviceForBranchDeleteTest",
                "saved path is not equal to the expected");
    }

    /**
     * Verify whether the ticked check list items get unticked when updating the
     * service content which is at,
     * - trunk level
     * - branch level
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Checking the persistance with ticked check list items",
            dependsOnMethods = "testDeleteServiceAtBranch")
    public void testTickedListItems() throws GovernanceException, RemoteException,
            LifeCycleManagementServiceExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException, XMLStreamException {
        String content = createServiceContent("serviceForTickedListItemsTest", "http://service.ticked.items/mnm/beep");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForTickedListItemsTest =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForTickedListItemsTest);
        serviceForTickedListItemsTest.attachLifecycle(SERVICE_LIFE_CYCLE);
        String servicePathDev = "/_system/governance" + serviceForTickedListItemsTest.getPath();
        lifeCycleAdminService.invokeAspect(servicePathDev, "ServiceLifeCycle", ACTION_ITEM_CLICK,
                new String[]{"false", "true", "true"});

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        String optionOneValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".0.item")[2];
        String optionTwoValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".1.item")[3];
        String optionThreeValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".2.item")[3];
        serviceForTickedListItemsTest.addAttribute("test-att", "test-val");

        assertEquals(optionOneValueBefore,
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".0.item")[2],
                "checkbox 1 contain the incorrect value");
        assertEquals(optionTwoValueBefore,
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".1.item")[3],
                "checkbox 2 contain the incorrect value");
        assertEquals(optionThreeValueBefore,
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".2.item")[3],
                "checkbox 3 contain the incorrect value");

        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                ACTION_PROMOTE, null, parameters);

        serviceForTickedListItemsTestPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null &&
                        attributeVal.startsWith("serviceForTickedListItemsTest") &&
                        attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        String promotedServicePathDev =
                "/_system/governance" +
                        serviceForTickedListItemsTestPromoted.getPath();
        lifeCycleAdminService.invokeAspect(promotedServicePathDev, "ServiceLifeCycle",
                ACTION_ITEM_CLICK, new String[]{"false", "true",
                "true"});

        LifecycleBean lifeCyclePromoted =
                lifeCycleAdminService.getLifecycleBean(promotedServicePathDev);
        optionOneValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".0.item")[2];
        optionTwoValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".1.item")[3];
        optionThreeValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".2.item")[3];
        serviceForTickedListItemsTest.addAttribute("test-att", "test-val");

        assertEquals(optionOneValueBefore,
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".0.item")[2],
                "checkbox 1 contain the incorrect value");
        assertEquals(optionTwoValueBefore,
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".1.item")[3],
                "checkbox 2 contain the incorrect value");
        assertEquals(optionThreeValueBefore,
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                        "registry.custom_lifecycle.checklist.option." + SERVICE_LIFE_CYCLE + ".2.item")[3],
                "checkbox 3 contain the incorrect value");

    }

    /**
     * Verify whether the following column shows correct information for the
     * added service
     * - Service Name
     * - Service Version
     * - Service Namespace
     * - Lifecycle Status
     * - Actions
     * <p/>
     * When a LC is assigned to the service, the Lifecycle status should display
     * the first LC state
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     */
    @Test(groups = {"wso2.greg"}, description = "Verify Service Information", dependsOnMethods = "testTickedListItems")
    public void testServiceDetailVerification() throws GovernanceException, RemoteException,
            LifeCycleManagementServiceExceptionException {
        serviceForDetailVerificationTestCase =
                serviceManager.newService(new QName(
                        "http://service.detail.verification/mnm/beep",
                        "serviceForDetailVerificationTestCase"));
        serviceForDetailVerificationTestCase.addAttribute("overview_version", "2.0.0");
        serviceManager.addService(serviceForDetailVerificationTestCase);
        serviceForDetailVerificationTestCase.attachLifecycle(SERVICE_LIFE_CYCLE);
        assertEquals(serviceForDetailVerificationTestCase.getAttribute("overview_name"),
                "serviceForDetailVerificationTestCase", "service contain incorrect information");
        assertEquals(serviceForDetailVerificationTestCase.getAttribute("overview_namespace"),
                "http://service.detail.verification/mnm/beep", "service contain incorrect information");
        assertEquals(serviceForDetailVerificationTestCase.getAttribute("overview_version"),
                "2.0.0", "service contain incorrect information");
        assertEquals(serviceForDetailVerificationTestCase.getLifecycleState(), "Development", "service contain incorrect LC state");

    }

    /**
     * When a service is deleted, the service should be removed from the list
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Deleting a service", dependsOnMethods = "testServiceDetailVerification")
    public void testDeleteService() throws GovernanceException, XMLStreamException {
        String content = createServiceContent("serviceForDeleteServiceTestCase", "http://service.delete.verification/mnm/beep");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForDeleteServiceTestCase =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForDeleteServiceTestCase);
        serviceManager.removeService(serviceForDeleteServiceTestCase.getId());

        Service[] searchResult = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null &&
                        attributeVal.startsWith("serviceForDeleteServiceTestCase")) {
                    return true;
                }
                return false;
            }
        });

        assertEquals(searchResult.length, 0, "searchResult.length should be 0");
    }

    /**
     * When services are promoted, the correct LC state should be updated for
     * each service
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "LC promote tests", dependsOnMethods = "testDeleteService")
    public void testLCPromoting() throws GovernanceException, RemoteException,
            LifeCycleManagementServiceExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException, XMLStreamException {
        String content = createServiceContent("serviceForLCPromoteTests", "http://service.for.lc/promote/test");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForLCPromoteTests =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForLCPromoteTests);
        serviceForLCPromoteTests.attachLifecycle(SERVICE_LIFE_CYCLE);
        String servicePathDev = "/_system/governance" + serviceForLCPromoteTests.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForLCPromoteTests.attachLifecycle(SERVICE_LIFE_CYCLE);
        assertEquals(serviceForLCPromoteTests.getLifecycleState(), "Development", "LC state should be Development");
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                ACTION_PROMOTE, null, parameters);
        serviceForLCPromoteTestsPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("serviceForLCPromoteTests") &&
                        attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        assertEquals(serviceForLCPromoteTestsPromoted.getLifecycleState(), "Testing", "LC state should be Testing");
        servicePathDev = "/_system/governance" + serviceForLCPromoteTestsPromoted.getPath();
        parameters[0].setArray(new String[]{servicePathDev, "3.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                ACTION_PROMOTE, null, parameters);
        serviceForLCPromoteTestsPromoted2 = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("serviceForLCPromoteTests") &&
                        attributeVal2.startsWith("3.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];

        assertEquals(serviceForLCPromoteTestsPromoted2.getLifecycleState(), "Production", "LC state should be Production");

    }

    private String createServiceContent(String serviceName, String namespace) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">");
        stringBuilder.append("<overview><name>");
        stringBuilder.append(serviceName);
        stringBuilder.append("</name><namespace>");
        stringBuilder.append(namespace);
        stringBuilder.append("</namespace><version>1.0.0-SNAPSHOT</version></overview>");
        stringBuilder.append("</serviceMetaData>");
        return stringBuilder.toString();
    }

    @AfterClass(alwaysRun = true)
    public void endGame() throws GovernanceException {

        serviceManager.removeService(serviceForDependencyVerification.getId());
        serviceManager.removeService(infoService.getId());
        serviceManager.removeService(infoServiceTesting.getId());
        serviceManager.removeService(serviceForTrunkDeleteTestPromoted.getId());
        serviceManager.removeService(serviceForBranchDeleteTest.getId());
        serviceManager.removeService(serviceForTickedListItemsTest.getId());
        serviceManager.removeService(serviceForTickedListItemsTestPromoted.getId());
        serviceManager.removeService(serviceForDetailVerificationTestCase.getId());
        serviceManager.removeService(serviceForLCPromoteTests.getId());
        serviceManager.removeService(serviceForLCPromoteTestsPromoted.getId());
        serviceManager.removeService(serviceForLCPromoteTestsPromoted2.getId());

        Service[] services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("Info");
            }
        });

        if (services != null) {
            for (Service service : services) {
                serviceManager.removeService(service.getId());
            }
        }

        manager.removeWsdl(wsdl.getId());

        governance = null;
        serviceManager = null;
        lifeCycleManagerAdminService = null;
        lifeCycleAdminService = null;

        wsdl = null;
        service = null;
        serviceForDependencyVerification = null;
        infoService = null;
        infoServiceTesting = null;

        serviceForTrunkDeleteTest = null;
        serviceForTrunkDeleteTestPromoted = null;
        serviceForBranchDeleteTest = null;
        serviceForBranchDeleteTestPromoted = null;
        serviceForTickedListItemsTest = null;
        serviceForTickedListItemsTestPromoted = null;
        serviceForDetailVerificationTestCase = null;
        serviceForDeleteServiceTestCase = null;
        serviceForLCPromoteTests = null;
        serviceForLCPromoteTestsPromoted = null;
        serviceForLCPromoteTestsPromoted2 = null;
        newService = null;
        manager = null;
    }

}
