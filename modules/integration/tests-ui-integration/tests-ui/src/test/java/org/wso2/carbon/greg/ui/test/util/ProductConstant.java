/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.greg.ui.test.util;

import java.io.File;

public class ProductConstant {

    //Default file name for the node information xml
    public static final String NODE_FILE_NAME = "instance.xml";
    public static final String PROPERTY_FILE_NAME = "automation.properties";

    public static final String GREG_SERVER_NAME = "GREG";
    public static final String CLUSTER = "CLUSTER";
    public static final String MANAGER_SERVER_NAME = "MANAGER";
    public static final String FIREFOX_BROWSER = "firefox";
    public static final String CHROME_BROWSER = "chrome";
    public static final String IE_BROWSER = "ie";
    public static final String HTML_UNIT_DRIVER = "htmlUnit";
    public static final int SUPER_ADMIN_USER_ID = 0;
    public static final int ADMIN_USER_ID = 1;
    public static final String TENANT_ADMIN_PASSWORD = "admin123";
    public static final String PORT_OFFSET_COMMAND = "-DportOffset";

    public static final String DEFAULT_PRODUCT_ROLE = "testRole";
    public static final String ADMIN_ROLE_NAME = "admin";
    public static final String SEVER_STARTUP_SCRIPT_NAME = "wso2server";


    public static String SYSTEM_TEST_RESOURCE_LOCATION;
    public static String SYSTEM_TEST_SETTINGS_LOCATION;
    public static String SYSTEM_TEST_USER_FILE;
    public static String SYSTEM_TEST_TENANT_FILE;
    public static String REPORT_LOCATION;
    public static String REPORT_REPOSITORY = REPORT_LOCATION + "reports" + File.separator;

    public static void init() {
        SYSTEM_TEST_RESOURCE_LOCATION = getSystemResourceLocation();
        SYSTEM_TEST_SETTINGS_LOCATION = getSystemSettingsLocation();
        REPORT_LOCATION = getReportLocation();
        SYSTEM_TEST_TENANT_FILE = getTenantFile();
        SYSTEM_TEST_USER_FILE = getUsersFile();
    }

    public static String getResourceLocations(String productName) {
        return SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" +
                File.separator + productName;
    }

    public static String getModuleClientPath() {
        return SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "client";
    }

    public static String getSecurityScenarios() {
        return SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "security" + File.separator +
                "policies";
    }

    /**
     * construct the resource file path by checking the OS tests are running.
     * Need to replace string path with correct forward or backward slashes as we set the system property with
     * forward slash.
     *
     * @return resource path
     */
    public static String getSystemResourceLocation() {
        String resourceLocation;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            resourceLocation = System.getProperty("framework.resource.location").replace("/", "\\");
        } else {
            resourceLocation = System.getProperty("framework.resource.location").replace("/", "/");
        }
        return resourceLocation;
    }

    public static String getSystemSettingsLocation() {
        String settingsLocation;
        if (System.getProperty("automation.settings.location") != null) {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                settingsLocation = System.getProperty("automation.settings.location").replace("/", "\\");
            } else {
                settingsLocation = System.getProperty("automation.settings.location").replace("/", "/");
            }
        } else {
            settingsLocation = getSystemResourceLocation();
        }
        return settingsLocation;
    }

    public static String getTenantFile() {
        String tenantFile;
        tenantFile = getSystemSettingsLocation() + "tenantList.csv";
        return tenantFile;
    }

    public static String getUsersFile() {
        String usersFile;
        usersFile = getSystemSettingsLocation() + "userList.csv";
        return usersFile;
    }

    /**
     * construct the report path by check OS. TestNG reports will be written to the reportPath.
     * Need to replace string path with correct forward or backward slashes as we set the system property with
     * forward slash.
     *
     * @return report location
     */
    public static String getReportLocation() {
        String reportLocation;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
//            reportLocation = System.getProperty("framework.resource.location").replace("/", "\\").
//                    substring(0, SYSTEM_TEST_RESOURCE_LOCATION.indexOf("core\\org.wso2.automation.platform"));
//
            reportLocation = (System.getProperty("basedir", ".")) + File.separator + "target";

        } else {
//            reportLocation = System.getProperty("framework.resource.location").replace("/", "/").
//                    substring(0, SYSTEM_TEST_RESOURCE_LOCATION.indexOf("core/org.wso2.automation.platform"));
//
            reportLocation = (System.getProperty("basedir", ".")) + File.separator + "target";
        }
        return reportLocation;
    }

}
