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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.greg.migration.GRegMigrationException;
import org.wso2.carbon.greg.migration.client.internal.ServiceHolder;
import org.wso2.carbon.greg.migration.util.Constants;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class contains all the methods which is used to migrate from WSO2 Governance Registry 4.6.0 to WSO2 Governance Registry 5.0.0.
 * The migration performs in database, registry and file system
 */

@SuppressWarnings("unchecked")
public class MigrateFrom460To500 implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom460To500.class);
    private List<Tenant> tenantsArray;
    private Registry govRegistry;

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

    @Override
    public void endpointMigration() throws GRegMigrationException {
        migrateEndpointResources();
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

    /**
     * This method is used to migrate endpoint registry resources into RXT
     * based endpoint artifacts (assets).
     *
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    private void migrateEndpointResources() throws GRegMigrationException{
        boolean isTenantFlowStarted = false;
        try {
            for (Tenant tenant : tenantsArray) {
                int tenantId = tenant.getId();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(tenant.getDomain());


                String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                        .getAdminUserName();
                ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
                ServiceHolder.getRXTStoragePathService().addStoragePath(Constants.CORRECT_ENDPOINT_MEDIA_TYPE,
                        Constants.ENDPOINT_STORAGE_PATH);

                Registry registry = ServiceHolder.getRegistryService().getRegistry(adminName, tenantId);
                Resource endpointRoot = registry.get(Constants.GOV_PATH + "/" + Constants.ENDPOINT_PATH);
                migrateEndpointCollection((Collection) endpointRoot, registry);

                PrivilegedCarbonContext.endTenantFlow();
                isTenantFlowStarted = false;

            }
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

    /**
     * This method is used to migrate endpoint resources recursively under the registry path /trunk/endpoints.
     *
     * @param  root     root collection of the endpoints.
     * @param  registry instance of tenant user registry.
     * @throws org.wso2.carbon.greg.migration.GRegMigrationException
     */
    private  void migrateEndpointCollection(Collection root, Registry registry) throws
            GRegMigrationException{
        try {
            String[] childrenPaths = root.getChildren();
            for (String child : childrenPaths) {
                org.wso2.carbon.registry.core.Resource childResource = registry.get(child);
                if (childResource instanceof Collection) {
                    migrateEndpointCollection((Collection) childResource, registry);
                } else {
                    String path = null;
                    if (Constants.PREVIOUS_ENDPOINT_MEDIA_TYPE.equals(childResource.getMediaType())) {
                        try {
                            path = childResource.getPath();
                            byte[] configContent = (byte[]) childResource.getContent();
                            String content = RegistryUtils.decodeBytes(configContent);
                            OMElement endpointElement = AXIOMUtil.stringToOM(content);

                            OMElement address = endpointElement.getFirstElement();
                            String endpointURI = address.getAttributeValue(new QName("uri"));

                            String pathExpression = Constants.ENDPOINT_STORAGE_PATH;
                            pathExpression = CommonUtil.replaceExpressionOfPath(pathExpression, "name",
                                    deriveEndpointNameWithNamespaceFromUrl(endpointURI));
                            pathExpression = CommonUtil
                                    .getPathFromPathExpression(pathExpression, childResource.getProperties(), null);
                            String namespace = EndpointUtils.deriveEndpointNamespaceFromUrl(endpointURI)
                                    .replace("//", "/");
                            pathExpression = CommonUtil.replaceExpressionOfPath(pathExpression, "namespace", namespace);
                            pathExpression = pathExpression.replace("//", "/");
                            pathExpression = RegistryUtils
                                    .getAbsolutePath(registry.getRegistryContext(), pathExpression.replace("//", "/"));

                            String version = getVersionFromAssociations(childResource, registry);
                            String environment = childResource.getProperty("environment");
                            String sourcePath = path;
                            String destinationPath = pathExpression;
                            String newEndpointName = pathExpression.split("/")[pathExpression.split("/").length - 1];
                            String resourceContent = createEndpointElement(newEndpointName, endpointURI, version,
                                    environment);

                            registry.move(sourcePath, destinationPath);
                            Resource movedEndpoint = registry.get(destinationPath);
                            movedEndpoint.setContent(RegistryUtils.encodeString(resourceContent));
                            registry.put(destinationPath, movedEndpoint);

                            Resource newEndpoint = registry.get(destinationPath);
                            newEndpoint.setMediaType(Constants.CORRECT_ENDPOINT_MEDIA_TYPE);
                            registry.put(destinationPath, newEndpoint);

                        } catch (XMLStreamException e) {
                            throw new GRegMigrationException("Error in migrating endpoint at path" + path, e);
                        } catch (RegistryException e) {
                            throw new GRegMigrationException("Error in migrating endpoint at path" + path, e);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error in migrating endpoints", e);
            throw new GRegMigrationException("Error in migrating endpoints", e);
        }
    }

    /* *
    * Method used to create the new endpoint content according to the endpoint RXT.
    *
    * @param name           endpoint name.
    * @param uri            endpoint uri.
    * @param version        endpoint version
    * @param environment    endpoint environment.
    *
    * @return  endpoint content according to the endpoint RXT.
    * @throws  XMLStreamException.
     */
    private  String createEndpointElement(String name, String uri, String version, String environment)
            throws XMLStreamException {
        StringBuilder builder = new StringBuilder();
        builder.append("<endpoint xmlns=\"http://www.wso2.org/governance/metadata\">");
        builder.append("<overview><name>");
        builder.append(name);
        if (version != null) {
            builder.append("</name><version>");
            builder.append(version);
            builder.append("</version><address>");
        } else {
            builder.append("</name><version>1.0.0</version><address>");
        }
        builder.append(uri);
        builder.append("</address>");
        if (environment != null) {
            builder.append("<environment>");
            builder.append(environment);
            builder.append("</environment>");
        }
        builder.append("</overview></endpoint>");
        return builder.toString();
    }

    /* *
    * Method use to get version for the endpoint using associated services.
    * @param resource   the endpoint registry resource
    * @param registry   instance of an tenant user registry.
    *
    * @return   vesion from association if associations exist with a version. Otherwise returns null.
    * @throws   RegistryException
     */
    private  String getVersionFromAssociations(Resource resource, Registry registry) throws
            RegistryException {
        Association[] associations = registry.getAllAssociations(resource.getPath());
        for (Association association : associations) {
            String path = association.getDestinationPath();
            org.wso2.carbon.registry.core.Resource associatedResource = registry.get(path);
            String mediaType = associatedResource.getMediaType();
            if (Constants.SOAPSERVICE_MEDIA_TYPE.equals(mediaType) || Constants.RESTSERVICE_MEDIA_TYPE.equals(mediaType)
                    || Constants.WADL_MEDIA_TYPE.equals(mediaType) || Constants.WSDL_MEDIA_TYPE.equals(mediaType)
                    || Constants.SERVICE_MEDIA_TYPE.equals(mediaType)) {

                byte[] configContent = (byte[]) associatedResource.getContent();
                String content = RegistryUtils.decodeBytes(configContent);
                String version = getVersion(content, registry);
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        }
        return null;
    }

    /**
     * Returns an endpoint name with namespace and ENDPOINT_RESOURCE_PREFIX
     *
     * @param url endpoint address.
     *
     * @return endpoint name with namespace.
     */
    private String deriveEndpointNameWithNamespaceFromUrl(String url) {

        String tempURL = url;
        if (tempURL.startsWith("jms:/")) {
            tempURL = tempURL.split("[?]")[0];
        }
        String name = tempURL.split("/")[tempURL.split("/").length - 1].replace(".", "-").
                replace("=", "-").replace("@", "-").replace("#", "-").replace("~", "-");
        String namespace =  EndpointUtils.deriveEndpointNamespaceFromUrl(url).replace("//", "/");
        namespace = namespace.replace("/", ".");
        namespace += "-";

        return Constants.ENDPOINT_RESOURCE_PREFIX + namespace +name;

    }

    /**
     * Method used to get version from content of an existing association of a endpoint.
     *
     * @param content   content of the association.
     * @param registry  instance of an registry.
     * @return version extracted from the content.
     */
    private String getVersion (String content, Registry registry){
        String pattern = "version";

        Pattern p = Pattern.compile(Pattern.quote(pattern) + "(.*?)" + Pattern.quote(pattern));
        Matcher m = p.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(m.group(1));
        }
        String matchedString = sb.toString();
        if(matchedString != null && !matchedString.isEmpty() && matchedString.indexOf('>') > -1 && matchedString
                .indexOf('>') < matchedString.indexOf('<')){
            return matchedString.substring(matchedString.indexOf('>') +1 ,matchedString.indexOf('<') );
        }
        else{
            return null;
        }

    }
}
