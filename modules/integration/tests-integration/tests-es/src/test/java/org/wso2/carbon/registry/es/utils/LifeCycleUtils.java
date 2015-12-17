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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LifeCycleUtils {
    /***
     * This method is use to filter an integer based on given input string
     * @param duration this is a formatted string. (01d:08h:12m:44s)
     * @return integer
     */
    public static int filterInteger(String duration) {
        int returnValue = -1;
        if (duration != null) {
            int durationLength = duration.length();
            if (durationLength < 4) {//expected duration value similar to  "07s" , Then filter the numeric value
                returnValue = Integer.parseInt(duration.substring(0, durationLength - 1));
            } else if (duration.contains("m")) { //expected duration value similar to  "1m:07s"
                returnValue = 60;
            }
        }
        return returnValue;
    }

    /***
     * This method is used to modify the configuration in asset.js true/false
     * @param fromState current value true/false
     * @param toState   current value true/false
     * @throws IOException
     */
    public static void changeConfigurationAssetJS(String fromState, String toState) throws IOException {
        Path assetJSPath = Paths.get(LifeCycleConstants.ASSET_JS_CONFIG_PATH);
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(assetJSPath), charset);
        content = content
                .replaceAll("isLCStateDurationEnabled:" + fromState,
                        "isLCStateDurationEnabled:" + toState );
        Files.write(assetJSPath, content.getBytes(charset));
    }
}
