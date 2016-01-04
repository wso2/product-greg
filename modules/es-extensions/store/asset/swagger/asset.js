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
    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var server = require('store').server;
        var user = server.current(cSession);
        var userRegistry;
        if (user) {
            userRegistry = userMod.userRegistry(cSession);
        } else {
            userRegistry = server.anonRegistry(tenantId);
        }
        return userRegistry;
    };

    var setCustomAssetAttributes = function(asset, userRegistry) {
        var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;

        var path = asset.path;
        if (path != null) {
            var subPaths = path.split('/');
            var swaggerName = subPaths[subPaths.length - 1];
            var version = subPaths[subPaths.length - 2];
            var resource = userRegistry.registry.get(path);
            var authorUserName = resource.getAuthorUserName();
            var content = resource.getContent();
            var value = '' + new Stream(new ByteArrayInputStream(content));
            asset.swaggerName = swaggerName;
            asset.assetName = swaggerName;
            asset.attributes.overview_name = swaggerName;
            asset.overview_name = swaggerName;
            asset.version = version;
            asset.attributes.overview_version = version;
            asset.overview_version = version;
            asset.authorUserName = authorUserName;
            asset.swaggerContent = value;
        }
    };

    var getAssociations = function(genericArtifacts, userRegistry){
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                var path = genericArtifacts[index].getPath();
                var mediaType = genericArtifacts[index].getMediaType();
                var name = genericArtifacts[index].getQName().getLocalPart();
                var govUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils
                var keyName = govUtils.getArtifactConfigurationByMediaType(getRegistry(ctx.session).registry, mediaType).getKey();
                var subPaths = path.split('/');
                var associationTypePlural = subPaths[2];
                var associationName = name;
                var resource = userRegistry.registry.get('/_system/governance' + path);
                var associationUUID = resource.getUUID();
                deps.associationName = associationName;
                deps.associationType = keyName;
                deps.associationUUID = associationUUID;
                associations.push(deps);
            }
        }
        return associations;
    };

    var setDependencies = function(genericArtifact, asset ,userRegistry) {
        var dependencyArtifacts = genericArtifact.getDependencies();
        asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
    };

    var setDependents = function(genericArtifact, asset, userRegistry) {
        var dependentArtifacts = genericArtifact.getDependents();
        asset.dependents = getAssociations(dependentArtifacts, userRegistry);
    };

    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            var userRegistry = getRegistry(ctx.session);
            for (var index in assets) {
                var asset = assets[index];
                try {
                    setCustomAssetAttributes(asset, userRegistry);
                } catch (e){}

                var path = asset.path;
                var subPaths = path.split('/');
                var name = subPaths[subPaths.length - 1];
                asset.name = name;
                asset.version = subPaths[subPaths.length - 2];
                asset.attributes.overview_name = name;
                asset.overview_version = asset.version;
                asset.attributes.overview_version = asset.version;
                asset.attributes.version = asset.version;
            }
            return assets;
        },
        get: function(id) {
            var item;
            try{
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

                var rawArtifact = this.am.manager.getGenericArtifact(id);
                try {
                    setDependencies(rawArtifact, item, userRegistry);
                } catch (e){}
                try {
                    setDependents(rawArtifact, item, userRegistry);
                } catch (e){}
            } catch(e) {
                log.error(e);
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

                result.attributes.overview_name = result.name;
                result.attributes.overview_version = result.version;
            }
            return items;
        },
        getName: function(asset) {
            if(asset.path){
                asset.name = asset.path.substring(asset.path.lastIndexOf("/") + 1);
                asset.overview_name = asset.name;
                asset.attributes.overview_name = asset.name;
                return asset.path.substring(asset.path.lastIndexOf("/") + 1);
            }
            return asset.name;
        },
        getVersion: function(asset) {
            asset.attributes["overview_version"] = asset.attributes["version"];
            return asset.attributes["version"];
        }
    };
};

asset.configure = function() {
    return {
        meta: {
            ui: {
                icon: 'fw fw-swagger',
                iconColor: 'grey'
            }
        }
    }
};