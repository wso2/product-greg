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
asset.manager = function(ctx) {
    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var userRegistry = userMod.userRegistry(cSession);
        return userRegistry;
    };
    var delayTillIndexed = function(milliSec) {
        var date = Date.now();
        var curDate = null;
        do {
            curDate = Date.now();
        } while (curDate-date < milliSec);
    };
    var restAssetManager = function(session){
        var rxt = require('rxt');
        var am = rxt.asset.createUserAssetManager(session, 'restservice');
        return am;
    };
    var addDefaultPropertyOfAssociationIfNotExist = function(registry, path, name){
        var associations = registry.getAllAssociations(path);

        for(var index = 0; index< associations.length; index++){
            log.debug(associations[index].getDestinationPath());
        }
        for(var index = 0; index< associations.length; index++){
            var associatedResourcePath = associations[index].getDestinationPath();

            if(!(associatedResourcePath.indexOf("schemas") > -1) && !(associatedResourcePath.indexOf("wadls") > -1)){     
                var associatedService = registry.get(associatedResourcePath);
                log.debug(associatedResourcePath);

                var serviceName = null;
                var indexVal = name.indexOf(".wadl");

                if(indexVal > -1) {
                    serviceName = name.substring(0, indexVal);
                } else {
                    serviceName = name;
                }

                var q = {};
                q.overview_name = serviceName;
                var artifacts = restAssetManager(ctx.session).search(q);

                if(artifacts.length < 2) {
                    associatedService.addProperty("default", "true");
                    registry.put(associatedResourcePath, associatedService);
                }
            }
        }
    };
    var addDefaultPropertyIfNotExist = function(registry, name, am) {
        var q = {};
        q.overview_name = name;
        var artifacts = am.search(q);

        while(artifacts.length == 0) {
            delayTillIndexed(3000);
            artifacts = am.search(q);
        }

        if(artifacts.length == 1) {
            var id = artifacts[0].id;
            var artifactObject = am.am.manager.getGenericArtifact(id);
            var wadlRelativePath = artifactObject.getPath();
            var wadlPath = "/_system/governance" + wadlRelativePath;
            var wadlResource = registry.get(wadlPath);
            wadlResource.addProperty("default", "true");
            try{
                registry.put(wadlPath, wadlResource);
            } catch (e){
                log.error(e);
                throw e;
            }

            addDefaultPropertyOfAssociationIfNotExist(registry, wadlPath, name);
        } 
    };

    var getAssociations = function(associatedResources, userRegistry){
        //Array to store the association names.
        var associations = [];

        for(var index = 0; index< associatedResources.length; index++){
            var deps = {};
            var path = associatedResources[index].getDestinationPath();
            var subPaths = path.split('/');
            var associationTypePlural = subPaths[4];
            var associationName = subPaths[subPaths.length - 1];
            var resource = userRegistry.registry.get(path);
            var associationUUID = resource.getUUID();
            deps.associationName = associationName;
            deps.associationType = associationTypePlural.substring(0, associationTypePlural.lastIndexOf('s'));
            deps.associationUUID = associationUUID;

            if(deps.associationType == "restservice") {
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
    	//without this 'create' method does not work.('options' object not retrieved.)
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
            var parentPath = "/_system/governance/trunk/wadls/".concat(version);
            var mediaType = "application/wadl+xml";
            var javaArray = Packages.java.lang.reflect.Array;
            var properties = javaArray.newInstance(java.lang.String, 1, 2);
            properties[0][0] = 'version';
            properties[0][1] = version;

            utils.importResource(parentPath, name, mediaType, '', url, '', userRegistry.registry, properties);

            if(!this.rxtManager.isGroupingEnabled(this.type)){
                log.debug('Omitted grouping');
                return;
            } 
            else {
                log.debug("Grouping seems to be enabled");
            }
            addDefaultPropertyIfNotExist(userRegistry.registry, name, this);
        },
        get: function(id) {
            var item;

            try {
                item = this._super.get.call(this, id);
                var subPaths = item.path.split('/');
                item.name = subPaths[subPaths.length - 1];
                item.attributes.overview_name = item.name;
                item.version = subPaths[subPaths.length - 2];
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
            }
            return results;
        },
        getName: function(asset) {
            return asset.name;
        },
        update: function(){

        },
        postCreate:function(){
            
        },
        addTags: function(){

        }
    };
};
asset.server = function(ctx) {
    var type = ctx.type;
    return {
        endpoints: {
            apis: [{
                       url: 'wadls',
                       path: 'wadls.jag'
                   }]
        }
    };
};
asset.renderer =  function (ctx){
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
                for(index in page.leftNav) {
                    var button = page.leftNav[index];

                    if(button.iconClass === "btn-edit") {
                        page.leftNav.splice(index, 1);
                    }

                    if(button.iconClass === "btn-copy") {
                        page.leftNav.splice(index, 1);
                    }
                }
            }
        }
    };
};
asset.configure = function() {
    return {
        meta: {
            ui: {
                icon: 'fw fw-wadl'
            }
        }
    }
};