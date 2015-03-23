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

package org.wso2.carbon.registry.metadata.test.swagger;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RESTServiceCreationTestCase extends GREGIntegrationBaseTest{
	private ResourceAdminServiceClient resourceAdminServiceClient;
	private String swaggerCommonPath = "/_system/governance/apimgt/applicationdata/api-docs/";
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
	 * Tests if the REST Service is created when imported a swagger using a URL.
	 *
	 * @throws ResourceAdminServiceExceptionException
	 * @throws RemoteException
	 */
	@Test(groups = { "wso2.greg" }, description = "Tests if the REST Service is created when imported a swagger using a URL.")
	public void testRestServiceCreationFromSwaggerImport() throws ResourceAdminServiceExceptionException, RemoteException {
		String resourceUrl = "http://petstore.swagger.io/api/api-docs";
		String resourceName = "swagger.json";
		resourceAdminServiceClient.addSwagger(resourceName, "adding From URL", resourceUrl);
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				swaggerCommonPath + "admin/SwaggerSampleApp/1.0.0/" + resourceName));
		Assert.assertNotNull(restServiceCommonPath + "admin/SwaggerSampleApp/1.0.0/SwaggerSampleApp-rest_service");
	}

	/**
	 * Tests if the REST Service is created when imported a swagger from a file.
	 *
	 * @throws ResourceAdminServiceExceptionException
	 * @throws RemoteException
	 * @throws java.net.MalformedURLException
	 */
	@Test(groups = { "wso2.greg" }, description = "Tests if the REST Service is created when imported a swagger from a file.")
	public void testRestServiceCreationFromSwaggerImportFromFile()
			throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
		String fileName = "swagger2.json";
		String swaggerPath =
				FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator +
				"swagger" + File.separator + fileName;
		resourceAdminServiceClient.addSwagger("Adding a swagger 2.0 definition from file. ",
		                                      new DataHandler(new URL("file:///" + swaggerPath)));

		Assert.assertNotNull(resourceAdminServiceClient
				                     .getResourceContent(swaggerCommonPath + "admin/SwaggerPetstore/1.0.0/" + fileName));
		Assert.assertNotNull(restServiceCommonPath + "admin/SwaggerPetstore/2.1.1/SwaggerPetstore-rest_service");
	}

	/**
	 * Clean up method.
	 */
	@AfterClass(groups = { "wso2.greg" })
	public void cleanup() throws RemoteException, ResourceAdminServiceExceptionException {
		deleteResource(restServiceCommonPath + "admin/SwaggerPetstore/2.1.1/SwaggerPetstore-rest_service");
		deleteResource(restServiceCommonPath + "admin/SwaggerSampleApp/1.0.0/SwaggerSampleApp-rest_service");
		deleteResource(swaggerCommonPath + "admin/SwaggerSampleApp/1.0.0/swagger.json");
		deleteResource(swaggerCommonPath + "admin/SwaggerSampleApp/1.0.0/pet");
		deleteResource(swaggerCommonPath + "admin/SwaggerSampleApp/1.0.0/store");
		deleteResource(swaggerCommonPath + "admin/SwaggerSampleApp/1.0.0/user");
		deleteResource(swaggerCommonPath + "admin/SwaggerPetstore/1.0.0/swagger2.json");
		resourceAdminServiceClient = null;
		swaggerCommonPath = null;
		restServiceCommonPath = null;
	}

	/**
	 * This method deletes a resource in a given path.
	 *
	 * @param resourcePath  resource path to delete.
	 * @return              True if successfully deleted and false if not.
	 * @throws RemoteException
	 * @throws ResourceAdminServiceExceptionException
	 */
	private boolean deleteResource(String resourcePath) throws RemoteException, ResourceAdminServiceExceptionException {
		return resourceAdminServiceClient.deleteResource(resourcePath);
	}
}
