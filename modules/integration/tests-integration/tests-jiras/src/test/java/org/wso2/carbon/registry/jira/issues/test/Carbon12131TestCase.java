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

package org.wso2.carbon.registry.jira.issues.test;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class Carbon12131TestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;

    private static final String PATH = "/c4/";
    private static final String RES_NAME = "testImage.JPEG";
    private static final String RES_DESC = "An image which has a capital letter extension";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        resourceAdminClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

    }

    @Test
    public void testResourceMediaType()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {

        String path = getTestArtifactLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "other" + File.separator + "wso2.JPG";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        String fileType = "image/jpeg";
        resourceAdminClient.addResource(PATH + RES_NAME, fileType, RES_DESC, dataHandler);

        String mediaType = resourceAdminClient.getMetadata(PATH + RES_NAME).getMediaType();
        assertTrue(fileType.equalsIgnoreCase(mediaType), "Media types of resources does not match");

    }

    @AfterClass()
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminClient.deleteResource(PATH);

        resourceAdminClient = null;
    }
}
