/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.permission.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class SymbolicServiceTestCase {
    private static final Log log = LogFactory.getLog(SymbolicServiceTestCase.class);
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;
    private static ResourceAdminServiceClient resourceAdminServiceClient;


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception, LoginAuthenticationExceptionException {
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a collection @ root level",
            priority = 1)
    public void testAddSymbolinktoCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        String parentPath = "/";
        String collectionName = "admin_service";
        String symbolinkName = "symb_" + collectionName;
        String targetPath = parentPath + collectionName;
        try {
            resourceAdminServiceClient.addCollection(parentPath, collectionName,
                    "default", "");
            log.info("***Collection successfully Created***");

            resourceAdminServiceClient.addSymbolicLink(parentPath, symbolinkName,
                    targetPath);
            log.info("***Symbolink successfully Created***");

            CollectionContentBean collectionContentBean = resourceAdminServiceClient.
                    getCollectionContent("/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (String aChildPath : childPath) {
                    if (aChildPath.equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(aChildPath.equalsIgnoreCase("/" + symbolinkName),
                                "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            resourceAdminServiceClient.deleteResource(targetPath);
            log.info("*************Add Symbolink to Root Level Collection test - Passed **********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Root Level Collection test - Failed :" + e);
            throw new RemoteException("Add Symbolink to Root Level Collection test - Failed :" +
                    e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink to Root Level Collection test - Failed :" + e);
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink to Root Level Collection test - Failed :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a resource @ root level",
            priority = 2)
    public void testAddSymbolinktoResource()
            throws RemoteException, ResourceAdminServiceExceptionException {
        String parentPath = "/";
        String resource_name = "resource.txt";
        String symbolinkName = "symb_" + resource_name;
        String targetPath = parentPath + resource_name;
        try {
            resourceAdminServiceClient.addTextResource(parentPath, resource_name,
                    "", "", "");
            log.info("Successfully resource.txt created :");

            resourceAdminServiceClient.addSymbolicLink(parentPath, symbolinkName,
                    targetPath);
            log.info("Successfully symb_resource symbolink created");

            CollectionContentBean collectionContentBean = resourceAdminServiceClient.
                    getCollectionContent("/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (String aChildPath : childPath) {
                    if (aChildPath.equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(aChildPath.equalsIgnoreCase("/" + symbolinkName),
                                "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            resourceAdminServiceClient.deleteResource(targetPath);
            log.info("*************Add Symbolink to Root Level Resource test - Passed **********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Root Level Resource test - Failed :" + e);
            throw new RemoteException("Add Symbolink to Root Level Resource test - Failed :" +
                    e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink to Root Level Resource test - Failed :" + e);
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink to Root Level Resource test - Failed :" + e);
        }


    }

    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a collection @ general level",
            priority = 3)
    public void testAddSymbolinktoGeneralCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        String parentPath = "/";
        String collectionName = "collection/symbolink/test";
        String symbolinkName = "symb_" + "test";
        String targetPath = parentPath + collectionName;
        try {
            resourceAdminServiceClient.addCollection(parentPath, collectionName,
                    "default", "");
            log.info("***Collection successfully Created***");

            resourceAdminServiceClient.addSymbolicLink(parentPath, symbolinkName,
                    targetPath);
            log.info("***Symbolink successfully Created***");

            CollectionContentBean collectionContentBean = resourceAdminServiceClient.
                    getCollectionContent("/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (String aChildPath : childPath) {
                    if (aChildPath.equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(aChildPath.equalsIgnoreCase("/" + symbolinkName),
                                "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            resourceAdminServiceClient.deleteResource("/collection");
            log.info("*********Add Symbolink to Collection at General level test - Passed ********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Collection at General level test - Failed :" + e);
            throw new RemoteException("Add Symbolink to Collection at General level test- Failed :" +
                    e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink Collection at General level test - Failed :" + e);
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink Collection at General level test - Failed :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a resource @ root level",
            priority = 4)
    public void testAddSymbolinktoGeneralResource()
            throws RemoteException, ResourceAdminServiceExceptionException {
        String parentPath = "/";
        String resource_name = "resource/symbolink/test123.txt";
        String symbolinkName = "symb_" + "test123.txt";
        String targetPath = parentPath + resource_name;
        try {
            resourceAdminServiceClient.addTextResource(parentPath, resource_name,
                    "", "", "");
            log.info("Successfully resource.txt created :");

            resourceAdminServiceClient.addSymbolicLink(parentPath, symbolinkName,
                    targetPath);
            log.info("Successfully symb_resource symbolink created");

            CollectionContentBean collectionContentBean = resourceAdminServiceClient.
                    getCollectionContent("/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (String aChildPath : childPath) {
                    if (aChildPath.equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(aChildPath.equalsIgnoreCase("/" + symbolinkName),
                                "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            resourceAdminServiceClient.deleteResource("/resource");
            log.info("******Add Symbolink to Resource at General Level test - Passed ********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Resource at General Level test - Failed :" + e);
            throw new RemoteException("Add Symbolink to Resource at General Level test - Failed :" +
                    e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink to Resource at General Level test - Failed :" + e);
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink to Resource at General Level test - Failed :" + e);
        }
    }

    @AfterClass
    public void clean() {
        resourceAdminServiceClient = null;
        builder = null;
        environment = null;

    }
}
