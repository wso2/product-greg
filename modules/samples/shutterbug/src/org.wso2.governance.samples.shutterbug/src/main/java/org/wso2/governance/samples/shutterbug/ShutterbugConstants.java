/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.governance.samples.shutterbug;

import org.wso2.carbon.registry.core.RegistryConstants;

public interface ShutterbugConstants {
    public final String SHUTTERBUG_MEDIA_TYPE = "application/vnd.wso2.shutterbug";

    public final String THUMBNAIL_PATH = "thumbs";
    public final String VOTE_PATH = "votes";

    public final int HEIGHT = 128;
    public final int WIDTH = 128;
    public final int QUALITY_PERCENTAGE = 100;

    public final String JPEG_MIME = "image/jpeg";

    public final String ASSOCIATION_TYPE_VOTED = "voted";
    public final String ASSOCIATION_TYPE_USED_BY = "usedBy";
    public final String ASSOCIATION_TYPE_THUMBNAIL = "thumbnail";

    public final String DEFAULT_SHUTTERBUG_HOME = RegistryConstants.ROOT_PATH + "shutterbug";
    public final int DEFAULT_UPLOAD_LIMIT = 2;
    public final int DEFAULT_VOTE_LIMIT = 12;

    public final String TENANT_USER_PREFIX = "registry.tenant";
    //public final String TENANT_USER_PREFIX = "tenant";

    public final String FEED_TITLE = "Shutterbug | Image Feed";
}
