/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.greg.migration.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.greg.migration.GRegMigrationException;
import org.wso2.carbon.greg.migration.MigrationDatabaseCreator;
import org.wso2.carbon.greg.migration.client.internal.ServiceHolder;
import org.wso2.carbon.greg.migration.util.Constants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class remove the old store tenant configuration (store.json) coming from G-Reg 5.1.0 and do a database migration
 * for latest identity tables.
 */
public class MigrateFrom510To520 implements MigrationClient{

    private static final Log log = LogFactory.getLog(MigrateFrom510To520.class);
    private DataSource dataSource;

    @Override
    public void databaseMigration(String migrateVersion) throws GRegMigrationException{
        try {
            long startTimeMillis = System.currentTimeMillis();
            log.info("Identity databases migration started.");
            initIdentityDataSource();
            log.info("Migration for identity databases Completed Successfully in " +
                     (System.currentTimeMillis() - startTimeMillis)
                     + "ms");
        } catch (IOException e) {
            String msg = "Error while processing registry.xml fle";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (CarbonException e) {
            String msg = "Error while processing inputstream of registry.xml";
            log.error(msg,e);
            throw new GRegMigrationException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while processing string to xml";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (NamingException e) {
            String msg = "Error when looking up the Data Source.";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (SQLException e) {
            String msg = "Failed to execute the migration script. " + e.getMessage();
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (Exception e) {
            String msg = "Error while migrating the idp and sp identity databases.";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        }
    }

    @Override
    public void registryResourceMigration() throws GRegMigrationException {
        log.info("Not implemented in 5.1.0 to 5.2.0 migration");
    }

    @Override
    public void fileSystemMigration() throws GRegMigrationException {
        log.info("Not implemented in 5.1.0 to 5.2.0 migration");
    }

    @Override
    public void cleanOldResources() throws GRegMigrationException {

        List<Tenant> tenantsArray = null;
        try {
            long startTimeMillis = System.currentTimeMillis();
            log.info("Store configuration migration started.");
            tenantsArray = getTenantsArray();
            for (Tenant tenant : tenantsArray) {
                    clean(tenant);
            }
            log.info("Migration for store config Completed Successfully in " +
                     (System.currentTimeMillis() - startTimeMillis)
                     + "ms");
        } catch (UserStoreException e) {
            String msg = "Error occurred while searching for tenant admin. ";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        }catch (RegistryException e) {
            String msg = "Error occurred while performing registry operation. ";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while processing string to xml";
            log.error(msg, e);
            throw new GRegMigrationException(msg, e);
        }

    }

    @Override
    public void endpointMigration() throws GRegMigrationException {
        log.info("Not implemented in 5.1.0 to 5.2.0 migration");
    }

    /**
     * This method returns the list of tenants.
     * @return list of tenants
     * @throws org.wso2.carbon.user.api.UserStoreException
     */
    private List<Tenant> getTenantsArray() throws UserStoreException {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        List<Tenant> tenantsArray = new ArrayList<Tenant>(Arrays.asList(tenantManager.getAllTenants()));
        Tenant superTenant = new Tenant();
        superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
        tenantsArray.add(superTenant);
        return tenantsArray;
    }

    /**
     * This method removes the store.json file at config registry which will fix issue REGISTRY-3528
     * @param tenant tenant
     * @throws UserStoreException
     * @throws RegistryException
     * @throws XMLStreamException
     */
    private void clean(Tenant tenant) throws UserStoreException, RegistryException, XMLStreamException {

        int tenantId = tenant.getId();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain(), true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                    .getAdminUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(adminName);
            ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
            Registry registry = ServiceHolder.getRegistryService().getConfigUserRegistry(adminName,tenantId);
            if(registry.resourceExists(Constants.STORE_CONFIG_PATH)){
               registry.delete(Constants.STORE_CONFIG_PATH);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    /**
     * This method reads the identity.xml and initialize the identity datasource.
     *
     * @throws Exception
     */
    public void initIdentityDataSource() throws Exception {
        OMElement persistenceManagerConfigElem = IdentityConfigParser.getInstance()
                .getConfigElement("JDBCPersistenceManager");
        if (persistenceManagerConfigElem == null) {
            String errorMsg = "Identity Persistence Manager configuration is not available in " +
                              "identity.xml file. Terminating the JDBC Persistence Manager " +
                              "initialization. This may affect certain functionality.";
            log.error(errorMsg);
            throw new GRegMigrationException(errorMsg);
        }
        OMElement dataSourceElem = persistenceManagerConfigElem.getFirstChildWithName(
                new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "DataSource"));
        if (dataSourceElem == null) {
            String errorMsg = "DataSource Element is not available for JDBC Persistence " +
                              "Manager in identity.xml file. Terminating the JDBC Persistence Manager " +
                              "initialization. This might affect certain features.";
            log.error(errorMsg);
            throw new GRegMigrationException(errorMsg);
        }
        OMElement dataSourceNameElem = dataSourceElem.getFirstChildWithName(
                new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "Name"));
        if (dataSourceNameElem != null) {
            String dataSourceName = dataSourceNameElem.getText();
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
            MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource);
            migrationDatabaseCreator.addNewIdentityTables();
        }
    }
}
