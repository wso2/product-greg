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
package org.wso2.carbon.registry.utils;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class TestUtils {

    private TestUtils() {
    }

    public static void cleanupResources(Registry registry) throws RegistryException {
        for (String string : new String[]{"/trunk/services","/trunk/wsdls","/trunk","/branches","/departments",
                "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {
            if (registry.resourceExists(string)) {
                try {
                    registry.delete(string);
                } catch (Exception skip) {

                }
            }
        }
    }
}
