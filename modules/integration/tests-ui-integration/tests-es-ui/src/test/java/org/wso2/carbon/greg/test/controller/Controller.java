/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.greg.test.controller;

import org.testng.TestNG;
import org.testng.annotations.BeforeSuite;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.util.ArrayList;
import java.util.List;
/***
 * This class will create dynamic testng.xml suites and submit for testing.
 * loop the given test classes
 * Original report will stored inside (tests-es-ui/target/test-output)
 * To enable this class add this class into testng.xml and remove other classes from testng.xml
 * put commandline arguments as ( -Diterations)
 */
public class Controller extends GREGIntegrationUIBaseTest {
    @BeforeSuite(groups = "wso2.greg")
    public void testController() throws InterruptedException {
        //Create an instance on TestNG
        TestNG testNG = new TestNG();
        List<XmlSuite> testSuites = new ArrayList<>();
        List<XmlTest> testsList;
        List<XmlClass> testClasses;
        XmlSuite testSuite;
        int testIterations = Integer.parseInt(System.getProperty("iterations"));
        for (int i = 1; i <= testIterations; i++) {
            //Create an instance of XML Suite and assign a name for it.
            testSuite = new XmlSuite();
            testSuite.setName("Greg-UI-Tests - " + i);
            //Create an instance of XmlTest and assign a name for it.
            XmlTest testScenario = new XmlTest(testSuite);
            testScenario.setName("TestSuite - " + i);
            //Create a list which can contain the classes that we want to run.
            testClasses = new ArrayList<>();
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.CustomRXTCRUDTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.RestServiceCRUDUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.SoapServiceCRUDUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.WsdlCRUDUrlUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.WadlCRUDUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.SwaggerCRUDUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.SchemaCRUDUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.crudui.PolicyCRUDUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.lifecycles.CustomLifecycleUITestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.searchui.SoapServiceSearchTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.searchui.CrossAssetSearchTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.general.AssociationsTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.publisher.general.VersionTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.store.test.SearchTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.store.test.LoginStoreTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.store.test.StoreHomePageTestCase"));
            testClasses.add(new XmlClass("org.wso2.carbon.greg.store.test.AssetsListingTestCase"));
            //Assign that to the XmlTest Object created earlier.
            testScenario.setXmlClasses(testClasses);
            //Create a list of XmlTests and add the XMLtest we created earlier to it.
            testsList = new ArrayList<>();
            testsList.add(testScenario);
            //add the list of tests to our Suite.
            testSuite.setTests(testsList);
            //Add the suite to the list of suites.
            testSuites.add(testSuite);
        }
        //Set the list of Suites to the testNG object we created earlier.
        testNG.setXmlSuites(testSuites);
        //invoke run() - this will run our test suite list.
        testNG.run();
    }
}
