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

import java.util.Collections;

public class APITestCase extends RXTTestBase {

    public APITestCase() {
        fileName = "api.metadata.xml";
        key = "api";
        path1 = "/apimgt/applicationdata/provider/WSO2/RenameArtifact/1.0.0/api";
        path2 = "/apimgt/applicationdata/provider/WSO2/NewRenameArtifact/1.0.0/api";
        values.put("overview_provider", "WSO2");
        values.put("overview_version", "1.0.0");
        search = Collections.singletonMap("overview_businessOwnerEmail", "alice@smith.com");
    }
}
