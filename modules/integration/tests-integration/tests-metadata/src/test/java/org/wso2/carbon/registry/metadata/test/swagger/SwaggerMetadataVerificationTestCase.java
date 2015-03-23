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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class SwaggerMetadataVerificationTestCase extends GREGIntegrationBaseTest{

	private String session;
	private WSRegistryServiceClient wsRegistry;
	private ResourceAdminServiceClient resourceAdminServiceClient;
	String swaggerPath;

	@BeforeClass(groups = "wso2.greg", alwaysRun = true)
	public void initialize() throws Exception {
		super.init(TestUserMode.SUPER_TENANT_ADMIN);
		session = new LoginLogoutClient(automationContext).login();
		RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
		wsRegistry = registryProviderUtil                 .getWSRegistry(automationContext);
		Registry governanceRegistry = registryProviderUtil
				.getGovernanceRegistry(wsRegistry, automationContext);

		resourceAdminServiceClient =
				new ResourceAdminServiceClient(backendURL, session);
		GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
	}

	@Test(groups = {"wso2.greg"}, description = "Adding a swagger")
	public void testAddSwagger() {
		String fileName = "swagger2.json";
		swaggerPath =
				FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator +
				"swagger" + File.separator + fileName;
		String msg = "Unexpected error adding the resource.";
		try {
			resourceAdminServiceClient.addSwagger("Adding a swagger 2.0 definition from file. ",
			                                      new DataHandler(new URL("file:///" + swaggerPath)));
		} catch (ResourceAdminServiceExceptionException | RemoteException e) {
			log.error(msg, e);
			Assert.fail(msg);
		} catch (MalformedURLException e) {
			msg = "File url is malformed.";
			log.error(msg, e);
			Assert.fail(msg);
		}
	}

	@Test(groups = {"wso2.greg"}, description = "metadata verification", dependsOnMethods = "testAddSwagger")
	public void testVerifyMetadata() throws RemoteException, LoginAuthenticationExceptionException, GovernanceException,
	                                        ResourceAdminServiceExceptionException {

		assertTrue(resourceAdminServiceClient.getMetadata(swaggerPath).getMediaType()
		                                     .contentEquals("application/swagger+json"));


	}
}
