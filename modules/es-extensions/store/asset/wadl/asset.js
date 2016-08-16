/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var configs = require("/extensions/assets/wadl/config/properties.json");
    var tenantAPI = require('/modules/tenant-api.js').api;
    var rxtApp = require('rxt').app;
    var availableUIAssetTypes = rxtApp.getUIActivatedAssets(ctx.tenantId);
    var availableAssetTypes = rxtApp.getActivatedAssets(ctx.tenantId);
    var allAvailableAssetTypes = String(availableAssetTypes.concat(availableUIAssetTypes));

    /**
     * The function augments the provided query to include published state information
     * @param  {[type]} query [description]
     * @return {[type]}       The provided query object
     */
    var buildPublishedQuery = function(query) {
        //Get all of the published assets
        var publishedStates = ctx.rxtManager.getPublishedStates(ctx.assetType) || [];
        //Determine if there are any published states
        if (publishedStates.length == 0) {
            return query;
        }
        //If there is no query then build a new one
        if (!query) {
            query = {};
        }
        //TODO: Even though an array is sent in only the first search value is accepted
        query.lcState = [publishedStates[0]];
        return query;
    };
    var getRegistry = function(cSession) {
        var tenantDetails = tenantAPI.createTenantAwareAssetResources(cSession,{type:ctx.assetType});
        if((!tenantDetails)&&(!tenantDetails.am)) {
            log.error('The tenant-api was unable to create a registry instance by resolving tenant details');
            throw 'The tenant-api  was unable to create a registry instance by resolving tenant details';
        }
        return tenantDetails.am.registry;
    };
    var setCustomAssetAttributes = function (asset, userRegistry){
        var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
        //check for the wadl url in the asset json object
        var path = asset.path;
        if (path != null) {
           var subPaths = path.split('/');
            //setting asset.name did not work, as it seems there comes a default 'name'
            //attribute.
            var wadlname = subPaths[subPaths.length - 1];
            var version = subPaths[subPaths.length - 2];
            var resource = userRegistry.registry.get(path);
            var authorUserName = resource.getAuthorUserName();
            var content = resource.getContent();
            var value = '' + new Stream(new ByteArrayInputStream(content));
            //since this is wsdlcontent.
            asset.wadlname = wadlname;
            asset.assetName = wadlname;
            asset.name = wadlname;
            asset.attributes.overview_name = wadlname;
            asset.overview_name = wadlname;
            asset.version = version;
            asset.attributes.overview_version = version;
            asset.overview_version = version;
            asset.authorUserName = authorUserName;
            var ComparatorUtils = Packages.org.wso2.carbon.governance.comparator.utils.ComparatorUtils;
            var comparatorUtils = new ComparatorUtils();
            var mediaType = "application/wadl+xml";
            try {
                value = comparatorUtils.prettyFormatText(value,mediaType);
            } catch (ex){

            }
            asset.wadlContent = value;
        }
    };
    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                var path = genericArtifacts[index].getPath();
                var mediaType = genericArtifacts[index].getMediaType();
                var name = genericArtifacts[index].getQName().getLocalPart();
                var govUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
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
                var resource = userRegistry.registry.get(configs.depends_asset_path_prefix + path);
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
    return {
        //due to a bug needed to replicate the 'search' method. JIRA:https://wso2.org/jira/browse/STORE-561
        search: function(query, paging) {
            //query = buildPublishedQuery(query);--commented this inorder to let anystate 
            //to be visible in store.
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
            var asset = this._super.get.call(this, id);
            var userRegistry = getRegistry(ctx.session);
            try {
                setCustomAssetAttributes(asset, userRegistry);
            } catch (e){}            //get the GenericArtifactManager
            var rawArtifact = this.am.manager.getGenericArtifact(id);
            try {
                setDependencies(rawArtifact, asset, userRegistry);
            } catch (e){}
            try {
                setDependents(rawArtifact, asset, userRegistry);
            } catch (e){}
            return asset;
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

asset.configure = function() {
    return {
        meta: {
            ui: {
                icon: 'fw fw-wadl',
                iconColor: 'blue'
            },
            downloadable:true,
            isDependencyShown: true
        }
    }
};
