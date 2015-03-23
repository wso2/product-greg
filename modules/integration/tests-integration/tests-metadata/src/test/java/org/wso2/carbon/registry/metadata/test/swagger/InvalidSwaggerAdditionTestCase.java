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
 * This class contains test cases to test adding invalid swagger documents to GREG.
 */
public class InvalidSwaggerAdditionTestCase extends GREGIntegrationBaseTest{
	private ResourceAdminServiceClient resourceAdminServiceClient;

	/**
	 * This method used to init the swagger addition test cases.
	 *
	 * @throws Exception
	 */
	@BeforeClass(groups = "wso2.greg", alwaysRun = true)
	public void initialize() throws Exception {
		super.init(TestUserMode.SUPER_TENANT_ADMIN);
		String session = new LoginLogoutClient(automationContext).login();

		resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
	}

	/**
	 * Adding a swagger document with invalid version.
	 */
	@Test(groups = {"wso2.greg"}, description = "Adding a swagger with unsupported version")
	public void testAddUnsupportedSwaggerVersion() {
		String fileName = "swagger3.json";
		String swaggerPath =
				FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator +
				"swagger" + File.separator + fileName;
		try {
			resourceAdminServiceClient.addSwagger("Adding a swagger document with unsupported version. ",
			                                      new DataHandler(new URL("file:///" + swaggerPath)));
		} catch (RemoteException | ResourceAdminServiceExceptionException e) {
			Assert.assertTrue(e.getMessage().contains("Unsupported swagger version."));
		} catch (MalformedURLException e) {
			String msg = "File url is malformed. ";
			log.error(msg, e);
			Assert.fail(msg);
		}
	}

	/**
	 * Adding an empty swagger document.
	 */
	@Test(groups = {"wso2.greg"}, description = "Adding an empty swagger")
	public void testAddEmptySwagger() throws RemoteException, ResourceAdminServiceExceptionException {
		String fileName = "emptySwagger.json";
		String swaggerPath =
				FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator +
				"swagger" + File.separator + fileName;
		try {
			resourceAdminServiceClient.addSwagger("Adding an empty swagger file. ",
			                                      new DataHandler(new URL("file:///" + swaggerPath)));
		} catch (RemoteException | ResourceAdminServiceExceptionException e) {
			Assert.assertTrue(e.getMessage().contains("Unsupported swagger version."));
		} catch (MalformedURLException e) {
			String msg = "File url is malformed. ";
			log.error(msg, e);
			Assert.fail(msg);
		}
	}

	/**
	 * Clean up method.
	 */
	@AfterClass(groups = { "wso2.greg" })
	public void cleanup() {
		resourceAdminServiceClient = null;
	}

}
