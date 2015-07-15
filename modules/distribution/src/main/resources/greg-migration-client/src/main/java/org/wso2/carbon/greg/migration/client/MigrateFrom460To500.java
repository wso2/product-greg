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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.greg.migration.GRegMigrationException;
import org.wso2.carbon.greg.migration.client.internal.ServiceHolder;
import org.wso2.carbon.greg.migration.util.Constants;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class contains all the methods which is used to migrate from WSO2 Governance Registry 4.6.0 to WSO2 Governance Registry 5.0.0.
 * The migration performs in database, registry and file system
 */

@SuppressWarnings("unchecked")
public class MigrateFrom460To500 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom460To500.class);
    private List<Tenant> tenantsArray;

    public MigrateFrom460To500() throws UserStoreException {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        tenantsArray = new ArrayList(Arrays.asList(tenantManager.getAllTenants()));
        Tenant superTenant = new Tenant();
        superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
        tenantsArray.add(superTenant);
    }

    /**
     * This method is used to migrate all registry resources
     * This migrates swagger resources and rxts
     *
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    @Override
    public void registryResourceMigration() throws GRegMigrationException {
        copyNewRxtFileToRegistry();
        //rxtContentMigration();
    }

    @Override
    public void databaseMigration(String migrateVersion) throws GRegMigrationException, SQLException {
        log.info("Not implemented in 4.6.0 to 5.0.0 migration");
    }

    @Override
    public void fileSystemMigration() throws GRegMigrationException {
        log.info("Not implemented in 4.6.0 to 5.0.0 migration");
    }

    @Override
    public void cleanOldResources() throws GRegMigrationException {
        log.info("Not implemented in 4.6.0 to 5.0.0 migration");
    }

    /**
     * This method is used to copy new rxt to the registry
     * This copies rxt from the file system to registry
     *
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    void copyNewRxtFileToRegistry() throws GRegMigrationException {
        log.info("Rxt migration for WSO2 Governance Registry 5.0.0 started.");
        boolean isTenantFlowStarted = false;
        try {
            String resourcePath = Constants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION + "/types/";
            Map<String, String> rxtContents = new HashMap<String, String>();
            for(String rxt : Constants.MIGRATING_RXTS) {
                File newRxtFile = new File(CarbonUtils.getCarbonHome() + Constants.RXT_PATH+rxt+Constants.RXT_EXT);
                rxtContents.put(rxt,FileUtils.readFileToString(newRxtFile, "UTF-8"));
            }


            for (Tenant tenant : tenantsArray) {
                int tenantId = tenant.getId();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantId(MultitenantConstants.SUPER_TENANT_ID);

                String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId)
                        .getRealmConfiguration().getAdminUserName();
                ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
                Registry registry = ServiceHolder.getRegistryService().getGovernanceUserRegistry(adminName, tenantId);
                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

                for(String rxt : Constants.MIGRATING_RXTS) {
                    String rxtPath = resourcePath+rxt+ Constants.RXT_EXT;
                    Resource resource;
                    if (!registry.resourceExists(rxtPath)) {
                        resource = registry.newResource();
                    } else {
                        resource = registry.get(rxtPath);
                    }
                    resource.setContent(rxtContents.get(rxt));
                    //resource.setMediaType("application/xml");
                    registry.put(rxtPath, resource);

                    ServiceHolder.getRealmService().getTenantUserRealm(tenant.getId()).getAuthorizationManager()
                                 .authorizeRole(Constants.ANONYMOUS_ROLE, rxtPath, ActionConstants.GET);
                }
                PrivilegedCarbonContext.endTenantFlow();
                isTenantFlowStarted = false;
            }
            //_system/governance/repository/components/org.wso2.carbon.governance/types/api.rxt

        } catch (IOException e) {
            String msg = "Error occurred while reading the rxt file from file system.  ";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Error occurred while searching for tenant admin. ";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (RegistryException e) {
            String msg ="Error occurred while performing registry operation. ";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (Exception e) {
            String msg ="Error occurred while performing registry migration. ";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        }finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
