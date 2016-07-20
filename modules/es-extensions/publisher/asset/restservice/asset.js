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
    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var userRegistry = userMod.userRegistry(cSession);
        return userRegistry;
    };

    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                if (genericArtifacts[index] != null) {
                    //extract the association name via the path.
                    var path = genericArtifacts[index].getPath();
                    var resource = userRegistry.registry.get("/_system/governance" + path);
                    var mediaType = resource.getMediaType();

                    if(mediaType == "application/wadl+xml" || mediaType == "application/swagger+json") {
                        var subPaths = path.split('/');
                        var associationName = subPaths[subPaths.length - 1];
                        var associationUUID = resource.getUUID();
                        var associationVersion = subPaths[subPaths.length - 2];
                        deps.associationName = associationName;

                        if(mediaType == "application/wadl+xml") {
                            deps.associationType = "wadl";
                        } else if(mediaType == "application/swagger+json") {
                            deps.associationType = "swagger";
                        }

                        deps.associationUUID = associationUUID;
                        deps.associationVersion = associationVersion;
                        associations.push(deps);
                    }
                }
            }
        }
        return associations;
    };

    var setDependencies = function(genericArtifact, asset ,userRegistry) {
        try {
            //get dependencies of the artifact.
            var dependencyArtifacts = genericArtifact.getDependencies();
            asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
        } catch(e) {
            asset.dependencies = [];
        }
    };

    return {
        get:function(id) {
            var item;
            try {
                item = this._super.get.call(this, id);
                var rawArtifact = this.am.manager.getGenericArtifact(id);
                var userRegistry = getRegistry(ctx.session);
                setDependencies(rawArtifact, item, userRegistry);
            } catch(e) {
                log.debug(e);
                return null;
            }

            return item;
        },
        create: function(options) {
            var name = encodeURIComponent(options.attributes.overview_name);
            var version = options.attributes.overview_version;
            var rxt = require('rxt');
            var am = rxt.asset.createUserAssetManager(ctx.session, this.type);
            var query = {};
            query.overview_name = name;
            query._wildcard = false;
            var assets = am.search(query);
            for (var i = 0; i < assets.length; i++) {
                if (assets[i].version == version) {
                    var msg = "Resource already exist with same Name \"" + decodeURIComponent(name) + "\" and version \"" + version + "\"";
                    var exceptionUtils = require('utils');
                    var exceptionModule = exceptionUtils.exception;
                    var constants = rxt.constants;
                    throw exceptionModule.buildExceptionObject(msg, constants.STATUS_CODES.BAD_REQUEST);
                }
            }
            return this._super.create.call(this, options);
        },
        combineWithRxt: function(asset) {
            var modAsset = this._super.combineWithRxt.call(this, asset);
            if (asset.dependencies) {
                var dependencies = asset.dependencies;
                modAsset.dependencies = dependencies;
            }
            return modAsset;
        }
    }
};

asset.configure = function () {
    return {
        meta: {
            ui: {
                icon: 'fw fw-rest-service'
            },
            lifecycle: {
                commentRequired: false,
                defaultAction: '',
                deletableStates: ['*'],
                defaultLifecycleEnabled: false,
                publishedStates: ['Published']
            }
        },
        table: {
            overview: {
                fields: {
                    name: {
                        readonly:true,
                        placeholder: "Name"
                    },
                    context: {
                        placeholder: "Context"
                    },
                    version: {
                        readonly:true,
                        placeholder: "Version"
                    },
                    description: {
                        placeholder: "Description"
                    }
                }
            },
            interface: {
                fields: {
                    transports: {
                        placeholder: "Transports"
                    },
                    wsdl: {
                    	placeholder: "WSDL"
                    },
                    wadl: {
                    	placeholder: "WADL"
                    },
                    swagger: {
                    	placeholder: "Swagger"
                    }
                }
            }
        }
    }
};
