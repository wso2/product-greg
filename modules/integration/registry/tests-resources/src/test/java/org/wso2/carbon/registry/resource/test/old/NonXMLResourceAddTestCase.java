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
package org.wso2.carbon.registry.resource.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry non xml resource add operation
 */

public class NonXMLResourceAddTestCase {
    private static final Log log = LogFactory.getLog(NonXMLResourceAddTestCase.class);

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserInfo userInfo;

    private static final String PARENT_PATH = "/_system/config/";
    private static final String RES_FILE_FOLDER = "TextFiles";
    private static final String TEXT_FILE_NAME = "sampleText.txt";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Non-XML Resource Tests");
        log.debug("Add Non-XML Resource Test Initialised");


        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        userInfo = UserListCsvReader.getUserInfo(userId);


        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = {"wso2.greg"})
    public void testAddNoneXmlResource()
            throws ResourceAdminServiceExceptionException, IOException {


        //add a collection to the registry
        String collectionPath =
                resourceAdminServiceClient.addCollection(PARENT_PATH, RES_FILE_FOLDER, "",
                                                         "contains Text Res Files");
        String authorUserName =
                resourceAdminServiceClient.getResource(
                        (PARENT_PATH + RES_FILE_FOLDER))[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + RES_FILE_FOLDER + " creation failure");
        log.info("collection added to " + collectionPath);

        // Changing media type
        collectionPath =
                resourceAdminServiceClient.addCollection(
                        PARENT_PATH, RES_FILE_FOLDER,
                        "application/vnd.wso2.esb",
                        "application/vnd.wso2.esb media type collection");
        authorUserName =
                resourceAdminServiceClient.getResource(
                        (PARENT_PATH + RES_FILE_FOLDER))[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + RES_FILE_FOLDER + " updating failure");
        log.info("collection updated in " + collectionPath);

        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                          "artifacts" + File.separator
                          + "GREG" + File.separator + "txt" + File.separator + "sampleText.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resource));


        resourceAdminServiceClient.addResource(PARENT_PATH + RES_FILE_FOLDER + "/" + TEXT_FILE_NAME,
                                               "text/html", "txtDesc",
                                               dh);

        String textContent =
                resourceAdminServiceClient.getTextContent(PARENT_PATH + RES_FILE_FOLDER + "/" +
                                                          TEXT_FILE_NAME);

        assertTrue(dh.getContent().toString().equalsIgnoreCase(textContent),
                   "Text file has not been added properly ");

        resourceAdminServiceClient.deleteResource(PARENT_PATH + RES_FILE_FOLDER);

    }

    //cleanup code
    @AfterClass
    public void cleanup()
            throws Exception {
        resourceAdminServiceClient.deleteResource("/TestAutomation/"+RES_FILE_FOLDER);
        resourceAdminServiceClient=null;
        userInfo=null;
    }

}
