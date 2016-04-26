/*
* Copyright (c) 2015-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.greg.migration.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.greg.migration.GRegMigrationException;
import org.wso2.carbon.greg.migration.client.EmailUserNameMigrationClient;
import org.wso2.carbon.greg.migration.client.MigrateFrom460To500;
import org.wso2.carbon.greg.migration.client.MigrateFrom510To520;
import org.wso2.carbon.greg.migration.client.MigrationClient;
import org.wso2.carbon.greg.migration.client.ProviderMigrationClient;
import org.wso2.carbon.greg.migration.util.Constants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;


/**
 * @scr.component name="org.wso2.carbon.greg.migration.client" immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="registry.core.dscomponent"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="tenant.registryloader" interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader" cardinality="1..1"
 * policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="registry.rxt.component"
 * interface="org.wso2.carbon.registry.extensions.services.RXTStoragePathService" cardinality="1..1"
 * policy="dynamic" bind="setRXTStoragePathService" unbind="unsetRXTStoragePathService"
 */

@SuppressWarnings("unused")
public class GRegMigrationServiceComponent {

    private static final Log log = LogFactory.getLog(GRegMigrationServiceComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        String migrateVersion = null;
        boolean isRegistryMigrationNeeded = false;
        boolean isFileSystemMigrationNeeded = false;
        boolean isProviderMigrationNeeded = false;
        boolean isEmailUsernameMigrationNeeded = false;

        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("migrateVersion", System.getProperty("migrate"));
        argsMap.put("isRegMigrationNeeded", System.getProperty("migrateReg"));
        argsMap.put("isFileSysMigrationNeeded", System.getProperty("migrateFS"));
        argsMap.put("isProviderMigrationNeeded", System.getProperty("migrateProvider"));
        argsMap.put("isEmailUsernameMigrationNeeded", System.getProperty("migrateEmailUsername"));
        argsMap.put("isStoreConfigMigrationNeeded", System.getProperty("migrateStoreConfig"));

        if (!argsMap.isEmpty()) {
            migrateVersion = argsMap.get("migrateVersion");
            if (argsMap.get("isRegMigrationNeeded") != null) {
                isRegistryMigrationNeeded = Boolean.parseBoolean(argsMap.get("isRegMigrationNeeded"));
            }
            if (argsMap.get("isFileSysMigrationNeeded") != null) {
                isFileSystemMigrationNeeded = Boolean.parseBoolean(argsMap.get("isFileSysMigrationNeeded"));
            }
            if (argsMap.get("isProviderMigrationNeeded") != null) {
                isProviderMigrationNeeded = Boolean.parseBoolean(argsMap.get("isProviderMigrationNeeded"));
            }
            if (argsMap.get("isEmailUsernameMigrationNeeded") != null) {
                isEmailUsernameMigrationNeeded = Boolean.parseBoolean(argsMap.get("isEmailUsernameMigrationNeeded"));
            }
        }

        try {
            if (migrateVersion != null) {
                if (Constants.VERSION_500.equalsIgnoreCase(migrateVersion)) {
                    log.info("Migrating Governance Registry 4.6.0 resources to Governance Registry 5.0.0");

                    MigrationClient migrationClient = new MigrateFrom460To500();

                    //Default operation will migrate all three types of resources
                    if (argsMap.get("isRegMigrationNeeded") == null && argsMap.get("isFileSysMigrationNeeded") == null) {
                        log.info("Migrating WSO2 Governance Registry 4.6.0 resources to WSO2 Governance Registry 5.0.0");
                        migrationClient.registryResourceMigration();
                        migrationClient.fileSystemMigration();
                        migrationClient.endpointMigration();
                    } else {
                        //Only performs registry migration which also includes endpoint migration as well.
                        if (isRegistryMigrationNeeded) {
                            log.info("Migrating WSO2 Governance Registry 4.6.0 registry resources to WSO2 Governance " +
                                     "Registry 5.0.0");
                            migrationClient.registryResourceMigration();
                            migrationClient.endpointMigration();
                        }
                        //Only performs file system migration
                        if (isFileSystemMigrationNeeded) {
                            log.info("Migrating WSO2 Governance Registry 4.6.0 file system resources to WSO2 Governance " +
                                     "Registry 5.0.0");
                            migrationClient.fileSystemMigration();
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("WSO2 Governance Registry 4.6.0 to 5.0.0 migration successfully completed");
                    }
                } else if (Constants.VERSION_520.equalsIgnoreCase(migrateVersion)) {
                    if (!isEmailUsernameMigrationNeeded && !isProviderMigrationNeeded) {
                        MigrateFrom510To520 migrateFrom510To520 = new MigrateFrom510To520();
                        migrateFrom510To520.databaseMigration(migrateVersion);
                        migrateFrom510To520.cleanOldResources();
                    } else if (isEmailUsernameMigrationNeeded) {
                        EmailUserNameMigrationClient emailUserNameMigrationClient = new EmailUserNameMigrationClient();
                        emailUserNameMigrationClient.migrateResourcesWithEmailUserName();
                    } else if (isProviderMigrationNeeded) {
                        ProviderMigrationClient providerMigrationClient = new ProviderMigrationClient();
                        providerMigrationClient.providerMigration();
                    }

                } else {
                    log.error("The given migrate version " + migrateVersion + " is not supported. Please check the " +
                              "version and try again.");
                }
            }
        } catch (GRegMigrationException e) {
            log.error("GReg Migration  exception occurred while migrating " + e.getMessage());
        } catch (UserStoreException e) {
            log.error("User store  exception occurred while migrating " + e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while initializing data source.  " + e.getMessage());
        }
        log.info("WSO2 Governance Registry migration component successfully activated.");
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.info("WSO2 Governance Registry migration bundle is deactivated");
    }

    /**
     * Method to set registry service.
     *
     * @param registryService service to get tenant data.
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting RegistryService for WSO2 Governance Registry migration");
        }
        ServiceHolder.setRegistryService(registryService);
    }

    /**
     * Method to unset registry service.
     *
     * @param registryService service to get registry data.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Registry service");
        }
        ServiceHolder.setRegistryService(null);
    }

    /**
     * Method to set realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting RealmService for WSO2 Governance Registry migration");
        ServiceHolder.setRealmService(realmService);
    }

    /**
     * Method to unset realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Realm service");
        }
        ServiceHolder.setRealmService(null);
    }

    /**
     * Method to set tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        log.debug("Setting TenantRegistryLoader for WSO2 Governance Registry migration");
        ServiceHolder.setTenantRegLoader(tenantRegLoader);
    }

    /**
     * Method to unset tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        log.debug("Unset Tenant Registry Loader");
        ServiceHolder.setTenantRegLoader(null);
    }

    /**
     * Method to set RXT storage service.
     *
     * @param rxtStoragePathService service to get RXT storage path data.
     */
    protected void setRXTStoragePathService(RXTStoragePathService rxtStoragePathService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting RegistryService for WSO2 Governance Registry migration");
        }
        ServiceHolder.setRXTStoragePathService(rxtStoragePathService);
    }

    /**
     * Method to unset RXT storage service.
     *
     * @param rxtStoragePathService service to get RXT storage path data.
     */
    protected void unsetRXTStoragePathService(RXTStoragePathService rxtStoragePathService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Registry service");
        }
        ServiceHolder.setRXTStoragePathService(null);
    }
}
