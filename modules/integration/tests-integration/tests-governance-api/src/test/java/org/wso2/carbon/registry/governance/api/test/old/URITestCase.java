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

import java.util.Collections;

public class URITestCase extends RXTTestBase {


    public URITestCase() {
        folder = "xml";
        fileName = "uri.metadata.xml";
        key = "uri";
        path1 = "/uris/Generic/RenameArtifact";
        path2 = "/uris/Generic/NewRenameArtifact";
        values.put("overview_type", "Generic");
        values.put("overview_uri", "http://google.com");
        search = Collections.singletonMap("overview_type", "Generic");
    }
}
