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

/**
 * This class used to test adding valid Swagger files in to the governance registry using resource-admin command.
 */
public class SwaggerAdditionTestCase extends GREGIntegrationBaseTest {
	private ResourceAdminServiceClient resourceAdminServiceClient;
	private String swaggerCommonPath = "/_system/governance/apimgt/applicationdata/api-docs/";
	private String restServiceCommonPath = "/_system/governance/trunk/restservices/";

	/**
	 * This method used to init the swagger addition test cases.
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
	 * Add swagger 2.0 resource from import URL functionality.
	 *
	 * @throws ResourceAdminServiceExceptionException
	 * @throws RemoteException
	 */
	@Test(groups = { "wso2.greg" }, description = "Add swagger 2.0 resource from import URL functionality.")
	public void testAddSwagger2FromURL() throws ResourceAdminServiceExceptionException, RemoteException {
		String resourceUrl = "http://petstore.swagger.io/v2/swagger.json";
		String resourceName = "swagger.json";
		resourceAdminServiceClient.addSwagger(resourceName, "adding From URL", resourceUrl);
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				swaggerCommonPath + "1.0.0/" + resourceName));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/" + resourceName));
	}

	/**
	 * Add swagger 1.2 resource from import URL functionality.
	 *
	 * @throws RemoteException
	 * @throws ResourceAdminServiceExceptionException
	 */
	@Test(groups = { "wso2.greg" }, description = "Add swagger 1.2 resource from import URL functionality.")
	public void testAddSwagger12FromURL() throws RemoteException, ResourceAdminServiceExceptionException {
		String resourceUrl = "http://petstore.swagger.io/api/api-docs";
		String resourceName = "api-docs.json";
		resourceAdminServiceClient.addSwagger(resourceName, "adding From URL", resourceUrl);
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				swaggerCommonPath + "1.0.0/" + resourceName));
		Assert.assertNotNull(
				resourceAdminServiceClient.getResourceContent(swaggerCommonPath + "1.0.0/pet"));
		Assert.assertNotNull(resourceAdminServiceClient
				                     .getResourceContent(swaggerCommonPath + "1.0.0/store"));
		Assert.assertNotNull(
				resourceAdminServiceClient.getResourceContent(swaggerCommonPath + "1.0.0/user"));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/" + resourceName));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/pet"));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/store"));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/user"));
	}

	/**
	 * Add swagger 2.0 resource from upload file functionality.
	 *
	 * @throws RemoteException
	 * @throws ResourceAdminServiceExceptionException
	 */
	@Test(groups = { "wso2.greg" }, description = " Add swagger 2.0 resource from upload file functionality.")
	public void testAddSwagger2FromFile() throws RemoteException, ResourceAdminServiceExceptionException {
		String fileName = "swagger2.json";
		String swaggerPath =
				FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator +
				"swagger" + File.separator + fileName;
		String msg = "Unexpected error adding the resource.";
		try {
			resourceAdminServiceClient.addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + swaggerPath)));
		} catch (ResourceAdminServiceExceptionException | RemoteException e) {
			log.error(msg, e);
			Assert.fail(msg);
		} catch (MalformedURLException e) {
			msg = "File url is malformed.";
			log.error(msg, e);
			Assert.fail(msg);
		}
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				swaggerCommonPath + "1.0.0/" + fileName));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/" + fileName));
	}

	/**
	 * Add swagger 1.2 resource from upload file functionality.
	 *
	 * @throws RemoteException
	 * @throws ResourceAdminServiceExceptionException
	 */
	@Test(groups = { "wso2.greg" }, description = "Add swagger 1.2 resource from upload file functionality.")
	public void testAddSwagger12FromFile() throws RemoteException, ResourceAdminServiceExceptionException {
		String fileName = "swagger12.json";
		String swaggerPath =
				FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator +
				"swagger" + File.separator + fileName;
		String msg = "Unexpected error adding the resource.";
		try {
			resourceAdminServiceClient.addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + swaggerPath)));
		} catch (ResourceAdminServiceExceptionException | RemoteException e) {
			log.error(msg, e);
			Assert.fail(msg);
		} catch (MalformedURLException e) {
			msg = "File url is malformed.";
			log.error(msg, e);
			Assert.fail(msg);
		}
		Assert.assertNotNull(resourceAdminServiceClient.getResourceContent(
				swaggerCommonPath + "1.0.0/" + fileName));
		Assert.assertTrue(deleteResource(swaggerCommonPath + "1.0.0/" + fileName));
	}

	/**
	 * Clean up method to remove created resources.
	 *
	 * @throws ResourceAdminServiceExceptionException
	 * @throws RemoteException
	 */
	@AfterClass(groups = { "wso2.greg" })
	public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
		deleteResource(restServiceCommonPath + "1.0.0/SwaggerPetstore");
		deleteResource(restServiceCommonPath + "2.1.1/SwaggerPetstore");
		deleteResource(restServiceCommonPath + "1.0.0/SwaggerSampleApp");
		//deleteResource("/_system/governance/trunk/endpoints/io/swagger/petstore/ep-api");
		deleteResource("/_system/governance/trunk/endpoints/ep-io.swagger.petstore-v2");
		resourceAdminServiceClient = null;
		swaggerCommonPath = null;
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
