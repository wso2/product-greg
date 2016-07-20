/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
asset.manager = function(ctx) {
    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var userRegistry = userMod.userRegistry(cSession);
        return userRegistry;
    };
    var restAssetManager = function(session){
		var rxt = require('rxt');
		var am = rxt.asset.createUserAssetManager(session, 'restservice');
		return am;
	};
    var isOnlyAssetVersion = function(asset, am) {
        var versions = am.getAssetGroup(asset);
        return (versions.length < 1) ? true : false;
    };
    var addDefaultPropertyIfNotExist = function(registry, path, name){
        var associations = registry.getAllAssociations(path);

        for(var index = 0; index< associations.length; index++){
            var associatedResourcePath = associations[index].getDestinationPath();
            if(associatedResourcePath.indexOf("restservice") > -1){     
                var associatedService = registry.get(associatedResourcePath);

                var serviceName = null;
                var indexVal = name.indexOf(".wadl");

                if(indexVal > -1) {
                    serviceName = name.substring(0, indexVal);
                } else {
                    serviceName = name;
                }

                var q = {};
                q.overview_name = serviceName;
                // Change the name to rest Asset Manager
                var artifacts = restAssetManager(ctx.session).search(q);

                if(artifacts.length < 1) {
                    associatedService.addProperty("default", "true");
                    registry.put(associatedResourcePath, associatedService);
                }
            }
        }
    };
    var addDefaultPropertyIfNotExistToThis = function(registry, path, name, am) {
        var wsdlResource = registry.get(path);
        var q = {};
        q.overview_name = name;
        var artifacts = am.search(q);

        if(artifacts.length < 1) {
            // Property named default with value true is needed for exactly one asset
            // of all types. This is important for asset grouping.
            wsdlResource.addProperty("default", "true");
            registry.put(path, wsdlResource);
        }
    };

    var getAssociations = function(associatedResources, userRegistry){
        //Array to store the association names.
        var associations = [];

        for(var index = 0; index< associatedResources.length; index++){
            var deps = {};
            var path = associatedResources[index].getDestinationPath();

            if(path.indexOf("restservice") > -1) {
                var subPaths = path.split('/');
                var associationName = subPaths[subPaths.length - 1];
                var resource = userRegistry.registry.get(path);
                var associationUUID = resource.getUUID();
                deps.associationName = associationName;
                deps.associationType = "restservice";
                deps.associationUUID = associationUUID;
                deps.associationVersion = subPaths[subPaths.length - 2];

                associations.push(deps);
            }
        }
        return associations;
    };

    var setDependencies = function(asset,userRegistry) {
        try {
            //get dependencies of the artifact.
            var associatedResources = userRegistry.registry.getAllAssociations(asset.path);
            asset.dependencies = getAssociations(associatedResources, userRegistry);
        } catch(e) {
            asset.dependencies = [];
        }
    };

    return {
        importAssetFromHttpRequest: function(options) {
            log.debug('Importing asset from request');
            return options;
        },
        combineWithRxt: function(asset) {
            return asset;
        },
        create: function(options) {
            var url = options.overview_url;
            var name = options.overview_name;
            var version = options.overview_version;
            var userRegistry = getRegistry(ctx.session);
            var utils = Packages.org.wso2.carbon.registry.resource.services.utils.ImportResourceUtil;
            var parentPath = "/_system/governance/apimgt/applicationdata/api-docs/".concat(version);
            var mediaType = "application/swagger+json";
            var javaArray = Packages.java.lang.reflect.Array;
            var properties = javaArray.newInstance(java.lang.String, 1, 2);
            properties[0][0] = 'version';
            properties[0][1] = version;

            var rxt = require('rxt');
            var am = rxt.asset.createUserAssetManager(ctx.session, this.type);
            var query = {};
            query.overview_name = name;
            query._wildcard = false;
            var assets = am.search(query);
            for (var i = 0; i < assets.length; i++) {
                if (assets[i].version == version) {
                    var msg = "resource already exist with Name \"" + name + "\" and version \"" + version + "\"";
                    var exceptionUtils = require('utils');
                    var exceptionModule = exceptionUtils.exception;
                    var constants = rxt.constants;
                    throw exceptionModule.buildExceptionObject(msg, constants.STATUS_CODES.BAD_REQUEST);
                }
            }

            log.debug("Grouping attribute " + this.rxtManager.groupingAttributes(this.type));

            var path = utils.importResource(parentPath, name, mediaType, '', url, '', userRegistry.registry, properties);

            if(!this.rxtManager.isGroupingEnabled(this.type)){
                log.debug('Omitted grouping');
                return;
            } else {
                log.debug("Grouping seems to be enabled");
            }

            addDefaultPropertyIfNotExist(userRegistry.registry, path, name);
            addDefaultPropertyIfNotExistToThis(userRegistry.registry, path, name, this);
        },
        get: function(id) {
            var item;
            try{
                item = this._super.get.call(this, id);
                var subPaths = item.path.split('/');
                item.name = subPaths[subPaths.length - 1];
                item.attributes.overview_name = item.name;
                item.version = subPaths[subPaths.length - 2];
                item.overview_version = item.version;
                item.attributes.overview_version = item.version;
                var userRegistry = getRegistry(ctx.session);
                var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
                var resource = userRegistry.registry.get(item.path);
                item.authorUserName = resource.getAuthorUserName();
                var content = resource.getContent();
                var value = '' + new Stream(new ByteArrayInputStream(content));
                item.content = value;

                var userRegistry = getRegistry(ctx.session);
                setDependencies(item, userRegistry);
            } catch(e) {
                log.debug(e);
                return null;
            }

            return item;
        },
        list: function(paging) {
            var items = this._super.list.call(this, paging);
            for (var index = 0; index < items.length; index++) {
                var result = items[index];
                var path = result.path;
                var subPaths = path.split('/');
                var name = subPaths[subPaths.length - 1];
                result.name = name;
                result.version = subPaths[subPaths.length - 2];
                result.attributes.overview_name = name;
                result.overview_version = result.version;
                result.attributes.overview_version = result.version;
                result.attributes.version = result.version;
            }
            return items;
        },
        searchByGroup: function(paging) {
            var items = this._super.searchByGroup.call(this, paging);
            for (var index = 0; index < items.length; index++) {
                var result = items[index];
                var path = result.path;
                var subPaths = path.split('/');
                var name = subPaths[subPaths.length - 1];
                result.name = name;
                result.version = subPaths[subPaths.length - 2];
                result.attributes.overview_name = name;
                result.overview_version = result.version;
                result.attributes.overview_version = result.version;
                result.attributes.version = result.version;
            }
            return items;
        },
        search: function(q, paging) {
            var results = this._super.search.call(this, q, paging);
            for (var index = 0; index < results.length; index++) {
                var result = results[index];
                var path = result.path;
                var subPaths = path.split('/');
                var name = subPaths[subPaths.length - 1];
                result.name = name;
                result.version = subPaths[subPaths.length - 2];
                result.attributes.overview_name = name;
                result.overview_version = result.version;
                result.attributes.overview_version = result.version;
                result.attributes.version = result.version;
            }
            return results;
        },
        advanceSearch: function(q, paging) {
            var results = this._super.advanceSearch.call(this, q, paging);
            for (var index = 0; index < results.length; index++) {
                var result = results[index];
                var path = result.path;
                var subPaths = path.split('/');
                var name = subPaths[subPaths.length - 1];
                result.name = name;
                result.version = subPaths[subPaths.length - 2];
                result.attributes.overview_name = name;
                result.overview_version = result.version;
                result.attributes.overview_version = result.version;
                result.attributes.version = result.version;
            }
            return results;
        },
        getName: function(asset) {
            if(asset.path){
                return asset.path.substring(asset.path.lastIndexOf("/") + 1);
            }
            return asset.name;
        },
        update: function(){

        },
        postCreate:function(){
            
        },
        getVersion: function(asset) {
            if (!asset.attributes["version"]) {
                var subPaths = asset.path.split('/');
                asset.version = subPaths[subPaths.length - 2];
                asset.attributes["version"] = asset.version;
            }
            asset.attributes["overview_version"] = asset.attributes["version"];
            return asset.attributes["version"];
        },
        getAssetGroup:function(asset){
            var results = this._super.getAssetGroup.call(this,asset);
            for (var index = 0; index < results.length; index++) {
                var result = results[index];
                var path = result.path;
                var subPaths = path.split('/');
                var name = subPaths[subPaths.length - 1];
                result.name = name;
                result.version = subPaths[subPaths.length - 2];
                result.attributes.overview_name = name;
                result.overview_version = result.version;
                result.attributes.overview_version = result.version;
                result.attributes.version = result.version;
            }
            return results;
        }
    };
};
asset.server = function(ctx) {
    var type = ctx.type;
    return {
        endpoints: {
            apis: [{
                       url: 'swaggers',
                       path: 'swaggers.jag'
                   }]
        }
    };
};
asset.renderer = function(ctx) {
    var assetManager = function(ctx) {
        var rxt = require('rxt');
        var type = ctx.assetType;
        var am = rxt.asset.createUserAssetManager(ctx.session, type);
        return am;
    };
    return {
        details:function(page,meta){
            return page;
        },
        pageDecorators: {
            populateAssetVersionDetails: function(page) {
                if ((page.assets) && (page.assets.id)) {
                    var am = assetManager(ctx);
                    var info = page.assets;
                    info.versions = [];
                    var versions;
                    var asset;
                    var assetInstance = am.get(page.assets.id);
                    var entry;
                    versions = am.getAssetGroup(assetInstance || {});
                    versions.sort(function(a1, a2) {
                        return am.compareVersions(a1, a2);
                    });
                    info.isDefault = am.isDefaultAsset(page.assets);

                    for (var index = 0; index < versions.length; index++) {
                        asset = versions[index];
                        entry = {};
                        entry.id = asset.id;
                        entry.name = asset.name;
                        entry.version = asset.attributes.version;
                        entry.isDefault = am.isDefaultAsset(asset);

                        if (asset.id == page.assets.id) {
                            entry.selected = true;
                            info.version = asset.attributes.version;
                        } else {
                            entry.selected = false;
                        }
                        entry.assetURL = this.buildAssetPageUrl(ctx.assetType, '/details/' + entry.id);
                        info.versions.push(entry);
                    }
                    info.hasMultipleVersions = (info.versions.length > 0) ? true : false;
                }

                // Following is to remove the edit button in the detail page since for asset types
                // wsdl, wadl, swagger, policy, schema, the edit operations are not allowed
                for(var index = 0; index < page.leftNav.length; index++) {
                    var button = page.leftNav[index];

                    if(button.iconClass === "btn-edit") {
                        page.leftNav.splice(index, 1);
                        index--;
                    }

                    if(button.iconClass === "btn-copy") {
                        page.leftNav.splice(index, 1);
                        index--;
                    }
                }
            },
            checkDependents:function(page) {
                if(page.assets){
                    var dependencies  = page.assets.dependencies || [];
                    var isDependentsPresent =  ( dependencies.length > 0 );
                    page.assets.isDependentsPresent = isDependentsPresent;
                }
            }
        }
    };
};
asset.configure = function() {
    return {
        meta: {
            ui: {
                icon: 'fw fw-swagger'
            }
        }
    }
};
