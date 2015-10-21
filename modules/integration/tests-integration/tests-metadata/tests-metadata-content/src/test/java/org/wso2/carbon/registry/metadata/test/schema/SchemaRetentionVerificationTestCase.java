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

package org.wso2.carbon.registry.metadata.test.schema;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class SchemaRetentionVerificationTestCase extends GREGIntegrationBaseTest {

    private Schema schema;
    private Registry governanceRegistry;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private SchemaManager schemaManager;
    private RegistryProviderUtil registryProviderUtil;
    private SchemaManager schemaManager2;
    private String schemaPath;
    private SimpleDateFormat dateFormat;
    private Date date;
    private Calendar calendar;
    private Schema schemaAddedByFirstUser;
    private String sessionCookie;

    @BeforeClass (groups = "wso2.greg", alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        registryProviderUtil = new RegistryProviderUtil();
        sessionCookie = new LoginLogoutClient(automationContext).login();
        WSRegistryServiceClient wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
        governanceRegistry =
                registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        schemaManager = new SchemaManager(governanceRegistry);
    }

    @Test (groups = "wso2.greg", description = "schema addition for retention Verification")
    public void testAddResourcesToVerifyRetention () throws RemoteException,
            MalformedURLException,
            ResourceAdminServiceExceptionException,
            GovernanceException {

        schema = schemaManager
                .newSchema("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/" +
                        "greg/schema/books.xsd");
        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Kanarupan");
        schema.addAttribute("description", "for retention verification");
        schemaManager.addSchema(schema);
        schemaManager.updateSchema(schema);
        schemaPath = "/_system/governance" + schema.getPath();
    }

    @Test (groups = "wso2.greg", description = "Retention Verification",
            dependsOnMethods = "testAddResourcesToVerifyRetention")
    public void testFirstUserRetention () throws GovernanceException,
            RemoteException, PropertiesAdminServiceRegistryExceptionException, LogoutAuthenticationExceptionException {

        propertiesAdminServiceClient = new PropertiesAdminServiceClient(
                backendURL, sessionCookie);

        /* getting current date and date exactly after a month */
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = new Date();
        calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        propertiesAdminServiceClient.setRetentionProperties(schemaPath,
                "delete", dateFormat.format(date),
                dateFormat.format(calendar.getTime())); // delete
        // access
        // locked
        schemaManager.updateSchema(schema);
        RetentionBean retentionBean = propertiesAdminServiceClient
                .getRetentionProperties(schemaPath);
        assertTrue(retentionBean.getDeleteLocked());
        assertTrue(retentionBean.getFromDate().contentEquals(
                dateFormat.format(date)));
        assertFalse(retentionBean.getWriteLocked());

        /* logout */
        new AuthenticatorClient(backendURL).logOut();
    }

    /**
     * With SecondUser, couldn't access the artifact using
     * schemaManager.getSchema(path), used getAllSchemas()
     */
    @Test (groups = "wso2.greg", description = "Retention verificaiton: second user",
            dependsOnMethods = "testFirstUserRetention")
    public void testSecondUserRetention () throws Exception {

        AutomationContext automationContext1 = new AutomationContext("GREG", "greg001",
                "superTenant", "user2");
        String sessionCookieUser2 = new LoginLogoutClient(automationContext1).login();
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(
                automationContext1.getContextUrls().getBackEndUrl(),
                sessionCookieUser2);
        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(automationContext1);
        Registry governanceRegistry1 = registryProviderUtil
                .getGovernanceRegistry(wsRegistryServiceClient, automationContext1);
        schemaManager2 = new SchemaManager(governanceRegistry1);
        Schema[] schemas = schemaManager2.getAllSchemas();
        for (Schema tmpSchema : schemas) {
            if (tmpSchema.getQName().toString()
                    .contains("{urn:books}books.xsd")) {
                schemaAddedByFirstUser = tmpSchema;
            }
        }
        assertTrue(schemaAddedByFirstUser.getQName().toString()
                .contains("{urn:books}books.xsd"));
        assertTrue(schemaAddedByFirstUser.getAttribute("author").contains(
                "Kanarupan"));
        schemaAddedByFirstUser.addAttribute("WriteAccess", "enabled");
        schemaManager2.updateSchema(schemaAddedByFirstUser);
        assertTrue(schemaAddedByFirstUser.getAttribute("WriteAccess").contains(
                "enabled"));
        RetentionBean retentionBean = propertiesAdminServiceClient
                .getRetentionProperties(schemaPath);
        assertEquals(retentionBean.getFromDate(), dateFormat.format(date));
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        String userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        assertEquals(retentionBean.getUserName(), userNameWithoutDomain);
        assertFalse(retentionBean.getWriteLocked());
        assertTrue(retentionBean.getDeleteLocked());

    }

    @Test (groups = "wso2.greg", description = "second user deletion check: blocked by first user",
            expectedExceptions = GovernanceException.class, dependsOnMethods = "testSecondUserRetention")
    public void testSecondUserRetentionDeleteCheck () throws GovernanceException {

        schemaManager2.removeSchema(schemaAddedByFirstUser.getPath());
    }

    @AfterClass (groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown () throws Exception {

        registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(automationContext);
        governanceRegistry = registryProviderUtil.getGovernanceRegistry(
                wsRegistryServiceClient, automationContext);
        schemaManager = new SchemaManager(governanceRegistry);
        schemaManager.removeSchema(schema.getId());
        date = null;
        dateFormat = null;
        calendar = null;
        propertiesAdminServiceClient = null;
        schema = null;
        schemaManager = null;
        registryProviderUtil = null;
        governanceRegistry = null;
        schemaManager2 = null;
        schemaAddedByFirstUser = null;

    }
}
