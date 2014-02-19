/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.samples.handler.process.utils;

public class ProcessUtil {

    public static final java.lang.String ARTIFACT_ID_PROP_KEY = "registry.artifactId";
    public static final java.lang.String DEPENDS = "depends";
    public static final java.lang.String USED_BY = "usedBy";
    public static final String OWNS = "depends";
    public static final String OWNED_BY = "ownedBy";
    public static final java.lang.String ACTUAL_PATH = "registry.actualpath";

    // handling the possibility that handlers are not called within each other.
    private static ThreadLocal<Boolean> updateInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isUpdateLockAvailable() {
        return !updateInProgress.get();
    }

    public static void acquireUpdateLock() {
        updateInProgress.set(true);
    }

    public static void releaseUpdateLock() {
        updateInProgress.set(false);
    }
}
