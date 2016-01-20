/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.es.utils;

import org.wso2.carbon.utils.CarbonUtils;

public class LifeCycleConstants {
    public static final int WAIT_TIME_MILLISECONDS = 6000;
    public static final String SERVICE_TYPE = "restservice";
    public static final String LIFECYCLE_NAME = "ServiceLifeCycleLC2";
    public static final String LIFECYCLE_FILE = "CheckpointsLifecycle.xml";
    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";
    public static final String LC_STATE_DURATION_META_DATA = "lifecycleStateDurationMetaData";
    public static final String IS_LC_STATE_DURATION_ENABLED = "isLCStateDurationEnabled";
    public static final String LC_STATE_DURATION = "lifecycleStateDuration";
    public static final String LC_STATE_DURATION_COLOR = "lifecycleStateDurationColor";
    public static final String ASSET_JS_CONFIG_PATH = CarbonUtils.getCarbonHome() + "/repository/deployment/"
            + "server/jaggeryapps/publisher/extensions/assets/default/asset.js";
}
