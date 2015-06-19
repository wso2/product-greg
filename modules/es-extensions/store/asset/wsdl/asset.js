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
  var configs = require("/extensions/assets/wsdl/config/properties.json");
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
        var userMod = require('store').user;
        var userRegistry = userMod.userRegistry(cSession);
        return userRegistry;
    };
    var setCustomAssetAttributes = function(asset, userRegistry) {
        var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
        //check for the wsdl url in the asset json object
        var path = asset.path;
        if (path != null) {
            var subPaths = path.split('/');
            //setting asset.name did not work, as it seems there comes a default 'name'
            //attribute.
            var wsdlname = subPaths[subPaths.length - 1];
            var version = subPaths[subPaths.length - 2];
            var resource = userRegistry.registry.get(path);
            var authorUserName = resource.getAuthorUserName();
            var content = resource.getContent();
            var value = '' + new Stream(new ByteArrayInputStream(content));
            //since this is wsdlcontent.
            asset.wsdlname = wsdlname;
            asset.assetName = wsdlname;
            asset.version = version;
            asset.authorUserName = authorUserName;
            asset.wsdlContent = value;
        }
    };
    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                //extract the association name via the path.
                var path = genericArtifacts[index].getPath();
                var subPaths = path.split('/');
                var associationTypePlural = subPaths[2];
                var associationName = subPaths[subPaths.length - 1];
                var resource = userRegistry.registry.get(configs.depends_asset_path_prefix+path);
                var associationUUID = resource.getUUID();
                deps.associationName = associationName;
                deps.associationType = associationTypePlural.substring(0,associationTypePlural.lastIndexOf('s'));
                deps.associationUUID = associationUUID;
                associations.push(deps);
            }
        }
        return associations;
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
                setCustomAssetAttributes(asset, userRegistry);
            }
            return assets;
        },
        get: function(id) {
            //TODO: support services added through WSDL, once multiple lifecycle is supported.
            var asset = this._super.get.call(this, id);
            var userRegistry = getRegistry(ctx.session);
            setCustomAssetAttributes(asset, userRegistry);
            //get the GenericArtifactManager
            var rawArtifact = this.am.manager.getGenericArtifact(id);
            setDependencies(rawArtifact, asset, userRegistry);
            setDependents(rawArtifact, asset, userRegistry);
            return asset;
        }
    };
};