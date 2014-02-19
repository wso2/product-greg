/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.governance.api.test;

import org.testng.annotations.BeforeClass;

import javax.xml.namespace.QName;
import java.util.Collections;

public class TestSuiteTestCase extends RXTTestBase {

    public TestSuiteTestCase() {
        fileName = "testSuite.metadata.xml";
        key = "suites";
        path1 = "/test_suites/RenameArtifact/1.0.0";
        path2 = "/test_suites/NewRenameArtifact/1.0.0";
        values.put("overview_version", "1.0.0");
        values.put("overview_type", "Automated Regression");
        search = Collections
                .singletonMap("overview_testSuiteDescription",
                        "Regression Tests for Governance API");
        nameReplacement = new QName("NewGovernanceTestSuite");
    }

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        super.initTest();
        loadRXTsForAssetModelSamples("TestPlanModel");
    }
}