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
    var configs = require("/extensions/assets/soapservice/config/properties.json");
    var QName = Packages.javax.xml.namespace.QName;
    var getRegistry = function(cSession) {
        var userMod = require('store').user;
        var userRegistry = userMod.userRegistry(cSession);
        return userRegistry;
    };

    var wsdlAssetManager = function(session){
        var rxt = require('rxt');
        var am = rxt.asset.createUserAssetManager(session, 'wsdl');
        return am;
    };

    /**
     * Checks if there are any other asset versions
     * @param  {[type]}  asset [description]
     * @return {Boolean}       [description]
     */
    var isOnlyAssetVersion = function(asset, am) {
        var versions = am.getAssetGroup(asset);
        return (versions.length < 1) ? true : false;
    };

    /**
     * Method to create artifact in create and update asset
     * @param manager
     * @param options
     * @returns {*}
     */
    var createArtifact = function (manager, options) {
        var name, attribute, i, length, lc, artifact,
            attributes = options.attributes;
        if(attributes.overview_namespace) {
            artifact = manager.newGovernanceArtifact(new QName(attributes.overview_namespace, options.name))
        } else {
            artifact = manager.newGovernanceArtifact(new QName(options.name))
        }
        for (name in attributes) {
            if (attributes.hasOwnProperty(name)) {
                attribute = attributes[name];
                if (attribute instanceof Array) {
                    artifact.setAttributes(name, attribute);
                } else {
                    artifact.setAttribute(name, attribute);
                }
            }
        }
        if (options.id) {
            artifact.id = options.id;
        }
        if (options.content) {
            if (options.content instanceof Stream) {
                artifact.setContent(IOUtils.toByteArray(options.content.getStream()));
            } else {
                artifact.setContent(new java.lang.String(options.content).getBytes());
            }
        }
        lc = options.lifecycles;
        if (lc) {
            length = lc.length;
            for (i = 0; i < length; i++) {
                artifact.attachLifeCycle(lc[i]);
            }
        }
        return artifact;
    };

    var setCustomAssetAttributes = function(asset, userRegistry) {
        var interfaceUrl=asset.attributes.interface_wsdlURL;
        if (interfaceUrl != null) {
            try {
                var resource = userRegistry.registry.get(interfaceUrl);
                var wsdlUUID = resource.getUUID();
                asset.wsdl_uuid = wsdlUUID;
                asset.wsdl_url = asset.attributes.interface_wsdlURL;
            } catch(e) {
                asset.wsdl_uuid = "";
                asset.wsdl_url = "";
            }
        }
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
                    var subPaths = path.split('/');
                    var associationTypePlural = subPaths[2];
                    var associationName = subPaths[subPaths.length - 1];
                    var resource = userRegistry.registry.get(configs.depends_asset_path_prefix + path);
                    var associationUUID = resource.getUUID();
                    deps.associationName = associationName;
                    deps.associationType = associationTypePlural.substring(0, associationTypePlural.lastIndexOf('s'));
                    deps.associationUUID = associationUUID;

                    if(deps.associationType == "wsdl") {
                        deps.associationVersion = subPaths[subPaths.length - 2];
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

    var setDependents = function(genericArtifact, asset, userRegistry) {
        try {
            var dependentArtifacts = genericArtifact.getDependents();
            asset.dependents = getAssociations(dependentArtifacts, userRegistry);
        } catch(e) {
            asset.dependents = [];
        }
    };

    var addDefaultPropertyIfNotExist = function(registry, path, name){
        var associations = registry.getAllAssociations(path);

        for(var index = 0; index< associations.length; index++){
            var associatedResourcePath = associations[index].getDestinationPath();
            if(associatedResourcePath.indexOf("wsdl") > -1){     
                var associatedWSDL = registry.get(associatedResourcePath);

                var wsdlName = name + ".wsdl";
                var q = {};
                q.overview_name = wsdlName;
                var artifacts = wsdlAssetManager(ctx.session).search(q);
                log.debug(artifacts.length);
                if(artifacts.length < 2) {
                    associatedWSDL.addProperty("default", "true");
                    registry.put(associatedResourcePath, associatedWSDL);
                }
            }
        }
    };

    return {
        get: function(id) {
            var asset;
            try {
                asset = this._super.get.call(this, id); 
                var userRegistry = getRegistry(ctx.session);
                setCustomAssetAttributes(asset, userRegistry);
                var rawArtifact = this.am.manager.getGenericArtifact(id);
                setDependencies(rawArtifact, asset, userRegistry);
                setDependents(rawArtifact, asset, userRegistry);
            } catch(e) {
                log.error(e);
                return null;
            }

            return asset;
        },
        combineWithRxt: function(asset) {
            var modAsset = this._super.combineWithRxt.call(this, asset);
            if (asset.wsdl_uuid) {
                var wsdlUUID = asset.wsdl_uuid;
                modAsset.wsdl_uuid = wsdlUUID;
            }
            if (asset.wsdl_url) {
                var wsdlURL = asset.wsdl_url;
                modAsset.wsdl_url = wsdlURL;
                for(var table in modAsset.tables) {
                    if (table.name == "interface") {
                        table.fields.wsdlURL.value = modAsset.wsdl_url;
                    }
                }
            }
            if (asset.interfaceType) {
                var interfaceType = asset.interfaceType;
                modAsset.interfaceType = interfaceType;
            }
            if (asset.dependencies) {
                var dependencies = asset.dependencies;
                modAsset.dependencies = dependencies;
            }
            if (asset.dependents) {
                var dependents = asset.dependents;
                modAsset.dependents = dependents;
            }
            return modAsset;
        },
        createVersion: function(options, newAsset) {
            var rxtModule = require('rxt');
            var existingAttributes = {};
            var isLCEnabled = false;
            var isDefaultLCEnabled = false;
            if (!options.id || !newAsset) {
                log.error('Unable to process create-version without having a proper ID or a new asset instance.');
                return false;
            }
            var existingAsset = this.get(options.id);
            var ctx = rxtModule.core.createUserAssetContext(session, options.type);
            var context = rxtModule.core.createUserAssetContext(session, options.type);
            var oldId = existingAsset.id;
            delete existingAsset.id;
            for (var key in newAsset) {
                existingAsset.attributes[key] = newAsset[key];
            }
            existingAttributes.attributes = existingAsset.attributes;
            existingAttributes.name = existingAsset.attributes['overview_name'];
            var tags = this.registry.tags(existingAsset.path);
            //remove wsdlurl and endpoint to make the soap service a shallow copy
            delete existingAttributes.attributes.interface_wsdlURL;
            delete existingAttributes.attributes.endpoints;
            delete existingAttributes.attributes.endpoints_entry;
            this.create(existingAttributes);
            createdAsset = this.get(existingAttributes.id);
            this.addTags(existingAttributes.id, tags);
            isLCEnabled = context.rxtManager.isLifecycleEnabled(options.type);
            isDefaultLCEnabled = context.rxtManager.isDefaultLifecycleEnabled(options.type);
            this.postCreate(createdAsset, ctx);
            this.update(existingAttributes);
            //Continue attaching the lifecycle
            if (isDefaultLCEnabled && isLCEnabled) {
                var isLcAttached = this.attachLifecycle(existingAttributes);
                //Check if the lifecycle was attached
                if (isLcAttached) {
                    var synched = this.synchAsset(existingAttributes);
                    if (synched) {
                        this.invokeDefaultLcAction(existingAttributes);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug('Failed to invoke default action as the asset could not be synched.')
                        }
                    }
                }
            }
            return existingAttributes.id;
        },
        create: function(options) {
            var log = new Log();
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

            var isDefault = false;
            var manager = this.am.manager;
            var artifact = createArtifact(manager, options);
            if((options.hasOwnProperty('_default')) &&(options._default===true)){
                delete options._default;
                isDefault = true;
            }
            manager.addGenericArtifact(artifact);
            log.debug('Service successfully created');
            options.id = artifact.getId();
            var asset = this.get(options.id);
            if(!this.rxtManager.isGroupingEnabled(this.type)){
                log.debug('Omitted grouping');
                return;
            }
            if ( (isDefault) || (isOnlyAssetVersion(asset,this)) ){
                this.setAsDefaultAsset(asset);
            }

            var wsdlRelativePath = artifact.getPath();
            var wsdlPath = "/_system/governance" + wsdlRelativePath;
            addDefaultPropertyIfNotExist(getRegistry(ctx.session).registry, wsdlPath, options.attributes.overview_name);
        },
        update: function(options) {
            var log = new Log();
            var manager = this.am.manager;
            var isDefault = false;
            if((options.hasOwnProperty('_default'))&&(options._default === true)){
                isDefault = true;
            }
            var asset = this.get(options.id);
            var artifact = createArtifact(manager, options);
            manager.updateGenericArtifact(artifact);
            log.debug('Service successfully updated');
            options.id = artifact.getId();
            if(!this.rxtManager.isGroupingEnabled(this.type)){
                log.debug('Omitting grouping step');
                return;
            }
            if(isDefault){
                this.setAsDefaultAsset(asset);
            }
        },
        postCreate:function(){
            
        }
    }
};

// removing custom asset renderer to use the default one. we need to show endpoints in publisher
/*asset.renderer = function(ctx) {
    var hideTables = function(page) {
        var tables = [];
        for (var index in page.assets.tables) {
            if (page.assets.tables[index].name == 'overview') {
                delete page.assets.tables[index].fields.scopes;
                delete page.assets.tables[index].fields.types;
                tables.push(page.assets.tables[index]);
            } else if (page.assets.tables[index].name == 'contacts') {
                tables.push(page.assets.tables[index]);
            } else if (page.assets.tables[index].name == 'interface') {
                tables.push(page.assets.tables[index]);
            } else if (page.assets.tables[index].name == 'docLinks') {
                tables.push(page.assets.tables[index]);
            }
        }
        page.assets.tables = tables;
        return page;
    };
    return {
        create: function(page) {
            return hideTables(page);
        },
        update: function(page) {
            return hideTables(page);
        },
        list: function(page) {
            require('/modules/page-decorators.js').pageDecorators.assetCategoryDetails(ctx, page, this);
            return hideTables(page);
        },
        details: function(page) {
            return hideTables(page);
        }
    };
};*/
asset.configure = function() {
    return {
        meta: {
            ui: {
                icon: 'fw fw-soap'
            },
            lifecycle: {
                commentRequired: false,
                defaultAction: '',
                deletableStates: ['*'],
                defaultLifecycleEnabled:false,
                publishedStates: ['Published']
            },
            grouping: {
                groupingEnabled: false,
                groupingAttributes: ['overview_name']
            }
        },
        table: {
            overview: {
                fields: {
                    name: {
                        readonly:true,
                        placeholder: "Name"
                    },
                    namespace: {
                        readonly:true,
                        placeholder: "Namespace"
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
                    wsdlUrl: {
                        placeholder: "Wsdl Url"
                    }
                }
            }
        }
    }
};
