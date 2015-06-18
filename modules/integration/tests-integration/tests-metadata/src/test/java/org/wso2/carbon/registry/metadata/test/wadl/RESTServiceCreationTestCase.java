/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.metadata.test.wadl;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.rmi.RemoteException;

public class RESTServiceCreationTestCase extends GREGIntegrationBaseTest{
	private ResourceAdminServiceClient resourceAdminServiceClient;
	private String restServiceCommonPath = "/_system/governance/apimgt/applicationdata/provider/";

	/**
	 * This method used to init the rest service creation test cases.
	 *
	 * @throws Exception
	 */
	@BeforeClass(groups ="wso2.greg", alwaysRun = true)
	public void initialize() throws Exception {
		super.init(TestUserMode.SUPER_TENANT_ADMIN);
		String session = new LoginLogoutClient(automationContext).login();

		resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
	}

	/**
	 * Tests if the REST Service is created when imported a wadl using a URL.
	 *
	 * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
	 * @throws java.rmi.RemoteException
	 */
	@Test(groups = { "wso2.greg" }, description = "Tests if the REST Service is created when imported a wadl using a URL.")
	public void testRestServiceCreationFromWadlImport() throws ResourceAdminServiceExceptionException, RemoteException {
		String resourceUrl = "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wadl/SearchSearvice.wadl";
		String resourceName = "SearchSearvice.wadl";
		resourceAdminServiceClient.addWADL(resourceName, "adding From URL", resourceUrl);
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				"/_system/governance/trunk/wadls/net/java/dev/wadl/_2009/_02/1.0.0/" + resourceName));
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				restServiceCommonPath + "admin/SearchSearvice/1.0.0/SearchSearvice-rest_service"));
	}

	/**
	 * Clean up method.
	 */
	@AfterClass(groups = { "wso2.greg" })
	public void cleanup() throws RemoteException, ResourceAdminServiceExceptionException {
		deleteResource(restServiceCommonPath + "admin/SearchSearvice/1.0.0/SearchSearvice-rest_service");
		deleteResource("/_system/governance/trunk/endpoints/ep-com.yahoo.search.api.newssearchservice-V1");
		deleteResource("/_system/governance/trunk/wadls/net/java/dev/wadl/_2009/_02/1.0.0/SearchSearvice.wadl");
		resourceAdminServiceClient = null;
		restServiceCommonPath = null;
	}

	/**
	 * This method deletes a resource in a given path.
	 *
	 * @param resourcePath  resource path to delete.
	 * @return              True if successfully deleted and false if not.
	 * @throws java.rmi.RemoteException
	 * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
	 */
	private boolean deleteResource(String resourcePath) throws RemoteException, ResourceAdminServiceExceptionException {
		return resourceAdminServiceClient.deleteResource(resourcePath);
	}
}
