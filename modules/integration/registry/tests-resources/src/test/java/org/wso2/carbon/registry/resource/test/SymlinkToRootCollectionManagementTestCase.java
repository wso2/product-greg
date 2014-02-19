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
package org.wso2.carbon.registry.resource.test;

import org.apache.abdera.model.AtomDate;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class SymlinkToRootCollectionManagementTestCase {

    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminClient;

    private static final String ROOT = "/";
    private static final String COLL_NAME = "rootTestFolder";
    private static final String SYMLINK_LOC = "/_system/";
    private static final String SYMLINK_NAME = "TestSymlink";
    private static final String SYMLINK_NAME_AFTER_RENAME = "TestSymlink";
    private static final String SYMLINK_NAME_AFTER_MOVING = "MovedTestSymlink";
    private static final String SYMLINK_MOVED_LOCATION = "/c1/c2/";
    private static final String SYMLINK_COPIED_LOCATION = "/c1/";
    private static final String SYMLINK_NAME_AFTER_COPYING = "CopiedTestSymlink";
    private static final String COLL_DESC = "A test collection";


    public static final String REGISTRY_NAMESPACE = "http://wso2.org/registry";

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {
        int userId = 2;
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
    }

    @Test(groups = "wso2.greg")
    public void testAddCollectionToRoot()
            throws ResourceAdminServiceExceptionException, RemoteException {

        String fileType = "other";
        resourceAdminClient.addCollection(ROOT, COLL_NAME, fileType, COLL_DESC);

        String authorUserName = resourceAdminClient.getResource(ROOT + COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Root collection creation failure");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddCollectionToRoot")
    public void testAddSymlinkToRootCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.addSymbolicLink(
                SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1), SYMLINK_NAME, ROOT + COLL_NAME);

        String authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Symlink creation failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddSymlinkToRootCollection", enabled = true)
    public void testFeed()
            throws ResourceAdminServiceExceptionException, IOException, XMLStreamException {

        ResourceData[] rData = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME);

        OMElement atomFeedOMElement = getAtomFeedContent(constructAtomUrl(SYMLINK_LOC + SYMLINK_NAME));

        assertNotNull(atomFeedOMElement, "No feed data available");

        //checking whether the created time is correct
        OMElement createdElement = atomFeedOMElement.getFirstChildWithName(
                new QName(REGISTRY_NAMESPACE, "createdTime"));

        assertTrue(createdElement.getText().equalsIgnoreCase(getAtomDateString(rData[0].getCreatedOn().getTime())),
                   "Symlink - Created time is incorrect");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testFeed")
    public void testRenameSymlink() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.renameResource(SYMLINK_LOC, SYMLINK_LOC + SYMLINK_NAME, SYMLINK_NAME_AFTER_RENAME);
        boolean found = false;
        ResourceData[] rData = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME_AFTER_RENAME);

        for (ResourceData resource : rData) {
            if (SYMLINK_NAME_AFTER_RENAME.equalsIgnoreCase(resource.getName())) {
                found = true;
            }
        }

        assertTrue(found, "Rename Symlink error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRenameSymlink", enabled = true)
    public void testFeedAfterRename()
            throws ResourceAdminServiceExceptionException, IOException, XMLStreamException {

        ResourceData[] rData = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME_AFTER_RENAME);

        OMElement atomFeedOMElement = getAtomFeedContent(constructAtomUrl(SYMLINK_LOC + SYMLINK_NAME_AFTER_RENAME));

        assertNotNull(atomFeedOMElement, "No feed data available");

        //checking whether the created time is correct
        OMElement createdElement = atomFeedOMElement.getFirstChildWithName(
                new QName(REGISTRY_NAMESPACE, "createdTime"));

        assertTrue(createdElement.getText().equalsIgnoreCase(getAtomDateString(rData[0].getCreatedOn().getTime())),
                   "Symlink - Created time is incorrect");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testFeedAfterRename")
    public void testCopySymlink() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.copyResource(SYMLINK_LOC, SYMLINK_LOC + SYMLINK_NAME_AFTER_RENAME,
                                         SYMLINK_COPIED_LOCATION.substring(0, SYMLINK_COPIED_LOCATION.length() - 1), SYMLINK_NAME_AFTER_COPYING);

        String pointsTo = resourceAdminClient.getResource(
                SYMLINK_COPIED_LOCATION + SYMLINK_NAME_AFTER_COPYING)[0].getRealPath();

        assertTrue((ROOT + COLL_NAME).equalsIgnoreCase(pointsTo), "Symlink has not being moved properly");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testCopySymlink", enabled = true)
    public void testFeedAfterCopying()
            throws ResourceAdminServiceExceptionException, IOException, XMLStreamException {

        ResourceData[] rData = resourceAdminClient.getResource(SYMLINK_COPIED_LOCATION + SYMLINK_NAME_AFTER_COPYING);

        OMElement atomFeedOMElement =
                getAtomFeedContent(constructAtomUrl(SYMLINK_COPIED_LOCATION + SYMLINK_NAME_AFTER_COPYING));

        assertNotNull(atomFeedOMElement, "No feed data available");

        //checking whether the created time is correct
        OMElement createdElement = atomFeedOMElement.getFirstChildWithName(
                new QName(REGISTRY_NAMESPACE, "createdTime"));

        assertTrue(createdElement.getText().equalsIgnoreCase(getAtomDateString(rData[0].getCreatedOn().getTime())),
                   "Copied Symlink - Created time is incorrect");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testFeedAfterCopying")
    public void testMoveSymlink()
            throws ResourceAdminServiceExceptionException, RemoteException, InterruptedException {
        resourceAdminClient.moveResource(SYMLINK_LOC,
                                         SYMLINK_LOC + SYMLINK_NAME_AFTER_RENAME,
                                         SYMLINK_MOVED_LOCATION.substring(0, SYMLINK_MOVED_LOCATION.length() - 1), SYMLINK_NAME_AFTER_MOVING);
        Thread.sleep(2000);


        //check that the collection has been moved
        String desc =
                resourceAdminClient.getResource(SYMLINK_MOVED_LOCATION + SYMLINK_NAME_AFTER_MOVING)[0].getDescription();

        assertTrue(COLL_DESC.equalsIgnoreCase(desc), "Symlink has not being copied properly");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testMoveSymlink", enabled = true)
    public void testFeedAfterMoving()
            throws ResourceAdminServiceExceptionException, IOException, XMLStreamException {

        ResourceData[] rData = resourceAdminClient.getResource(SYMLINK_MOVED_LOCATION + SYMLINK_NAME_AFTER_MOVING);

        OMElement atomFeedOMElement =
                getAtomFeedContent(constructAtomUrl(SYMLINK_MOVED_LOCATION + SYMLINK_NAME_AFTER_MOVING));

        assertNotNull(atomFeedOMElement, "No feed data available");

        //checking whether the created time is correct
        OMElement createdElement = atomFeedOMElement.getFirstChildWithName(
                new QName(REGISTRY_NAMESPACE, "createdTime"));

        assertTrue(createdElement.getText().equalsIgnoreCase(getAtomDateString(rData[0].getCreatedOn().getTime())),
                   "Copied Symlink - Created time is incorrect");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testFeedAfterMoving", expectedExceptions = AxisFault.class)
    public void testDeleteSymlink()
            throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminClient.deleteResource(SYMLINK_COPIED_LOCATION);

        resourceAdminClient.getResource(SYMLINK_MOVED_LOCATION + SYMLINK_NAME_AFTER_MOVING);


    }

    private String getAtomDateString(Date date) {
        AtomDate atomDate = new AtomDate(date);
        return atomDate.getValue();
    }

    private String constructAtomUrl(String feedPath) {

        FrameworkProperties gregProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        String registryURL;
        if (builder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            registryURL =
                    ProductUrlGeneratorUtil.getRemoteRegistryURLOfStratos(environment.getGreg().getProductVariables().
                            getHttpsPort(), environment.getGreg().getProductVariables().getHostName(),
                                                                          gregProperties, userInfo);
        } else {
            registryURL =
                    ProductUrlGeneratorUtil.getRemoteRegistryURLOfProducts(environment.getGreg().getProductVariables().
                            getHttpsPort(), environment.getGreg().getProductVariables().getHostName(),
                                                                           environment.getGreg().getProductVariables().getWebContextRoot());
        }
        return registryURL + "atom" + feedPath;
    }

    private OMElement getAtomFeedContent(String registryUrl) throws IOException,
                                                                    XMLStreamException {
        StringBuilder sb;
        InputStream inputStream = null;
        URL url = new URL(registryUrl);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            String userPassword = userInfo.getUserName() + ":" + userInfo.getPassword();
            String encodedAuthorization = Base64Utils.encode(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " +
                                                           encodedAuthorization);
            connection.connect();

            inputStream = connection.getInputStream();
            sb = new StringBuilder();
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            assert inputStream != null;
            inputStream.close();
        }

        return AXIOMUtil.stringToOM(sb.toString());

    }

    @AfterClass
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource(ROOT + COLL_NAME);
        resourceAdminClient = null;
        environment = null;
        builder=null;
        userInfo=null;
    }
}
