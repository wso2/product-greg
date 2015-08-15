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
    var setCustomAssetAttributes = function(asset, userRegistry) {
        var wadlUrl=asset.attributes.overview_wadl;
        if (wadlUrl != null) {
            var resource = userRegistry.registry.get(wadlUrl);
            var wadlContent = getInterfaceTypeContent(resource);
            asset.wadlContent = wadlContent;
        }
    }; 

	var setDependencies = function(genericArtifact, asset ,userRegistry) {
        var dependencyArtifacts = genericArtifact.getDependencies();
        asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
    };

    var setDependents = function(genericArtifact, asset, userRegistry) {
        var dependentArtifacts = genericArtifact.getDependents();
        asset.dependents = getAssociations(dependentArtifacts, userRegistry);
    };

    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var userRegistry = userMod.userRegistry(cSession);
        return userRegistry;
    };

    var getInterfaceTypeContent = function (resource) {
        var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
        var content = resource.getContent();
        var value = '' + new Stream(new ByteArrayInputStream(content));
        return value;
    };

    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                var path = genericArtifacts[index].getPath();
                var resource = userRegistry.registry.get('/_system/governance/'+ path);
                var mediaType = resource.getMediaType();
                var name = genericArtifacts[index].getQName().getLocalPart();
                var govUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils
                var keyName = govUtils.getArtifactConfigurationByMediaType(getRegistry(ctx.session).registry, mediaType).getKey();
                var subPaths = path.split('/');
                var associationName = name;
                var associationUUID = resource.getUUID();
                deps.associationName = associationName;
                deps.associationType = keyName;
                deps.associationUUID = associationUUID;

                if(deps.associationType == "wadl") {
                    associations.push(deps);
                }
            }
        }
        return associations;
    };

    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get: function(id) {
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

asset.configure = function () {
    return {
        meta: {
            ui: {
                icon: 'fw fw-rest-service'
            }
        }
    }
};