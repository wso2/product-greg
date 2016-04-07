/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.greg.migration.util;

import java.io.File;

public class Constants {

    public static final String VERSION_500 = "5.0.0";
    public static final String VERSION_520 = "5.2.0";

    public static final String ANONYMOUS_ROLE = "system/wso2.anonymous.role";

    public static final String GOVERNANCE_COMPONENT_REGISTRY_LOCATION = "/repository/components/org.wso2.carbon" +
                                                                        ".governance";
    public static final String RXT_PATH = "/repository/resources/rxts/";
    public static final String RXT_EXT = ".rxt";

    public static final String[] MIGRATING_RXTS =
            { "wadl", "wsdl", "service", "policy", "schema", "endpoint", "uri" };

    public static final String GOV_PATH = "/_system/governance";
    public static final String ENDPOINT_PATH = "trunk/endpoints/";
    public static final String PREVIOUS_ENDPOINT_MEDIA_TYPE = "application/vnd.wso2.endpoint";
    public static final String CORRECT_ENDPOINT_MEDIA_TYPE = "application/vnd.wso2-endpoint+xml";
    public static final String RESTSERVICE_MEDIA_TYPE = "application/vnd.wso2-restservice+xml";
    public static final String SOAPSERVICE_MEDIA_TYPE = "application/vnd.wso2-soap-service+xml";
    public static final String SERVICE_MEDIA_TYPE = "application/vnd.wso2-service+xml";
    public static final String WSDL_MEDIA_TYPE = "application/wsdl+xml";
    public static final String WADL_MEDIA_TYPE = "application/wadl+xml";
    public static final String ENDPOINT_RESOURCE_PREFIX = "ep-";
    public static final String ENDPOINT_STORAGE_PATH = GOV_PATH + "/" + ENDPOINT_PATH + "@{overview_name}";
    public static final String RESOURCETYPES_RXT_PATH =
            GOV_PATH + "/repository/components/org.wso2.carbon.governance/types";

    public static final String OVERVIEW = "overview";
    public static final String PROVIDER = "provider";
    public static final String STORAGE_PATH = "storagePath";
    public static final String ARTIFACT_TYPE = "artifactType";
    public static final String TYPE = "type";
    public static final String FILE_EXTENSION = "fileExtension";

    //Constants for email username migration client
    public static final String METADATA_NAMESPACE = "http://www.wso2.org/governance/metadata";
    public static final String OLD_EMAIL_AT_SIGN = ":";
    public static final String NEW_EMAIL_AT_SIGN = "-at-";
    public static final String CARBON_HOME = System.getProperty("carbon.home");
    public static final String REGISTRY_MIGRATION_SCRIPT = "migration-scripts" + File.separator + "reg_migration.sql";
    public static final String UM_MIGRATION_SCRIPT = "migration-scripts" + File.separator + "um_migration.sql";
    public static final String REGISTRY_XML_PATH = Constants.CARBON_HOME + File.separator + "repository" + File.separator
                                                   + "conf" + File.separator + "registry.xml";

    //registry path for store.json file at config registry
    public static final String STORE_CONFIG_PATH = "/store/configs/store.json";


}
