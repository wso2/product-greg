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

/***
 * This class use to validate REST API response details
 */
public class LifeCycleBean {
    /***
     * lifecycle state duration state
     */
    private boolean isLCStateDurationEnabled;
    /***
     * lifecycle state duration meta data availability
     */
    private boolean isLifecycleMetaDataAvailable;
    /***
     * lifecycle state duration
     */
    private String lifecycleStateDuration;
    /***
     * lifecycle state duration color
     */
    private String lifecycleStateDurationColor;

    /***
     * this method gives lifecycle meta data availability
     *
     * @return boolean
     */
    public boolean getLifecycleMetaDataState() {
        return isLifecycleMetaDataAvailable;
    }

    /***
     * this method is sets lifecycle state duration availability
     *
     * @param state boolean
     */
    public void setLifecycleMetaDataState(boolean state) {
        this.isLifecycleMetaDataAvailable = state;
    }

    /***
     * this method gives lifecycle State duration feature availability
     *
     * @return boolean
     */
    public boolean getLifecycleStateDurationState() {
        return isLCStateDurationEnabled;
    }

    /***
     * this method sets lifecycle state duration feature availability
     *
     * @param state boolean
     */
    public void setLifecycleStateDurationState(boolean state) {
        this.isLCStateDurationEnabled = state;
    }

    /***
     * this method gives lifecycle current state duration time
     *
     * @return String
     */
    public String getLifecycleStateDuration() {
        return lifecycleStateDuration;
    }

    /***
     * this method sets lifecycle current state duration time
     *
     * @param duration String
     */
    public void setLifecycleStateDuration(String duration) {
        this.lifecycleStateDuration = duration;
    }

    /***
     * this method gives lifecycle state duration color
     *
     * @return String
     */
    public String getLifecycleStateDurationColor() {
        return lifecycleStateDurationColor;
    }

    /***
     * this method sets lifecycle state duration color
     *
     * @param color String
     */
    public void setLifecycleStateDurationColor(String color) {
        this.lifecycleStateDurationColor = color;
    }
}
