/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
asset.manager = function(ctx) {
    var tenantAPI = require('/modules/tenant-api.js').api;
    var rxtApp = require('rxt').app;
    var availableUIAssetTypes = rxtApp.getUIActivatedAssets(ctx.tenantId);
    var availableAssetTypes = rxtApp.getActivatedAssets(ctx.tenantId);
    var allAvailableAssetTypes = String(availableAssetTypes.concat(availableUIAssetTypes));

    var getAssociations = function (genericArtifacts, userRegistry) {
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                var path = genericArtifacts[index].getPath();
                var mediaType = genericArtifacts[index].getMediaType();
                var name = genericArtifacts[index].getQName().getLocalPart();
                var govUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils
                var keyName = govUtils.getArtifactConfigurationByMediaType(getRegistry(ctx.session).registry, mediaType).getKey();
                if (isDisabledAsset(keyName)) {
                    continue;
                }
                var subPaths = path.split('/');
                var versionAttribute = ctx.rxtManager.getVersionAttribute(keyName);
                var associationVersion = genericArtifacts[index].getAttribute(versionAttribute);
                // This is only for WSO2 OOTB artifacts which have correct storage path
                if (!associationVersion && (subPaths.length - 2) > -1) {
                    associationVersion = subPaths[subPaths.length - 2]
                }
                var resource = userRegistry.registry.get('/_system/governance' + path);
                var associationUUID = resource.getUUID();
                deps.associationName = name;
                deps.associationType = keyName;
                deps.associationUUID = associationUUID;
                deps.associationVersion = associationVersion;
                associations.push(deps);
            }
        }
        return associations;
    };

    var isDisabledAsset = function (shortName) {
        // This method will return true if shortName not available in allAvailableAssetTypes string.
        var pat1 = new RegExp("^" + shortName + ",");
        var pat2 = new RegExp("," + shortName + "$");
        var pat3 = new RegExp("," + shortName + ",");
        return (!(pat3.test(allAvailableAssetTypes)) && !(pat1.test(allAvailableAssetTypes)) && !(pat2.test(allAvailableAssetTypes)));
    };

    var setDependencies = function(genericArtifact, asset ,userRegistry) {
        //get dependencies of the artifact.
        var dependencyArtifacts = genericArtifact.getDependencies();
        asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
    };
    var setDependents = function(genericArtifact, asset, userRegistry) {
        var dependentArtifacts = genericArtifact.getDependents();
        asset.dependents = getAssociations(dependentArtifacts, userRegistry);
    };
    var getRegistry = function(cSession) {
        var tenantDetails = tenantAPI.createTenantAwareAssetResources(cSession,{type:ctx.assetType});
        if((!tenantDetails)&&(!tenantDetails.am)) {
            log.error('The tenant-api was unable to create a registry instance by resolving tenant details');
            throw 'The tenant-api  was unable to create a registry instance by resolving tenant details';
        }
        return tenantDetails.am.registry;
    };

    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get: function(id) {
            //TODO: support services added through WSDL, once multiple lifecycle is supported.
            var asset = this._super.get.call(this, id);
            var userRegistry = getRegistry(ctx.session);
            var rawArtifact = this.am.manager.getGenericArtifact(id);
            try {
                setDependencies(rawArtifact, asset, userRegistry);
            } catch (e){
                log.error('Error occurred while retrieving associated "depends" dependencies');
            }
            try {
                setDependents(rawArtifact, asset, userRegistry);
            } catch (e){
                log.error('Error occurred while retrieving associated "usedBy" dependencies');
            }
            return asset;
        }
    };
};

asset.renderer = function(ctx) {
    var gregAPI = require('/modules/greg-publisher-api.js').gregAPI;
    var rxt = require('rxt');
    var server = require('store').server;

    var assetManager = function(session, type) {
        var am;
        var user = server.current(ctx.session);
        //Create asset manager depend on whether there is a loggedIn user or not.
        if (user) {
            am = rxt.asset.createUserAssetManager(ctx.session, type);
        } else {
            var carbon = require('carbon');
            var tenantAPI = require('/modules/tenant-api.js').api;
            var tenantDetails = tenantAPI.tenantContext(session);
            var tenantId = tenantDetails.urlTenantId; //carbon.server.superTenant.tenantId;
            am = rxt.asset.createAnonAssetManager(ctx.session, type, tenantId);
        }
        return am;
    };
	return {
        pageDecorators: {
            recentAssets: function (page) {
                return;
            },
            myAssets: function (page) {
                return;
            },
            embedLinks: function (page, meta) {
                return;
            },
            sidebarPopulator: function(page) {
                if (page.meta.pageName === 'details') {
                    page.isSidebarEnabled = true;
                    page.isNotificationbarEnabled = true;
                }
                if (page.meta.pageName === 'list') {
                    page.isNotificationbarEnabled = true;
                }
            },
            subscriptionPopulator: function(page) {
                if (page.meta.pageName === 'details') {
                    var am = assetManager(ctx.session,ctx.assetType);
                    log.debug('### obtaining subscriptions ###');
                    page.subscriptions = gregAPI.subscriptions.list(am,page.assets.id);
                    log.debug('### done ###');
                }
            },
            notificationPopulator: function(page) {
                if (page.meta.pageName === 'list' || page.meta.pageName === 'details') {
                    var am = assetManager(ctx.session,ctx.assetType);
                    page.notificationsCount = gregAPI.notifications.count(am);
                }
            },
            notificationListPopulator: function(page) {
                if (page.meta.pageName === 'list' || page.meta.pageName === 'details') {
                    var am = assetManager(ctx.session,ctx.assetType);
                    page.notifications = gregAPI.notifications.list(am);
                }
            },
            checkDependents: function (page) {
                if (page.assets) {
                    var dependencies = page.assets.dependencies || [];
                    var dependents = page.assets.dependents || [];
                    var dependencyCheck = {
                        isDependencies: dependencies.length > 0,
                        isDependents: dependents.length > 0
                    };
                    dependencyCheck.isAnyDependence = ( dependencyCheck.isDependencies ) || ( dependencyCheck.isDependents );
                    page.assets.dependencyCheck = dependencyCheck;
                }
            },
            downloadPopulator:function(page){
                //Populate the links for downloading content RXTs
                if(page.meta.pageName === 'details'){
                    var isDownloadable = ctx.rxtManager.isDownloadable(page.assets.type);
                    if(!isDownloadable){
                        return;
                    }
                    var config = require('/config/store.js').config();
                    var rxt = require('rxt').server;
                    var pluralType = page.rxt.pluralLabel.toLowerCase();
                    var domain = require('carbon').server.tenantDomain({tenantId:ctx.tenantId});
                    page.downloadMetaData = {}; 
                    page.downloadMetaData.downloadFileType = page.rxt.singularLabel;
                    page.downloadMetaData.enabled = isDownloadable;
                    page.downloadMetaData.url = config.server.https + rxt.buildURL('governance/') +
                        pluralType + '/' + page.assets.id + '/content?tenant=' + domain;
                }
            },
            versions: function (page) {
                if (page.meta.pageName !== 'details') {
                    return;
                }

                var type = page.assets.type;
                page.assetVersions = gregAPI.getAssetVersions(ctx.session, ctx.assetType, page.assets.path, page.assets.name);
                var single_version = false;
                if(page.assetVersions.length === 1){
                    single_version = true;
                }
                page.single_version = single_version;
            },
            checkSubscriptionMenuItems: function (page) {
                page.isContentType = false;
                if (page.rxt && page.rxt.fileExtension) {
                    page.isContentType = true;
                }
            }
        }
    }
};

asset.configure = function() {
    return {
        meta: {
            'isDependencyShown': true,
            'isDiffViewShown': true,
            timestamp:'createdDate',
            sorting: {
                attributes: [
                    {name: "overview_name", label: "Name"},
                    {name: "createdDate", label: "Date/Time"}
                ]
            },
            ui:{
                icon:'fw fw-resource'
            }
        }
    }
};