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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class SchemaRetentionVerificationTestCase {
    private ManageEnvironment environment;
    private int userId1 = 2;
    private UserInfo userInfo2;
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

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws LoginAuthenticationExceptionException,
                                    RemoteException, RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId1);
        environment = builder.build();

        registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(userId1, ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = registryProviderUtil.getGovernanceRegistry(
                wsRegistryServiceClient, userId1);
        schemaManager = new SchemaManager(governanceRegistry);
    }

    @Test(groups = "wso2.greg", description = "schema addition for retention Verification")
    public void testAddResourcesToVerifyRetention() throws RemoteException,
                                                           MalformedURLException,
                                                           ResourceAdminServiceExceptionException,
                                                           GovernanceException {


        schema = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                           + "platform-integration/"
                           + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/"
                           + "resources/artifacts/GREG/schema/books.xsd");

        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Kanarupan");
        schema.addAttribute("description", "for retention verification");
        schemaManager.addSchema(schema);
        schemaManager.updateSchema(schema);
        schemaPath = "/_system/governance" + schema.getPath();
    }

    @Test(groups = "wso2.greg", description = "Retention Verification", dependsOnMethods = "testAddResourcesToVerifyRetention")
    public void testFirstUserRetention() throws GovernanceException,
                                                RemoteException,
                                                PropertiesAdminServiceRegistryExceptionException,
                                                LogoutAuthenticationExceptionException {

        userInfo2 = UserListCsvReader.getUserInfo(userId1);
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

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
        new AuthenticatorClient(environment.getGreg().getBackEndUrl()).logOut();
    }

    /**
     * With SecondUser, couldn't access the artifact using
     * schemaManager.getSchema(path), used getAllSchemas()
     */

    @Test(groups = "wso2.greg", description = "Retention verificaiton: second user", dependsOnMethods = "testFirstUserRetention")
    public void testSecondUserRetention() throws Exception {
        int userId2 = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId2);
        environment = builder.build();
        UserInfo userInfo3 = UserListCsvReader.getUserInfo(userId2);

        propertiesAdminServiceClient = new PropertiesAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(userId2, ProductConstant.GREG_SERVER_NAME);
        Registry governanceRegistry1 = registryProviderUtil
                .getGovernanceRegistry(wsRegistryServiceClient, userId2);

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
        assertEquals(retentionBean.getUserName(), userInfo2.getUserNameWithoutDomain());

        assertFalse(retentionBean.getWriteLocked());
        assertTrue(retentionBean.getDeleteLocked());

    }

    @Test(groups = "wso2.greg", description = "second user deletion check: blocked by first user", expectedExceptions = GovernanceException.class, dependsOnMethods = "testSecondUserRetention")
    public void testSecondUserRetentionDeleteCheck() throws GovernanceException {
        schemaManager2.removeSchema(schemaAddedByFirstUser.getPath());
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws AxisFault, RegistryException {
        registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(userId1, ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = registryProviderUtil.getGovernanceRegistry(
                wsRegistryServiceClient, userId1);
        schemaManager = new SchemaManager(governanceRegistry);
        schemaManager.removeSchema(schema.getId());
        date = null;
        dateFormat = null;
        calendar = null;
        propertiesAdminServiceClient = null;
        schema = null;
        schemaManager = null;
        registryProviderUtil = null;
        environment = null;
        governanceRegistry = null;
        schemaManager2 = null;
        schemaAddedByFirstUser = null;

    }
}
