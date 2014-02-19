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
package org.wso2.registry.checkin.scm.repository;

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

public class ProviderRepository extends ScmProviderRepositoryWithHost {

    private String location;

    public ProviderRepository(String url) {
        super();
        this.location = location;
        setHost(url);
    }

    public ProviderRepository(String url, String username, String password) {
        this(url);
        setUser(username);
        setPassword(password);
    }

    public String getLocation() {
        return location;
    }

    public ScmProviderRepository getParent() {
        return new ProviderRepository(getHost(), getUser(), getPassword());
    }
}
