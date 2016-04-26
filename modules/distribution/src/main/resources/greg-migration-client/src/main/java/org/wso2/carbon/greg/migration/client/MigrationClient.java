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

package org.wso2.carbon.greg.migration.client;

import org.wso2.carbon.greg.migration.GRegMigrationException;

import java.sql.SQLException;

/**
 * Public interface for all migrations.
 * All the migrations after 4.60 to 5.0.0 migrations
 * Migration handled in three different steps as Database migrations, registry resource migrations and
 * file system resource migrations
 *
 */
@SuppressWarnings("unused")
public interface MigrationClient {

    /**
     * This method is used to migrate databases. This method adds the newly added columns, tables and alters the tables
     * according to the new database
     *
     * @param migrateVersion version to be migrated
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     * @throws SQLException
     */
    public void databaseMigration(String migrateVersion) throws GRegMigrationException, SQLException;

    /**
     * This method is used to migrate all the registry resources
     * RXTs and all other registry resources will be migrated
     *
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    public void registryResourceMigration() throws GRegMigrationException;


    /**
     * This method is used to migrate all file system resources.
     *
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    public void fileSystemMigration() throws GRegMigrationException;


    /**
     * This method is used to clean old resources from registry.
     * All older registry resources will be removed here
     *
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    public void cleanOldResources() throws GRegMigrationException;

    public void endpointMigration() throws GRegMigrationException;

}
