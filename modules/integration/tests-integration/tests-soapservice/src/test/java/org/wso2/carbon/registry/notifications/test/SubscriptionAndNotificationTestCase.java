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
package org.wso2.carbon.registry.notifications.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.HumanTaskAdminClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.WorkItem;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.wso2.greg.integration.common.utils.subscription.WorkItemClient;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command in the purpose for
 * wsdl addition test cases.
 */
public class SubscriptionAndNotificationTestCase extends GREGIntegrationBaseTest {

    private GenericArtifactManager artifactManager;
    private InfoServiceAdminClient infoServiceAdminClient;
    private Registry governance;
    private String artifactId;
    private String backEndUrl;
    private String sessionCookie;

    /**
     * This method used to init the wsdl addition test cases.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        infoServiceAdminClient = new InfoServiceAdminClient(backEndUrl, sessionCookie);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance,
                                                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        artifactManager = new GenericArtifactManager(governance, "soapservice");

    }

    @Test(groups = { "wso2.greg" }, description = "create SOAP Service using GenericArtifact")
    public void createSOAPService() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("SOAPService1"));

        artifact.setAttribute("overview_name", "SOAPServiceNotification");
        artifact.setAttribute("overview_version", "5.0.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                     " Service name must be equal");

        artifactId = artifact.getId();

    }

    @Test(groups = { "wso2.greg" }, description = "try add SOAP service without version", dependsOnMethods = "createSOAPService")
    public void addSubscription() throws GovernanceException, RegistryException, RemoteException, InterruptedException {
        SubscriptionBean bean = testMgtConsoleResourceSubscription("/_system/governance"+artifactManager.getGenericArtifact(artifactId).getPath(), "PublisherResourceUpdated");

        assertTrue(bean.getSubscriptionInstances() != null);
    }

    @Test(groups = { "wso2.greg" }, description = "create SOAP Service using GenericArtifact")
    public void updateSOAPService() throws GovernanceException, InterruptedException {
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);

        artifact.setAttribute("overview_description", "New Description");

        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        Thread.sleep(5000);
        assertEquals(artifact.getAttribute("overview_description"),
                     receivedArtifact.getAttribute("overview_description"), " Service Description must be equal");

        artifactId = artifact.getId();

    }

    @Test(groups = { "wso2.greg" }, description = "try add SOAP service without version",
          dependsOnMethods = "updateSOAPService")
    public void getNotification() throws GovernanceException,InterruptedException, RemoteException, IllegalStateFault,
                                         IllegalAccessFault,IllegalArgumentFault {
        HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(backendURL, sessionCookie);
        boolean notiTag = false;
        Thread.sleep(2000);
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for(WorkItem workItem : workItems) {
            if(workItem.getPresentationSubject().toString().contains(artifactManager.getGenericArtifact(artifactId).getPath())){
                notiTag = true;
            }

        }
        workItems = null;
        Assert.assertTrue(notiTag);
        humanTaskAdminClient = null;
    }

    /**
     * This method act as the test case cleaning process after the wsdl test case.
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     */
    @AfterClass(groups = { "wso2.greg" })
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        artifactManager.removeGenericArtifact(artifactId);
        artifactManager = null;
        infoServiceAdminClient = null;


    }

    public SubscriptionBean testMgtConsoleResourceSubscription(String path, String updateType)
            throws RegistryException, RemoteException {
        return infoServiceAdminClient.subscribe(path, "work://admin", updateType, sessionCookie);
    }
}
