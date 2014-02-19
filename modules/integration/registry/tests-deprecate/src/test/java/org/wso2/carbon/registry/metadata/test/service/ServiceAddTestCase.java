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

package org.wso2.carbon.registry.metadata.test.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.services.stub.AddServicesServiceStub;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import static org.testng.Assert.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import static org.wso2.carbon.registry.metadata.test.util.TestUtils.isResourceExist;

/**
 * A test case which tests registry service add operation
 */
public class ServiceAddTestCase {

    private static final Log log = LogFactory.getLog(ServiceAddTestCase.class);
    public static final String TRUNK = "/_system/governance/trunk";
    private String servicePath = TRUNK + "/services/";
    private String wsdlPath = TRUNK + "/wsdls/";
    private String schemaPath = TRUNK + "/schemas/";

    private AddServicesServiceStub addServicesServiceStub;
    private ResourceAdminServiceStub resourceAdminServiceStub;

    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";


    @BeforeClass(groups = {"wso2.greg.service.a"})
    public void init() throws Exception {
        log.info("Initializing Add Service Resource Tests");
        log.debug("Add Service Resource Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();

    }

    @Test(groups = {"wso2.greg.service.a"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        addServicesServiceStub = TestUtils.getAddServicesServiceStub(loggedInSessionCookie);
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);

        String resourceName = "info.xml";
        String serviceName = "NewBizService";
        String wsdlName = serviceName + ".wsdl";
        String wsdlNamespacePath = "com/foo/";
        String schemaNamespacePath = "org/bar/purchasing/";
        String schemaName = "purchasing.xsd";

        String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + resourceName;


        try {
            addServicesServiceStub.addService(fileReader(resource));

            String textContent = resourceAdminServiceStub.getTextContent(servicePath +
                    wsdlNamespacePath + serviceName);


            if (textContent.indexOf("http://foo.com") != -1) {
                log.info("service content found");

            } else {
                log.error("service content not found");
                fail("service content not found");
            }


            String textContentWsdl = resourceAdminServiceStub.getTextContent(wsdlPath +
                    wsdlNamespacePath + wsdlName);

            if (textContentWsdl.indexOf("../../../schemas/org/bar/purchasing/purchasing.xsd") != -1) {
                log.info("wsdl content found");

            } else {
                log.error("wsdl content not found");
                fail("wsdl content not found");
            }
            String textContentSchema = resourceAdminServiceStub.getTextContent(schemaPath +
                    schemaNamespacePath + schemaName);

            if (textContentSchema.indexOf("http://bar.org/purchasing") != -1) {
                log.info("schema content found");

            } else {
                log.error("schema content not found");
                fail("schema content not found");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(servicePath +
                    wsdlNamespacePath + serviceName);

//            check if the deleted file exists in registry
            try{
//                This is for a normal delete operation.
                if(isResourceExist(loggedInSessionCookie, servicePath +
                        wsdlNamespacePath, serviceName, resourceAdminServiceStub)){
                    log.error("Resource not deleted from the registry");
                    fail("Resource not deleted from the registry");
                }else{
                    log.info("Resource successfully deleted from the registry");
                }
            }catch(Exception re){
//                If the delete service handler has been engaged, then the collection hierarchy is deleted too.
//                We test that scenario from this code segment.
                if(re.getMessage().contains("Resource does not exist at path")){
                    if(isResourceExist(loggedInSessionCookie,TRUNK,"",resourceAdminServiceStub)){
                        log.error("Collection hierarchy not deleted from the registry");
                        fail("Collection hierarchy not deleted from the registry");
                    }else{
                        log.info("Collection hierarchy successfully deleted from the registry");
                    }
                }
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    public static String fileReader(String fileName) {
        String fileContent = "";
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new
                    FileInputStream(fileName);

            // Convert our input stream to a
            // DataInputStream
            DataInputStream in =
                    new DataInputStream(fstream);

            // Continue to read lines while
            // there are still some left to read

            while (in.available() != 0) {
                fileContent = fileContent + (in.readLine());
            }

            in.close();
        } catch (Exception e) {
            System.err.println("File input error");
        }
        return fileContent;

    }
}

