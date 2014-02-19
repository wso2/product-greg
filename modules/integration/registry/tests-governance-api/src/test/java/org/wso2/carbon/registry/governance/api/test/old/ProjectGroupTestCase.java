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
package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.Collections;

public class ProjectGroupTestCase extends RXTTestBase {

    public ProjectGroupTestCase() {
        folder = "xml";
        fileName = "project-group.metadata.xml";
        key = "groups";
        path1 = "/project-groups/RenameArtifact";
        path2 = "/project-groups/NewRenameArtifact";
        values.put("overview_groupOwner", "/people/123-abc-4567-defghi");
        search = Collections.singletonMap("overview_groupOwner", "/people/123-abc-4567-defghi");
        nameReplacement = new QName("ACME Development Group New");
    }

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        super.initTest();
        loadRXTsForAssetModelSamples("PeopleModel");
    }

}
